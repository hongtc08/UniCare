package com.unicare.application.service;

import com.unicare.application.dto.request.CancelAppointmentRequest;
import com.unicare.application.dto.request.RescheduleAppointmentRequest;
import com.unicare.application.dto.response.AppointmentResponse;
import com.unicare.application.service.impl.AppointmentServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.Appointment;
import com.unicare.domain.entity.MedicalStaff;
import com.unicare.domain.entity.Shift;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;
import com.unicare.domain.enums.UserStatus;
import com.unicare.domain.repository.AppointmentRepository;
import com.unicare.domain.repository.ShiftRepository;
import com.unicare.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private User studentUser;
    private User otherStudentUser;
    private User doctorUser;
    private Student student;
    private MedicalStaff medicalStaff;
    private Shift currentShift;
    private Shift targetShift;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        studentUser = User.builder()
                .id(10L)
                .username("sv001")
                .fullName("Nguyễn Văn Sinh Viên")
                .role(Role.SINH_VIEN)
                .status(UserStatus.ACTIVE)
                .build();

        otherStudentUser = User.builder()
                .id(11L)
                .username("sv002")
                .fullName("Trần Thị Khác")
                .role(Role.SINH_VIEN)
                .status(UserStatus.ACTIVE)
                .build();

        doctorUser = User.builder()
                .id(20L)
                .username("bs001")
                .fullName("BS. Lê Y Tế")
                .role(Role.NHAN_VIEN_Y_TE)
                .status(UserStatus.ACTIVE)
                .build();

        student = Student.builder()
                .id(10L)
                .user(studentUser)
                .studentCode("SV20260001")
                .build();

        medicalStaff = MedicalStaff.builder()
                .id(20L)
                .user(doctorUser)
                .staffCode("NVYT001")
                .title("Bác sĩ")
                .specialty("Nội tổng quát")
                .build();

        LocalDate futureDate = LocalDate.now().plusDays(2);
        currentShift = Shift.builder()
                .id(101L)
                .staff(medicalStaff)
                .shiftDate(futureDate)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .shiftType(ServiceType.KHAM_DA_KHOA)
                .room("P101")
                .maxSlots(5)
                .bookedSlots(5)
                .status(ShiftStatus.FULL)
                .build();

        targetShift = Shift.builder()
                .id(102L)
                .staff(medicalStaff)
                .shiftDate(futureDate)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .shiftType(ServiceType.KHAM_DA_KHOA)
                .room("P102")
                .maxSlots(5)
                .bookedSlots(4)
                .status(ShiftStatus.OPEN)
                .build();

        appointment = Appointment.builder()
                .id(1L)
                .appointmentCode("LH2611040001")
                .student(student)
                .shift(currentShift)
                .reasonSymptoms("Đau bụng sốt nhẹ")
                .status("CONFIRMED")
                .createdAt(OffsetDateTime.now())
                .isReminded(false)
                .build();
    }

    // =========================================================================
    // 1. Tests for cancelAppointment
    // =========================================================================

    @Test
    @DisplayName("Hủy lịch hẹn thành công bởi Sinh viên: Ca khám FULL tự động mở lại OPEN")
    void cancelAppointment_Success_ByStudent_ShouldReopenShift() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));
        when(shiftRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(currentShift));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        CancelAppointmentRequest request = new CancelAppointmentRequest("Em có lịch thi đột xuất");
        AppointmentResponse response = appointmentService.cancelAppointment(1L, request, "sv001");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("CANCELED");
        assertThat(response.getCanceledBy()).isEqualTo("STUDENT");
        assertThat(response.getCancelRescheduleReason()).isEqualTo("Em có lịch thi đột xuất");

        // Ca khám cũ giảm slot từ 5 xuống 4 và mở lại OPEN
        assertThat(currentShift.getBookedSlots()).isEqualTo(4);
        assertThat(currentShift.getStatus()).isEqualTo(ShiftStatus.OPEN);
        verify(shiftRepository).save(currentShift);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    @DisplayName("Hủy lịch hẹn thành công bởi Bác sĩ: canceledBy là DOCTOR")
    void cancelAppointment_Success_ByDoctor() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));
        when(shiftRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(currentShift));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        CancelAppointmentRequest request = new CancelAppointmentRequest("Bác sĩ có ca phẫu thuật khẩn cấp");
        AppointmentResponse response = appointmentService.cancelAppointment(1L, request, "bs001");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("CANCELED");
        assertThat(response.getCanceledBy()).isEqualTo("DOCTOR");
    }

    @Test
    @DisplayName("Hủy lịch hẹn: Lỗi khi lý do rỗng hoặc request null")
    void cancelAppointment_BadRequest_WhenReasonBlank() {
        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, null, "sv001"))
                .isInstanceOf(BadRequestException.class);

        CancelAppointmentRequest blankReq = new CancelAppointmentRequest("   ");
        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, blankReq, "sv001"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Hủy lịch hẹn: Lỗi khi không tìm thấy lịch hẹn")
    void cancelAppointment_NotFound() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(appointmentRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        CancelAppointmentRequest request = new CancelAppointmentRequest("Lý do");
        assertThatThrownBy(() -> appointmentService.cancelAppointment(999L, request, "sv001"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Hủy lịch hẹn: Sinh viên không được hủy lịch người khác")
    void cancelAppointment_AccessDenied_WhenCancelingOthersAppointment() {
        when(userRepository.findByUsername("sv002")).thenReturn(Optional.of(otherStudentUser));
        when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));

        CancelAppointmentRequest request = new CancelAppointmentRequest("Lý do");
        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, request, "sv002"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Hủy lịch hẹn: Lỗi khi lịch hẹn không ở trạng thái CONFIRMED")
    void cancelAppointment_BadRequest_WhenStatusNotConfirmed() {
        appointment.setStatus("CANCELED");
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));

        CancelAppointmentRequest request = new CancelAppointmentRequest("Lý do");
        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, request, "sv001"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Hủy lịch hẹn: Lỗi khi ca khám đã kết thúc hoặc trong quá khứ")
    void cancelAppointment_BadRequest_WhenShiftEndedOrPast() {
        currentShift.setStatus(ShiftStatus.ENDED);
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));
        when(shiftRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(currentShift));

        CancelAppointmentRequest request = new CancelAppointmentRequest("Lý do");
        assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, request, "sv001"))
                .isInstanceOf(BadRequestException.class);
    }

    // =========================================================================
    // 2. Tests for rescheduleAppointment
    // =========================================================================

    @Test
    @DisplayName("Dời lịch hẹn thành công: hoàn trả ca cũ (FULL -> OPEN) và trừ ca mới (đầy -> FULL)")
    void rescheduleAppointment_Success() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));
        when(shiftRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(currentShift));
        when(shiftRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(targetShift));
        when(appointmentRepository.existsByStudentIdAndShiftIdAndStatus(10L, 102L, "CONFIRMED")).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L, "Dời sang buổi chiều cho tiện việc học");
        AppointmentResponse response = appointmentService.rescheduleAppointment(1L, request, "sv001");

        assertThat(response).isNotNull();
        assertThat(response.getShiftId()).isEqualTo(102L);
        assertThat(response.getCanceledBy()).isEqualTo("STUDENT");

        // Ca cũ: 5 -> 4 (FULL -> OPEN)
        assertThat(currentShift.getBookedSlots()).isEqualTo(4);
        assertThat(currentShift.getStatus()).isEqualTo(ShiftStatus.OPEN);

        // Ca mới: 4 -> 5 (OPEN -> FULL)
        assertThat(targetShift.getBookedSlots()).isEqualTo(5);
        assertThat(targetShift.getStatus()).isEqualTo(ShiftStatus.FULL);

        verify(shiftRepository).save(currentShift);
        verify(shiftRepository).save(targetShift);
    }

    @Test
    @DisplayName("Dời lịch hẹn thành công bởi Bác sĩ: ca mới chưa đầy vẫn giữ trạng thái OPEN")
    void rescheduleAppointment_Success_ByDoctor() {
        targetShift.setBookedSlots(2);
        targetShift.setMaxSlots(5);

        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));
        when(shiftRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(currentShift));
        when(shiftRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(targetShift));
        when(appointmentRepository.existsByStudentIdAndShiftIdAndStatus(10L, 102L, "CONFIRMED")).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L, "Bác sĩ đổi lịch");
        AppointmentResponse response = appointmentService.rescheduleAppointment(1L, request, "bs001");

        assertThat(response.getCanceledBy()).isEqualTo("DOCTOR");
        assertThat(targetShift.getBookedSlots()).isEqualTo(3);
        assertThat(targetShift.getStatus()).isEqualTo(ShiftStatus.OPEN);
    }

    @Test
    @DisplayName("Dời lịch hẹn: Lỗi validation khi request null hoặc thiếu ca mới hoặc lý do rỗng")
    void rescheduleAppointment_BadRequest_WhenInvalidRequest() {
        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, null, "sv001"))
                .isInstanceOf(BadRequestException.class);

        RescheduleAppointmentRequest noShiftId = new RescheduleAppointmentRequest(null, "Lý do");
        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, noShiftId, "sv001"))
                .isInstanceOf(BadRequestException.class);

        RescheduleAppointmentRequest blankReason = new RescheduleAppointmentRequest(102L, "  ");
        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, blankReason, "sv001"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Dời lịch hẹn: Lỗi khi chọn ca mới trùng với ca khám hiện tại")
    void rescheduleAppointment_BadRequest_WhenSameShift() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));

        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(101L, "Trùng ca");
        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, request, "sv001"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Dời lịch hẹn: Lỗi khi ca khám mới khác loại hình dịch vụ")
    void rescheduleAppointment_BadRequest_WhenDifferentShiftType() {
        targetShift.setShiftType(ServiceType.TU_VAN_TAM_LY);

        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));
        when(shiftRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(currentShift));
        when(shiftRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(targetShift));

        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L, "Đổi loại khám");
        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, request, "sv001"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Dời lịch hẹn: Lỗi khi ca khám mới đã đầy chỗ hoặc đã kết thúc")
    void rescheduleAppointment_BadRequest_WhenNewShiftFullOrEnded() {
        targetShift.setStatus(ShiftStatus.FULL);
        targetShift.setBookedSlots(5);

        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));
        when(shiftRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(currentShift));
        when(shiftRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(targetShift));

        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L, "Dời ca đầy");
        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, request, "sv001"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Dời lịch hẹn: Lỗi khi sinh viên đã có lịch CONFIRMED trong ca mới")
    void rescheduleAppointment_Duplicate_WhenAlreadyBookedInTargetShift() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));
        when(shiftRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(currentShift));
        when(shiftRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(targetShift));
        when(appointmentRepository.existsByStudentIdAndShiftIdAndStatus(10L, 102L, "CONFIRMED")).thenReturn(true);

        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L, "Trùng ca mới");
        assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, request, "sv001"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    // =========================================================================
    // 3. Tests for getStudentAppointments & getAppointmentById
    // =========================================================================

    @Test
    @DisplayName("Lấy danh sách lịch hẹn của sinh viên: hỗ trợ cả có lọc và không lọc status")
    void getStudentAppointments_Success() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(appointmentRepository.findByStudentIdWithDetails(10L)).thenReturn(List.of(appointment));
        when(appointmentRepository.findByStudentIdAndStatusWithDetails(10L, "CONFIRMED")).thenReturn(List.of(appointment));

        List<AppointmentResponse> allList = appointmentService.getStudentAppointments("sv001", null);
        assertThat(allList).hasSize(1);

        List<AppointmentResponse> filteredList = appointmentService.getStudentAppointments("sv001", "CONFIRMED");
        assertThat(filteredList).hasSize(1);
    }

    @Test
    @DisplayName("Lấy danh sách lịch hẹn: Chặn truy cập khi người dùng không phải sinh viên")
    void getStudentAppointments_AccessDenied_WhenNotStudent() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));

        assertThatThrownBy(() -> appointmentService.getStudentAppointments("bs001", null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Lấy chi tiết lịch hẹn: Thành công cho chính chủ và chặn người khác")
    void getAppointmentById_Success_And_Errors() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));

        AppointmentResponse response = appointmentService.getAppointmentById(1L, "sv001");
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);

        // Sinh viên khác xem -> AccessDeniedException
        when(userRepository.findByUsername("sv002")).thenReturn(Optional.of(otherStudentUser));
        assertThatThrownBy(() -> appointmentService.getAppointmentById(1L, "sv002"))
                .isInstanceOf(AccessDeniedException.class);
    }
}
