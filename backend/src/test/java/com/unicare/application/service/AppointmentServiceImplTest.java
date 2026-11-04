package com.unicare.application.service;

import com.unicare.application.dto.request.*;
import com.unicare.application.dto.response.AppointmentResponse;
import com.unicare.application.service.impl.AppointmentServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.*;
import com.unicare.domain.enums.*;
import com.unicare.domain.repository.*;
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
        private Student otherStudent;
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

                otherStudent = Student.builder()
                                .id(11L)
                                .user(otherStudentUser)
                                .studentCode("SV20260002")
                                .build();

                medicalStaff = MedicalStaff.builder()
                                .id(20L)
                                .user(doctorUser)
                                .staffCode("NVYT001")
                                .title("Bác sĩ chuyên khoa")
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
                assertThat(response.getCancelRescheduleReason()).isEqualTo("Bác sĩ có ca phẫu thuật khẩn cấp");
        }

        @Test
        @DisplayName("Hủy lịch hẹn thất bại khi không tìm thấy lịch hẹn")
        void cancelAppointment_NotFound() {
                when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
                when(appointmentRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

                CancelAppointmentRequest request = new CancelAppointmentRequest("Lý do");
                assertThatThrownBy(() -> appointmentService.cancelAppointment(999L, request, "sv001"))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Không tìm thấy lịch hẹn");
        }

        @Test
        @DisplayName("Hủy lịch hẹn thất bại khi sinh viên cố tình hủy lịch của sinh viên khác")
        void cancelAppointment_AccessDenied_WhenCancelingOthersAppointment() {
                when(userRepository.findByUsername("sv002")).thenReturn(Optional.of(otherStudentUser));
                when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));

                CancelAppointmentRequest request = new CancelAppointmentRequest("Lý do hủy");
                assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, request, "sv002"))
                                .isInstanceOf(AccessDeniedException.class)
                                .hasMessageContaining("Sinh viên chỉ được phép hủy lịch hẹn của chính mình");
        }

        @Test
        @DisplayName("Hủy lịch hẹn thất bại khi lịch hẹn không ở trạng thái CONFIRMED")
        void cancelAppointment_BadRequest_WhenStatusNotConfirmed() {
                appointment.setStatus("CANCELED");
                when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
                when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));

                CancelAppointmentRequest request = new CancelAppointmentRequest("Muốn hủy tiếp");
                assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, request, "sv001"))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Chỉ có thể hủy lịch hẹn đang ở trạng thái xác nhận");
        }

        @Test
        @DisplayName("Hủy lịch hẹn thất bại khi ca khám đã kết thúc (ENDED)")
        void cancelAppointment_BadRequest_WhenShiftEnded() {
                currentShift.setStatus(ShiftStatus.ENDED);
                when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
                when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));
                when(shiftRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(currentShift));

                CancelAppointmentRequest request = new CancelAppointmentRequest("Hủy ca đã đóng");
                assertThatThrownBy(() -> appointmentService.cancelAppointment(1L, request, "sv001"))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Không thể hủy lịch hẹn của ca khám đã kết thúc");
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
                when(appointmentRepository.existsByStudentIdAndShiftIdAndStatus(10L, 102L, "CONFIRMED"))
                                .thenReturn(false);
                when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArgument(0));

                RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L,
                                "Dời sang buổi chiều cho tiện việc học");
                AppointmentResponse response = appointmentService.rescheduleAppointment(1L, request, "sv001");

                assertThat(response).isNotNull();
                assertThat(response.getShiftId()).isEqualTo(102L);
                assertThat(response.getCanceledBy()).isEqualTo("STUDENT");
                assertThat(response.getCancelRescheduleReason()).isEqualTo("Dời sang buổi chiều cho tiện việc học");

                // Ca cũ được hoàn trả slot: 5 -> 4 và mở lại OPEN
                assertThat(currentShift.getBookedSlots()).isEqualTo(4);
                assertThat(currentShift.getStatus()).isEqualTo(ShiftStatus.OPEN);

                // Ca mới nhận thêm slot: 4 -> 5 và đạt tối đa nên chuyển sang FULL
                assertThat(targetShift.getBookedSlots()).isEqualTo(5);
                assertThat(targetShift.getStatus()).isEqualTo(ShiftStatus.FULL);

                verify(shiftRepository).save(currentShift);
                verify(shiftRepository).save(targetShift);
                verify(appointmentRepository).save(appointment);
        }

        @Test
        @DisplayName("Dời lịch thất bại khi chọn ca mới trùng ca hiện tại")
        void rescheduleAppointment_SameShift() {
                when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
                when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));

                RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(101L, "Trùng ca");
                assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, request, "sv001"))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Ca khám mới trùng với ca khám hiện tại");
        }

        @Test
        @DisplayName("Dời lịch thất bại khi ca khám mới khác loại hình dịch vụ")
        void rescheduleAppointment_DifferentShiftType() {
                targetShift.setShiftType(ServiceType.TU_VAN_TAM_LY);

                when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
                when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));
                when(shiftRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(currentShift));
                when(shiftRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(targetShift));

                RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L, "Muốn đổi loại khám");
                assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, request, "sv001"))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Không thể dời sang ca khám khác loại hình dịch vụ");
        }

        @Test
        @DisplayName("Dời lịch thất bại khi ca khám mới đã đầy slot")
        void rescheduleAppointment_TargetShiftFull() {
                targetShift.setStatus(ShiftStatus.FULL);
                targetShift.setBookedSlots(5);

                when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
                when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));
                when(shiftRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(currentShift));
                when(shiftRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(targetShift));

                RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L, "Dời sang ca đã đầy");
                assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, request, "sv001"))
                                .isInstanceOf(BadRequestException.class)
                                .hasMessageContaining("Ca khám mới đã đầy chỗ");
        }

        @Test
        @DisplayName("Dời lịch thất bại khi sinh viên đã có lịch CONFIRMED ở ca mới")
        void rescheduleAppointment_AlreadyBookedInTargetShift() {
                when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
                when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));
                when(shiftRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(currentShift));
                when(shiftRepository.findByIdForUpdate(102L)).thenReturn(Optional.of(targetShift));
                when(appointmentRepository.existsByStudentIdAndShiftIdAndStatus(10L, 102L, "CONFIRMED"))
                                .thenReturn(true);

                RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L, "Trùng lịch ca mới");
                assertThatThrownBy(() -> appointmentService.rescheduleAppointment(1L, request, "sv001"))
                                .isInstanceOf(DuplicateResourceException.class)
                                .hasMessageContaining("Sinh viên đã có một lịch hẹn đã xác nhận trong ca khám mới");
        }

        // =========================================================================
        // 3. Tests for getStudentAppointments & getAppointmentById
        // =========================================================================

        @Test
        @DisplayName("Lấy danh sách lịch hẹn của sinh viên thành công")
        void getStudentAppointments_Success() {
                when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
                when(appointmentRepository.findByStudentIdWithDetails(10L)).thenReturn(List.of(appointment));

                List<AppointmentResponse> list = appointmentService.getStudentAppointments("sv001", null);

                assertThat(list).hasSize(1);
                assertThat(list.get(0).getAppointmentCode()).isEqualTo("LH2611040001");
                assertThat(list.get(0).getStudentCode()).isEqualTo("SV20260001");
        }

        @Test
        @DisplayName("Lấy chi tiết lịch hẹn theo ID thành công")
        void getAppointmentById_Success() {
                when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
                when(appointmentRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(appointment));

                AppointmentResponse response = appointmentService.getAppointmentById(1L, "sv001");

                assertThat(response).isNotNull();
                assertThat(response.getId()).isEqualTo(1L);
                assertThat(response.getAppointmentCode()).isEqualTo("LH2611040001");
        }
}
