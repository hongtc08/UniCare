package com.unicare.application.service.impl;

import com.unicare.application.dto.request.HealthInsuranceRequest;
import com.unicare.application.dto.response.HealthInsuranceResponse;
import com.unicare.application.service.HealthInsuranceService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.common.util.ValidationUtils;
import com.unicare.domain.entity.HealthInsurance;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.repository.HealthInsuranceRepository;
import com.unicare.domain.repository.StudentRepository;
import com.unicare.domain.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HealthInsuranceServiceImpl implements HealthInsuranceService {

    private final HealthInsuranceRepository healthInsuranceRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public HealthInsuranceServiceImpl(
            HealthInsuranceRepository healthInsuranceRepository,
            StudentRepository studentRepository,
            UserRepository userRepository
    ) {
        this.healthInsuranceRepository = healthInsuranceRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthInsuranceResponse> getHealthInsurances(String search, String status, String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);
        LocalDate today = LocalDate.now();

        // Nếu là Sinh viên, chỉ được phép xem thẻ của chính mình
        if (currentUser.getRole() == Role.SINH_VIEN) {
            return healthInsuranceRepository.findByStudentIdWithDetails(currentUser.getId())
                    .map(h -> Collections.singletonList(HealthInsuranceResponse.fromEntity(h, today)))
                    .orElse(Collections.emptyList());
        }

        // Bác sĩ / Quản trị viên xem toàn bộ danh sách, có hỗ trợ tìm kiếm và lọc theo trạng thái
        String cleanSearch = ValidationUtils.cleanSearch(search);
        List<HealthInsurance> list = healthInsuranceRepository.searchHealthInsurances(cleanSearch);

        return list.stream()
                .map(h -> HealthInsuranceResponse.fromEntity(h, today))
                .filter(res -> status == null || status.isBlank() || res.getStatus().equalsIgnoreCase(status.trim()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HealthInsuranceResponse getHealthInsuranceById(Long id, String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);
        HealthInsurance insurance = healthInsuranceRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thẻ BHYT với mã ID: " + id));

        validateStudentAccess(currentUser, insurance.getStudent().getId());
        return HealthInsuranceResponse.fromEntity(insurance, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public HealthInsuranceResponse getHealthInsuranceByStudentId(Long studentId, String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);
        validateStudentAccess(currentUser, studentId);

        HealthInsurance insurance = healthInsuranceRepository.findByStudentIdWithDetails(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thẻ BHYT cho sinh viên với mã ID: " + studentId));

        return HealthInsuranceResponse.fromEntity(insurance, LocalDate.now());
    }

    @Override
    @Transactional
    public HealthInsuranceResponse createHealthInsurance(HealthInsuranceRequest request, String currentUsername) {
        validateRequestNotNull(request);
        User currentUser = resolveCurrentUser(currentUsername);

        Long effectiveStudentId = resolveEffectiveStudentId(currentUser, request.getStudentId());
        validateInsuranceDates(request.getEffectiveDate(), request.getExpirationDate());

        String cleanCardNumber = ValidationUtils.requireValidBhytCardNumber(request.getCardNumber());
        if (healthInsuranceRepository.existsByCardNumberIgnoreCase(cleanCardNumber)) {
            throw new DuplicateResourceException("Mã thẻ BHYT '" + cleanCardNumber + "' đã tồn tại trên hệ thống.");
        }

        Student student = resolveStudent(effectiveStudentId);
        if (healthInsuranceRepository.existsByStudentId(student.getId())) {
            throw new DuplicateResourceException("Sinh viên này đã có thông tin thẻ BHYT trên hệ thống.");
        }

        String cleanHospital = ValidationUtils.requireBoundedText(
                request.getInitialHospital(), 150, "Nơi đăng ký khám chữa bệnh ban đầu"
        );

        HealthInsurance insurance = HealthInsurance.builder()
                .cardNumber(cleanCardNumber)
                .student(student)
                .effectiveDate(request.getEffectiveDate())
                .expirationDate(request.getExpirationDate())
                .initialHospital(cleanHospital)
                .build();

        HealthInsurance savedInsurance = healthInsuranceRepository.save(insurance);
        return HealthInsuranceResponse.fromEntity(savedInsurance, LocalDate.now());
    }

    @Override
    @Transactional
    public HealthInsuranceResponse updateHealthInsurance(Long id, HealthInsuranceRequest request, String currentUsername) {
        validateRequestNotNull(request);
        User currentUser = resolveCurrentUser(currentUsername);

        HealthInsurance insurance = healthInsuranceRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thẻ BHYT với mã ID: " + id));

        validateStudentAccess(currentUser, insurance.getStudent().getId());
        validateInsuranceDates(request.getEffectiveDate(), request.getExpirationDate());

        String cleanCardNumber = ValidationUtils.requireValidBhytCardNumber(request.getCardNumber());
        if (healthInsuranceRepository.existsByCardNumberIgnoreCaseAndIdNot(cleanCardNumber, id)) {
            throw new DuplicateResourceException("Mã thẻ BHYT '" + cleanCardNumber + "' đã tồn tại trên hệ thống.");
        }

        String cleanHospital = ValidationUtils.requireBoundedText(
                request.getInitialHospital(), 150, "Nơi đăng ký khám chữa bệnh ban đầu"
        );

        insurance.setCardNumber(cleanCardNumber);
        insurance.setEffectiveDate(request.getEffectiveDate());
        insurance.setExpirationDate(request.getExpirationDate());
        insurance.setInitialHospital(cleanHospital);

        HealthInsurance updatedInsurance = healthInsuranceRepository.save(insurance);
        return HealthInsuranceResponse.fromEntity(updatedInsurance, LocalDate.now());
    }

    @Override
    @Transactional
    public void deleteHealthInsurance(Long id, String currentUsername) {
        HealthInsurance insurance = healthInsuranceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thẻ BHYT với mã ID: " + id));

        healthInsuranceRepository.delete(insurance);
    }

    private User resolveCurrentUser(String currentUsername) {
        if (currentUsername == null || currentUsername.isBlank()) {
            throw new AccessDeniedException("Yêu cầu đăng nhập để thực hiện thao tác này");
        }
        return userRepository.findByUsername(currentUsername.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + currentUsername));
    }

    private Long resolveEffectiveStudentId(User currentUser, Long requestedStudentId) {
        if (currentUser.getRole() == Role.SINH_VIEN) {
            if (requestedStudentId != null && !requestedStudentId.equals(currentUser.getId())) {
                throw new AccessDeniedException("Sinh viên chỉ được phép tạo thẻ BHYT cho chính mình");
            }
            return currentUser.getId();
        }

        if (requestedStudentId == null) {
            throw new BadRequestException("Vui lòng cung cấp mã ID sinh viên (studentId)");
        }
        return requestedStudentId;
    }

    private void validateStudentAccess(User currentUser, Long targetStudentId) {
        if (currentUser.getRole() == Role.SINH_VIEN && !currentUser.getId().equals(targetStudentId)) {
            throw new AccessDeniedException("Sinh viên chỉ được phép xem thông tin thẻ BHYT của chính mình");
        }
    }

    private Student resolveStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseGet(() -> {
                    User user = userRepository.findById(studentId)
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với mã ID: " + studentId));
                    if (user.getRole() != Role.SINH_VIEN) {
                        throw new BadRequestException("Người dùng (ID: " + studentId + ") không phải là sinh viên");
                    }
                    Student newStudent = Student.builder()
                            .id(user.getId())
                            .user(user)
                            .studentCode(user.getUsername())
                            .build();
                    return studentRepository.save(newStudent);
                });
    }

    private void validateInsuranceDates(LocalDate effectiveDate, LocalDate expirationDate) {
        if (effectiveDate == null) {
            throw new BadRequestException("Ngày bắt đầu hiệu lực không được để trống");
        }
        if (expirationDate == null) {
            throw new BadRequestException("Ngày hết hạn không được để trống");
        }
        if (effectiveDate.isAfter(expirationDate)) {
            throw new BadRequestException("Ngày bắt đầu hiệu lực phải trước hoặc bằng ngày hết hạn");
        }
    }

    private void validateRequestNotNull(HealthInsuranceRequest request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu thẻ BHYT không được để trống");
        }
    }
}

