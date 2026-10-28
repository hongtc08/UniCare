package com.unicare.application.service;

import com.unicare.application.dto.request.ShiftRequest;
import com.unicare.application.dto.response.ShiftResponse;
import com.unicare.application.service.impl.ShiftServiceImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftServiceImplTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ShiftServiceImpl shiftService;

    private User sampleDoctor;
    private Shift sampleShift;
    private LocalDate sampleDate;

    @BeforeEach
    void setUp() {
        sampleDoctor = new User("bs01", "hashed_pwd", "BS. Nguyễn Văn Y", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        sampleDoctor.setId(10L);

        sampleDate = LocalDate.of(2026, 10, 28);

        sampleShift = new Shift(
                sampleDoctor,
                sampleDate,
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                ServiceType.KHAM_DA_KHOA,
                "Phòng 101",
                10
        );
        sampleShift.setId(100L);
        sampleShift.setBookedQuantity(3);
    }

    @Test
    @DisplayName("Lấy danh sách ca khám và lọc theo các tiêu chí thành công")
    void getShifts_Success_WithFilters() {
        when(shiftRepository.findAllWithDoctor()).thenReturn(List.of(sampleShift));

        List<ShiftResponse> all = shiftService.getShifts(null, null, null, null, null, null);
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getAvailableSlots()).isEqualTo(7);

        List<ShiftResponse> filteredMatch = shiftService.getShifts(
                sampleDate,
                sampleDate.minusDays(1),
                sampleDate.plusDays(1),
                10L,
                ServiceType.KHAM_DA_KHOA,
                ShiftStatus.CON_TRONG
        );
        assertThat(filteredMatch).hasSize(1);

        List<ShiftResponse> filteredMismatch = shiftService.getShifts(
                sampleDate,
                null,
                null,
                10L,
                ServiceType.TU_VAN_TAM_LY,
                null
        );
        assertThat(filteredMismatch).isEmpty();
    }

    @Test
    @DisplayName("Lọc ca khám với ngày kết thúc nhỏ hơn ngày bắt đầu ném lỗi 400")
    void getShifts_InvalidDateRange_ThrowsException() {
        LocalDate from = LocalDate.of(2026, 10, 30);
        LocalDate to = LocalDate.of(2026, 10, 25);

        assertThatThrownBy(() -> shiftService.getShifts(null, from, to, null, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ngày kết thúc tìm kiếm không được nhỏ hơn ngày bắt đầu");
    }

    @Test
    @DisplayName("Xem chi tiết ca khám theo ID thành công và khi không tồn tại báo 404")
    void getShiftById_Success_And_NotFound() {
        when(shiftRepository.findByIdWithDoctor(100L)).thenReturn(Optional.of(sampleShift));
        when(shiftRepository.findByIdWithDoctor(999L)).thenReturn(Optional.empty());

        ShiftResponse response = shiftService.getShiftById(100L);
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getDoctorName()).isEqualTo("BS. Nguyễn Văn Y");

        assertThatThrownBy(() -> shiftService.getShiftById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Tạo ca khám mới thành công với doctorId hợp lệ")
    void createShift_Success() {
        ShiftRequest request = new ShiftRequest(
                10L,
                sampleDate,
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                ServiceType.TU_VAN_TAM_LY,
                "  Phòng Tư vấn 2  ",
                5
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleDoctor));
        when(shiftRepository.existsOverlappingShift(10L, sampleDate, LocalTime.of(9, 0), LocalTime.of(10, 0)))
                .thenReturn(false);
        when(shiftRepository.save(any(Shift.class))).thenAnswer(inv -> {
            Shift s = inv.getArgument(0);
            s.setId(101L);
            return s;
        });

        ShiftResponse response = shiftService.createShift(request, "admin01");

        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getRoom()).isEqualTo("Phòng Tư vấn 2");
        assertThat(response.getServiceType()).isEqualTo(ServiceType.TU_VAN_TAM_LY);
        assertThat(response.getBookedQuantity()).isZero();
        assertThat(response.getStatus()).isEqualTo(ShiftStatus.CON_TRONG);
    }

    @Test
    @DisplayName("Tự động gán bác sĩ đang đăng nhập nếu doctorId để trống")
    void createShift_AutoResolveDoctorFromPrincipal_Success() {
        ShiftRequest request = new ShiftRequest(
                null,
                sampleDate,
                LocalTime.of(14, 0),
                LocalTime.of(15, 30),
                ServiceType.KHAM_DA_KHOA,
                null,
                10
        );

        when(userRepository.findByUsername("bs01")).thenReturn(Optional.of(sampleDoctor));
        when(shiftRepository.existsOverlappingShift(10L, sampleDate, LocalTime.of(14, 0), LocalTime.of(15, 30)))
                .thenReturn(false);
        when(shiftRepository.save(any(Shift.class))).thenAnswer(inv -> inv.getArgument(0));

        ShiftResponse response = shiftService.createShift(request, "bs01");

        assertThat(response.getDoctorId()).isEqualTo(10L);
        assertThat(response.getRoom()).isNull();
    }

    @Test
    @DisplayName("Chặn tạo ca khám khi Giờ kết thúc <= Giờ bắt đầu")
    void createShift_EndTimeNotAfterStartTime_ThrowsBadRequest() {
        ShiftRequest equalTimeReq = new ShiftRequest(
                10L, sampleDate, LocalTime.of(8, 0), LocalTime.of(8, 0), ServiceType.KHAM_DA_KHOA, "P1", 10
        );
        ShiftRequest beforeTimeReq = new ShiftRequest(
                10L, sampleDate, LocalTime.of(9, 0), LocalTime.of(8, 0), ServiceType.KHAM_DA_KHOA, "P1", 10
        );

        assertThatThrownBy(() -> shiftService.createShift(equalTimeReq, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Giờ kết thúc phải lớn hơn giờ bắt đầu");

        assertThatThrownBy(() -> shiftService.createShift(beforeTimeReq, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Giờ kết thúc phải lớn hơn giờ bắt đầu");
    }

    @Test
    @DisplayName("Chặn tạo ca khám khi trùng khung giờ của cùng 1 Bác sĩ trong ngày (409)")
    void createShift_OverlappingShift_ThrowsDuplicateResourceException() {
        ShiftRequest request = new ShiftRequest(
                10L, sampleDate, LocalTime.of(8, 30), LocalTime.of(9, 30), ServiceType.KHAM_DA_KHOA, "P1", 10
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleDoctor));
        when(shiftRepository.existsOverlappingShift(10L, sampleDate, LocalTime.of(8, 30), LocalTime.of(9, 30)))
                .thenReturn(true);

        assertThatThrownBy(() -> shiftService.createShift(request, "admin01"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã có ca khám trùng khung giờ");

        verify(shiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Chặn gán ca khám cho tài khoản không phải Bác sĩ hoặc đang bị khóa")
    void createShift_InvalidOrLockedDoctor_ThrowsBadRequest() {
        User studentUser = new User("sv01", "pwd", "Sinh Viên A", Role.SINH_VIEN, UserStatus.ACTIVE);
        studentUser.setId(20L);

        User lockedDoctor = new User("bs02", "pwd", "BS. Khóa", Role.NHAN_VIEN_Y_TE, UserStatus.LOCKED);
        lockedDoctor.setId(30L);

        ShiftRequest reqStudent = new ShiftRequest(
                20L, sampleDate, LocalTime.of(8, 0), LocalTime.of(9, 0), ServiceType.KHAM_DA_KHOA, "P1", 10
        );
        ShiftRequest reqLocked = new ShiftRequest(
                30L, sampleDate, LocalTime.of(8, 0), LocalTime.of(9, 0), ServiceType.KHAM_DA_KHOA, "P1", 10
        );
        ShiftRequest reqNoDoctorByAdmin = new ShiftRequest(
                null, sampleDate, LocalTime.of(8, 0), LocalTime.of(9, 0), ServiceType.KHAM_DA_KHOA, "P1", 10
        );

        when(userRepository.findById(20L)).thenReturn(Optional.of(studentUser));
        when(userRepository.findById(30L)).thenReturn(Optional.of(lockedDoctor));

        assertThatThrownBy(() -> shiftService.createShift(reqStudent, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không phải là Bác sĩ");

        assertThatThrownBy(() -> shiftService.createShift(reqLocked, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đang bị khóa");

        assertThatThrownBy(() -> shiftService.createShift(reqNoDoctorByAdmin, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Bác sĩ phụ trách không được để trống");
    }

    @Test
    @DisplayName("Cập nhật ca khám thành công và tự động chuyển trạng thái DA_DAY khi đủ chỗ")
    void updateShift_Success_And_AutoStatus() {
        ShiftRequest request = new ShiftRequest(
                null,
                sampleDate,
                LocalTime.of(8, 0),
                LocalTime.of(9, 30),
                ServiceType.KHAM_DA_KHOA,
                "Phòng 102",
                3 // Bằng đúng bookedQuantity = 3 -> Tự chuyển sang DA_DAY
        );

        when(shiftRepository.findByIdWithDoctor(100L)).thenReturn(Optional.of(sampleShift));
        when(shiftRepository.existsOverlappingShiftExcludingId(
                100L, 10L, sampleDate, LocalTime.of(8, 0), LocalTime.of(9, 30)
        )).thenReturn(false);
        when(shiftRepository.save(any(Shift.class))).thenAnswer(inv -> inv.getArgument(0));

        ShiftResponse response = shiftService.updateShift(100L, request, "bs01");

        assertThat(response.getStatus()).isEqualTo(ShiftStatus.DA_DAY);
        assertThat(response.getAvailableSlots()).isZero();
        assertThat(response.getRoom()).isEqualTo("Phòng 102");
    }

    @Test
    @DisplayName("Chặn cập nhật giảm số lượng tối đa thấp hơn số lịch hẹn đã đặt")
    void updateShift_MaxQuantityLessThanBooked_ThrowsBadRequest() {
        // sampleShift đang có bookedQuantity = 3
        ShiftRequest request = new ShiftRequest(
                10L,
                sampleDate,
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                ServiceType.KHAM_DA_KHOA,
                "Phòng 101",
                2 // < 3 -> Lỗi
        );

        when(shiftRepository.findByIdWithDoctor(100L)).thenReturn(Optional.of(sampleShift));
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleDoctor));

        assertThatThrownBy(() -> shiftService.updateShift(100L, request, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Không thể giảm số lượng tối đa (2) thấp hơn số lịch hẹn đã đặt (3)");
    }

    @Test
    @DisplayName("Chặn hủy ca khám khi đã có lịch hẹn được đặt")
    void updateShift_CancelWithBookings_ThrowsBadRequest() {
        ShiftRequest request = new ShiftRequest(
                10L, sampleDate, LocalTime.of(8, 0), LocalTime.of(9, 0), ServiceType.KHAM_DA_KHOA, "P1", 10
        );
        request.setStatus(ShiftStatus.DA_HUY);

        when(shiftRepository.findByIdWithDoctor(100L)).thenReturn(Optional.of(sampleShift));
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleDoctor));

        assertThatThrownBy(() -> shiftService.updateShift(100L, request, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Vui lòng hủy hoặc dời tất cả lịch hẹn trước khi hủy ca khám");
    }

    @Test
    @DisplayName("Chặn xóa ca khám khi đã có lịch hẹn, cho phép xóa khi chưa có lịch hẹn")
    void deleteShift_ChecksBookedQuantity() {
        // 1. Ca khám đã có 3 lịch hẹn -> Chặn xóa
        when(shiftRepository.findByIdWithDoctor(100L)).thenReturn(Optional.of(sampleShift));

        assertThatThrownBy(() -> shiftService.deleteShift(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ca khám đã có 3 lịch hẹn. Vui lòng hủy hoặc dời tất cả lịch hẹn trước khi xóa ca khám");

        verify(shiftRepository, never()).delete(any());

        // 2. Ca khám chưa có lịch hẹn (bookedQuantity = 0) -> Cho phép xóa
        sampleShift.setBookedQuantity(0);
        shiftService.deleteShift(100L);

        verify(shiftRepository).delete(sampleShift);
    }
}
