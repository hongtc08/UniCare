package com.unicare.application.service.impl;

import com.unicare.application.dto.request.CancelAppointmentRequest;
import com.unicare.application.dto.request.RescheduleAppointmentRequest;
import com.unicare.application.dto.response.AppointmentResponse;
import com.unicare.application.service.AppointmentService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.Appointment;
import com.unicare.domain.entity.Shift;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;
import com.unicare.domain.repository.AppointmentRepository;
import com.unicare.domain.repository.ShiftRepository;
import com.unicare.domain.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Dùng chung cho STUDENT và DOCTOR
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_CANCELED = "CANCELED";
    private static final String ACTOR_STUDENT = "STUDENT";
    private static final String ACTOR_DOCTOR = "DOCTOR";

    private final AppointmentRepository appointmentRepository;
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    public AppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            ShiftRepository shiftRepository,
            UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(Long appointmentId, CancelAppointmentRequest request,
            String currentUsername) {
        if (request == null || request.getReason() == null || request.getReason().trim().isBlank()) {
            throw new BadRequestException("Lý do hủy lịch hẹn không được để trống");
        }

        User currentUser = resolveCurrentUser(currentUsername);
        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với mã ID: " + appointmentId));

        String actor = resolveActorAndValidateAccess(currentUser, appointment, "hủy");

        // Kiểm tra trạng thái cuộc hẹn
        if (!STATUS_CONFIRMED.equalsIgnoreCase(appointment.getStatus())) {
            throw new BadRequestException(
                    "Chỉ có thể hủy lịch hẹn đang ở trạng thái xác nhận (CONFIRMED). Trạng thái hiện tại: "
                            + appointment.getStatus());
        }

        // Lấy thông tin ca khám có lock để hoàn trả slot
        Shift shift = shiftRepository.findByIdForUpdate(appointment.getShift().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca khám của lịch hẹn"));

        validateShiftNotEndedOrPast(shift, "hủy");

        // Hoàn trả slot cho ca khám cũ: giảm booked_slots, mở lại ca nếu đang FULL
        int currentBooked = shift.getBookedSlots() != null ? shift.getBookedSlots() : 0;
        int updatedBooked = Math.max(0, currentBooked - 1);
        shift.setBookedSlots(updatedBooked);

        if (shift.getStatus() == ShiftStatus.FULL && updatedBooked < shift.getMaxSlots()) {
            shift.setStatus(ShiftStatus.OPEN);
        }
        shiftRepository.save(shift);

        // Cập nhật trạng thái lịch hẹn
        appointment.setStatus(STATUS_CANCELED);
        appointment.setCancelRescheduleReason(request.getReason().trim());
        appointment.setCanceledBy(actor);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(savedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse rescheduleAppointment(Long appointmentId, RescheduleAppointmentRequest request,
            String currentUsername) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu yêu cầu dời lịch không hợp lệ");
        }
        if (request.getNewShiftId() == null) {
            throw new BadRequestException("Vui lòng chọn ca khám mới (newShiftId)");
        }
        if (request.getReason() == null || request.getReason().trim().isBlank()) {
            throw new BadRequestException("Lý do dời lịch hẹn không được để trống");
        }

        User currentUser = resolveCurrentUser(currentUsername);
        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với mã ID: " + appointmentId));

        String actor = resolveActorAndValidateAccess(currentUser, appointment, "dời");

        // Kiểm tra trạng thái cuộc hẹn cũ
        if (!STATUS_CONFIRMED.equalsIgnoreCase(appointment.getStatus())) {
            throw new BadRequestException(
                    "Chỉ có thể dời lịch hẹn đang ở trạng thái xác nhận (CONFIRMED). Trạng thái hiện tại: "
                            + appointment.getStatus());
        }

        Long oldShiftId = appointment.getShift().getId();
        if (oldShiftId.equals(request.getNewShiftId())) {
            throw new BadRequestException("Ca khám mới trùng với ca khám hiện tại của lịch hẹn");
        }

        // Khóa ca khám cũ để hoàn trả slot
        Shift oldShift = shiftRepository.findByIdForUpdate(oldShiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ca khám hiện tại của lịch hẹn"));
        validateShiftNotEndedOrPast(oldShift, "dời");

        // Khóa ca khám mới để kiểm tra và trừ slot
        Shift newShift = shiftRepository.findByIdForUpdate(request.getNewShiftId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy ca khám mới với mã ID: " + request.getNewShiftId()));

        validateShiftNotEndedOrPast(newShift, "đăng ký dời sang");

        // Kiểm tra loại hình dịch vụ của ca khám phải tương thích
        if (oldShift.getShiftType() != newShift.getShiftType()) {
            throw new BadRequestException("Không thể dời sang ca khám khác loại hình dịch vụ ("
                    + oldShift.getShiftType() + " != " + newShift.getShiftType() + ")");
        }

        // Kiểm tra ca khám mới còn chỗ hay không
        int newShiftBooked = newShift.getBookedSlots() != null ? newShift.getBookedSlots() : 0;
        if (newShift.getStatus() == ShiftStatus.FULL || newShiftBooked >= newShift.getMaxSlots()) {
            throw new BadRequestException("Ca khám mới đã đầy chỗ (hết slot khả dụng)");
        }

        // Kiểm tra ràng buộc sinh viên đã có lịch CONFIRMED trong ca mới hay chưa
        Long studentId = appointment.getStudent().getId();
        boolean alreadyBookedInNewShift = appointmentRepository.existsByStudentIdAndShiftIdAndStatus(studentId,
                newShift.getId(), STATUS_CONFIRMED);
        if (alreadyBookedInNewShift) {
            throw new DuplicateResourceException("Sinh viên đã có một lịch hẹn đã xác nhận trong ca khám mới này");
        }

        // 1. Hoàn trả slot ca cũ
        int oldShiftBooked = oldShift.getBookedSlots() != null ? oldShift.getBookedSlots() : 0;
        int updatedOldBooked = Math.max(0, oldShiftBooked - 1);
        oldShift.setBookedSlots(updatedOldBooked);
        if (oldShift.getStatus() == ShiftStatus.FULL && updatedOldBooked < oldShift.getMaxSlots()) {
            oldShift.setStatus(ShiftStatus.OPEN);
        }
        shiftRepository.save(oldShift);

        // 2. Trừ slot ca mới
        int updatedNewBooked = newShiftBooked + 1;
        newShift.setBookedSlots(updatedNewBooked);
        if (updatedNewBooked >= newShift.getMaxSlots()) {
            newShift.setStatus(ShiftStatus.FULL);
        }
        shiftRepository.save(newShift);

        // 3. Cập nhật lịch hẹn sang ca mới
        appointment.setShift(newShift);
        appointment.setCancelRescheduleReason(request.getReason().trim());
        appointment.setCanceledBy(actor);
        appointment.setIsReminded(false);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(savedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getStudentAppointments(String currentUsername, String status) {
        User currentUser = resolveCurrentUser(currentUsername);
        if (currentUser.getRole() != Role.SINH_VIEN) {
            throw new AccessDeniedException("Chỉ tài khoản sinh viên mới có quyền tra cứu danh sách lịch hẹn cá nhân");
        }

        List<Appointment> list;
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            list = appointmentRepository.findByStudentIdAndStatusWithDetails(currentUser.getId(), status.trim());
        } else {
            list = appointmentRepository.findByStudentIdWithDetails(currentUser.getId());
        }

        return list.stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(
            String currentUsername,
            LocalDate date,
            ServiceType serviceType,
            String status) {
        User currentUser = resolveCurrentUser(currentUsername);
        if (currentUser.getRole() != Role.NHAN_VIEN_Y_TE) {
            throw new AccessDeniedException(
                    "Chỉ tài khoản Bác sĩ / Nhân viên y tế mới có quyền tra cứu danh sách lịch hẹn phụ trách");
        }

        String normalizedStatus = (status != null && !status.trim().isEmpty()
                && !"ALL".equalsIgnoreCase(status.trim()))
                        ? status.trim()
                        : null;

        List<Appointment> appointments = appointmentRepository.findByStaffIdWithDetails(currentUser.getId());

        return appointments.stream()
                .filter(a -> date == null || date.equals(a.getShift().getShiftDate()))
                .filter(a -> serviceType == null || serviceType == a.getShift().getShiftType())
                .filter(a -> normalizedStatus == null || normalizedStatus.equalsIgnoreCase(a.getStatus()))
                .map(AppointmentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long appointmentId, String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);
        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với mã ID: " + appointmentId));

        if (currentUser.getRole() == Role.SINH_VIEN && !appointment.getStudent().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Sinh viên chỉ được phép xem thông tin lịch hẹn của chính mình");
        }

        if (currentUser.getRole() == Role.NHAN_VIEN_Y_TE
                && !appointment.getShift().getStaff().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Bác sĩ chỉ được phép xem thông tin lịch hẹn thuộc ca khám do mình phụ trách");
        }

        return AppointmentResponse.fromEntity(appointment);
    }

    private User resolveCurrentUser(String currentUsername) {
        if (currentUsername == null || currentUsername.isBlank()) {
            throw new AccessDeniedException("Yêu cầu đăng nhập để thực hiện thao tác này");
        }
        return userRepository.findByUsername(currentUsername.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + currentUsername));
    }

    private String resolveActorAndValidateAccess(User currentUser, Appointment appointment, String actionName) {
        if (currentUser.getRole() == Role.SINH_VIEN) {
            if (!appointment.getStudent().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Sinh viên chỉ được phép " + actionName + " lịch hẹn của chính mình");
            }
            return ACTOR_STUDENT;
        }

        if (currentUser.getRole() == Role.NHAN_VIEN_Y_TE) {
            if (!appointment.getShift().getStaff().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException(
                        "Bác sĩ chỉ được phép " + actionName + " lịch hẹn thuộc ca khám do mình phụ trách");
            }
            return ACTOR_DOCTOR;
        }

        if (currentUser.getRole() == Role.BAN_QUAN_LY) {
            return ACTOR_DOCTOR;
        }

        throw new AccessDeniedException("Người dùng không có quyền thực hiện thao tác " + actionName + " lịch hẹn");
    }

    private void validateShiftNotEndedOrPast(Shift shift, String actionName) {
        if (shift.getStatus() == ShiftStatus.ENDED) {
            throw new BadRequestException("Không thể " + actionName + " lịch hẹn của ca khám đã kết thúc");
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (shift.getShiftDate().isBefore(today) ||
                (shift.getShiftDate().isEqual(today) && shift.getEndTime().isBefore(now))) {
            throw new BadRequestException("Không thể " + actionName + " lịch hẹn của ca khám đã diễn ra trong quá khứ");
        }
    }
}
