/**
 * Lớp cài đặt nghiệp vụ Thiết lập lịch trực & Mở ca khám tuần (ShiftService).
 */
package com.unicare.application.service.impl;

import com.unicare.application.dto.request.ShiftRequest;
import com.unicare.application.dto.response.DoctorOptionResponse;
import com.unicare.application.dto.response.ShiftResponse;
import com.unicare.application.service.ShiftService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.Shift;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;
import com.unicare.domain.enums.UserStatus;
import com.unicare.domain.repository.ShiftRepository;
import com.unicare.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    public ShiftServiceImpl(ShiftRepository shiftRepository, UserRepository userRepository) {
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> getShifts(
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate,
            Long doctorId,
            ServiceType serviceType,
            ShiftStatus status,
            String room
    ) {
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new BadRequestException("Ngày kết thúc tìm kiếm không được nhỏ hơn ngày bắt đầu.");
        }

        String cleanRoom = (room != null && !room.trim().isEmpty() && !"ALL".equalsIgnoreCase(room.trim()))
                ? room.trim().toLowerCase(Locale.ROOT)
                : null;

        List<Shift> shifts = shiftRepository.findAllWithDoctor();
        return shifts.stream()
                .filter(s -> date == null || date.equals(s.getShiftDate()))
                .filter(s -> fromDate == null || !s.getShiftDate().isBefore(fromDate))
                .filter(s -> toDate == null || !s.getShiftDate().isAfter(toDate))
                .filter(s -> doctorId == null || (s.getDoctor() != null && doctorId.equals(s.getDoctor().getId())))
                .filter(s -> serviceType == null || serviceType == s.getServiceType())
                .filter(s -> status == null || status == s.getStatus())
                .filter(s -> cleanRoom == null || (s.getRoom() != null && s.getRoom().toLowerCase(Locale.ROOT).contains(cleanRoom)))
                .map(ShiftResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorOptionResponse> getAvailableDoctors() {
        List<User> doctors = userRepository.searchUsers("", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        return doctors.stream()
                .map(DoctorOptionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftResponse getShiftById(Long id) {
        Shift shift = shiftRepository.findByIdWithDoctor(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca khám với mã ID: " + id));
        return ShiftResponse.fromEntity(shift);
    }

    @Override
    @Transactional
    public ShiftResponse createShift(ShiftRequest request, String currentUsername) {
        validateTimeRange(request.getStartTime(), request.getEndTime());

        User doctor = resolveAndValidateDoctor(request.getDoctorId(), currentUsername, null);

        boolean overlapping = shiftRepository.existsOverlappingShift(
                doctor.getId(),
                request.getShiftDate(),
                request.getStartTime(),
                request.getEndTime()
        );
        if (overlapping) {
            throw new DuplicateResourceException(
                    "Bác sĩ " + doctor.getFullName() + " đã có ca khám trùng khung giờ trong ngày " + request.getShiftDate() + "."
            );
        }

        String cleanRoom = (request.getRoom() != null && !request.getRoom().trim().isEmpty())
                ? request.getRoom().trim()
                : null;

        if (cleanRoom != null) {
            boolean roomOverlapping = shiftRepository.existsOverlappingRoom(
                    cleanRoom,
                    request.getShiftDate(),
                    request.getStartTime(),
                    request.getEndTime()
            );
            if (roomOverlapping) {
                throw new DuplicateResourceException(
                        "Phòng khám '" + cleanRoom + "' đã có ca khám trùng khung giờ trong ngày " + request.getShiftDate() + "."
                );
            }
        }

        Shift shift = new Shift(
                doctor,
                request.getShiftDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getServiceType(),
                cleanRoom,
                request.getMaxQuantity()
        );

        Shift savedShift = shiftRepository.save(shift);
        return ShiftResponse.fromEntity(savedShift);
    }

    @Override
    @Transactional
    public ShiftResponse updateShift(Long id, ShiftRequest request, String currentUsername) {
        Shift shift = shiftRepository.findByIdWithDoctor(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca khám với mã ID: " + id));

        validateTimeRange(request.getStartTime(), request.getEndTime());

        User doctor = resolveAndValidateDoctor(request.getDoctorId(), currentUsername, shift.getDoctor());

        int bookedQty = shift.getBookedQuantity() != null ? shift.getBookedQuantity() : 0;

        // Ràng buộc 1: Không cho phép giảm số lượng tối đa thấp hơn số lịch đã đặt
        if (request.getMaxQuantity() < bookedQty) {
            throw new BadRequestException(
                    "Không thể giảm số lượng tối đa (" + request.getMaxQuantity() +
                    ") thấp hơn số lịch hẹn đã đặt (" + bookedQty + ")."
            );
        }

        // Ràng buộc 2: Nếu ca đã có lịch hẹn thì không cho phép chuyển sang Đã hủy trước khi hủy/dời lịch
        if (request.getStatus() == ShiftStatus.DA_HUY && bookedQty > 0) {
            throw new BadRequestException(
                    "Ca khám đã có " + bookedQty + " lịch hẹn. Vui lòng hủy hoặc dời tất cả lịch hẹn trước khi hủy ca khám."
            );
        }

        ShiftStatus nextStatus;
        if (request.getStatus() == ShiftStatus.DA_HUY) {
            nextStatus = ShiftStatus.DA_HUY;
        } else {
            nextStatus = bookedQty >= request.getMaxQuantity() ? ShiftStatus.DA_DAY : ShiftStatus.CON_TRONG;
        }

        String cleanRoom = (request.getRoom() != null && !request.getRoom().trim().isEmpty())
                ? request.getRoom().trim()
                : null;

        // Kiểm tra trùng khung giờ của Bác sĩ và Phòng khám trong ngày (bỏ qua nếu ca đang được hủy)
        if (nextStatus != ShiftStatus.DA_HUY) {
            boolean overlapping = shiftRepository.existsOverlappingShiftExcludingId(
                    id,
                    doctor.getId(),
                    request.getShiftDate(),
                    request.getStartTime(),
                    request.getEndTime()
            );
            if (overlapping) {
                throw new DuplicateResourceException(
                        "Bác sĩ " + doctor.getFullName() + " đã có ca khám trùng khung giờ trong ngày " + request.getShiftDate() + "."
                );
            }

            if (cleanRoom != null) {
                boolean roomOverlapping = shiftRepository.existsOverlappingRoomExcludingId(
                        id,
                        cleanRoom,
                        request.getShiftDate(),
                        request.getStartTime(),
                        request.getEndTime()
                );
                if (roomOverlapping) {
                    throw new DuplicateResourceException(
                            "Phòng khám '" + cleanRoom + "' đã có ca khám trùng khung giờ trong ngày " + request.getShiftDate() + "."
                    );
                }
            }
        }

        shift.setDoctor(doctor);
        shift.setShiftDate(request.getShiftDate());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setServiceType(request.getServiceType());
        shift.setRoom(cleanRoom);
        shift.setMaxQuantity(request.getMaxQuantity());
        shift.setStatus(nextStatus);

        Shift updatedShift = shiftRepository.save(shift);
        return ShiftResponse.fromEntity(updatedShift);
    }

    @Override
    @Transactional
    public void deleteShift(Long id) {
        Shift shift = shiftRepository.findByIdWithDoctor(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca khám với mã ID: " + id));

        int bookedQty = shift.getBookedQuantity() != null ? shift.getBookedQuantity() : 0;
        if (bookedQty > 0) {
            throw new BadRequestException(
                    "Ca khám đã có " + bookedQty + " lịch hẹn. Vui lòng hủy hoặc dời tất cả lịch hẹn trước khi xóa ca khám."
            );
        }

        shiftRepository.delete(shift);
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new BadRequestException("Giờ kết thúc phải lớn hơn giờ bắt đầu.");
        }
    }

    private User resolveAndValidateDoctor(Long requestedDoctorId, String currentUsername, User fallbackDoctor) {
        User doctor;
        if (requestedDoctorId != null) {
            doctor = userRepository.findById(requestedDoctorId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy bác sĩ / nhân viên y tế với mã ID: " + requestedDoctorId
                    ));
        } else if (fallbackDoctor != null) {
            doctor = fallbackDoctor;
        } else if (currentUsername != null && !currentUsername.isBlank()) {
            User currentUser = userRepository.findByUsername(currentUsername.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản đang đăng nhập."));
            if (currentUser.getRole() != Role.NHAN_VIEN_Y_TE) {
                throw new BadRequestException("Bác sĩ phụ trách không được để trống.");
            }
            doctor = currentUser;
        } else {
            throw new BadRequestException("Bác sĩ phụ trách không được để trống.");
        }

        if (doctor.getRole() != Role.NHAN_VIEN_Y_TE) {
            throw new BadRequestException("Người dùng được chọn không phải là Bác sĩ / Nhân viên y tế.");
        }
        if (doctor.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Tài khoản Bác sĩ / Nhân viên y tế đang bị khóa, không thể phân công ca khám.");
        }
        return doctor;
    }
}
