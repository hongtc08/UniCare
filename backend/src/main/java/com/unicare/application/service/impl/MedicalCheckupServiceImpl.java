package com.unicare.application.service.impl;

import com.unicare.application.dto.request.PrescribedMedicineRequest;
import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.service.MedicalCheckupService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.MedicalCheckup;
import com.unicare.domain.entity.PrescribedMedicine;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.repository.MedicalCheckupRepository;
import com.unicare.domain.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class MedicalCheckupServiceImpl implements MedicalCheckupService {

    private static final String STATUS_COMPLETED = "COMPLETED";

    private final MedicalCheckupRepository medicalCheckupRepository;
    private final UserRepository userRepository;

    public MedicalCheckupServiceImpl(
            MedicalCheckupRepository medicalCheckupRepository,
            UserRepository userRepository) {
        this.medicalCheckupRepository = medicalCheckupRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalCheckupDetailResponse getCheckupDetail(Long checkupId, String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);

        MedicalCheckup checkup = medicalCheckupRepository.findByIdWithDetails(checkupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu khám với ID: " + checkupId));

        if (currentUser.getRole() == Role.BAN_QUAN_LY) {
            throw new AccessDeniedException("Ban quản lý không có quyền truy cập hồ sơ bệnh án chi tiết của sinh viên");
        }

        if (currentUser.getRole() == Role.SINH_VIEN) {
            if (checkup.getStudent() == null || !checkup.getStudent().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Sinh viên chỉ được phép xem thông tin phiếu khám của chính mình");
            }
        }

        return MedicalCheckupDetailResponse.fromEntity(checkup);
    }

    @Override
    @Transactional
    public MedicalCheckupDetailResponse updatePrescription(Long checkupId, UpdatePrescriptionRequest request, String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);

        if (currentUser.getRole() != Role.NHAN_VIEN_Y_TE) {
            throw new AccessDeniedException("Chỉ tài khoản Bác sĩ / Nhân viên y tế mới có quyền kê đơn thuốc");
        }

        MedicalCheckup checkup = medicalCheckupRepository.findByIdWithDetails(checkupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu khám với ID: " + checkupId));

        if (checkup.getStaff() == null || !checkup.getStaff().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Chỉ bác sĩ phụ trách ca khám mới có quyền kê đơn thuốc cho phiếu khám này");
        }

        if (STATUS_COMPLETED.equalsIgnoreCase(checkup.getStatus())) {
            throw new BadRequestException("Phiếu khám đã hoàn tất (COMPLETED), đơn thuốc đã bị khóa vĩnh viễn và không thể chỉnh sửa");
        }

        checkup.clearPrescribedMedicines();

        // Nếu có thuốc được kê, tiến hành validate và thêm vào đơn
        if (request != null && request.getMedicines() != null && !request.getMedicines().isEmpty()) {
            Set<String> seenNames = new HashSet<>();
            for (PrescribedMedicineRequest item : request.getMedicines()) {
                String name = item.getMedicineName() != null ? item.getMedicineName().trim() : "";
                if (name.isEmpty()) {
                    throw new BadRequestException("Tên thuốc không được để trống");
                }
                if (name.length() > 100) {
                    throw new BadRequestException("Tên thuốc không được vượt quá 100 ký tự: " + name);
                }
                if (item.getQuantity() == null || item.getQuantity() < 1) {
                    throw new BadRequestException("Số lượng thuốc phải lớn hơn hoặc bằng 1 đối với thuốc: " + name);
                }
                String instruction = item.getDosageInstruction() != null ? item.getDosageInstruction().trim() : "";
                if (instruction.isEmpty()) {
                    throw new BadRequestException("Hướng dẫn liều dùng không được để trống đối với thuốc: " + name);
                }
                if (instruction.length() > 200) {
                    throw new BadRequestException("Hướng dẫn liều dùng không được vượt quá 200 ký tự đối với thuốc: " + name);
                }

                String lowerName = name.toLowerCase();
                if (!seenNames.add(lowerName)) {
                    throw new BadRequestException("Thuốc '" + name + "' bị trùng lặp trong đơn thuốc. Vui lòng gộp số lượng hoặc chỉnh sửa lại");
                }

                PrescribedMedicine medicine = PrescribedMedicine.builder()
                        .medicineName(name)
                        .quantity(item.getQuantity())
                        .dosageInstruction(instruction)
                        .build();

                checkup.addPrescribedMedicine(medicine);
            }
        }

        MedicalCheckup savedCheckup = medicalCheckupRepository.save(checkup);
        return MedicalCheckupDetailResponse.fromEntity(savedCheckup);
    }

    private User resolveCurrentUser(String currentUsername) {
        if (currentUsername == null || currentUsername.isBlank()) {
            throw new AccessDeniedException("Yêu cầu đăng nhập để thực hiện thao tác này");
        }
        return userRepository.findByUsername(currentUsername.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + currentUsername));
    }
}
