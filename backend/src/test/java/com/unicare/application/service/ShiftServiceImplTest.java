package com.unicare.application.service;

import com.unicare.application.dto.request.ShiftRequest;
import com.unicare.application.dto.response.DoctorOptionResponse;
import com.unicare.application.dto.response.ShiftResponse;
import com.unicare.application.service.impl.ShiftServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.MedicalStaff;
import com.unicare.domain.entity.Shift;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;
import com.unicare.domain.enums.UserStatus;
import com.unicare.domain.repository.MedicalStaffRepository;
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

    @Mock
    private MedicalStaffRepository medicalStaffRepository;

    @InjectMocks
    private ShiftServiceImpl shiftService;

    private User sampleDoctor;
    private MedicalStaff sampleStaff;
    private Shift sampleShift;
    private LocalDate sampleDate;

    @BeforeEach
    void setUp() {
        sampleDoctor = new User("bs01", "hashed_pwd", "BS. Nguyễn Văn Y", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        sampleDoctor.setId(10L);

        sampleStaff = MedicalStaff.builder()
                .id(10L)
                .user(sampleDoctor)
                .staffCode("BS0010")
                .title("Bác sĩ")
                .specialty("Đa khoa")
                .build();
        sampleDoctor.setMedicalStaff(sampleStaff);

        sampleDate = LocalDate.of(2026, 10, 28);

        sampleShift = new Shift(
                sampleStaff,
                sampleDate,
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                ServiceType.KHAM_DA_KHOA,
                "Phòng 101",
                10
        );
        sampleShift.setId(100L);
        sampleShift.setBookedSlots(3);
    }

    @Test
    @DisplayName("Lấy danh sách ca khám và lọc theo các tiêu chí (bao gồm room) thành công")
    void getShifts_Success_WithFilters() {
        when(shiftRepository.findAllWithDoctor()).thenReturn(List.of(sampleShift));

        List<ShiftResponse> all = shiftService.getShifts(null, null, null, null, null, null, null);
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getAvailableSlots()).isEqualTo(7);

        List<ShiftResponse> filteredMatch = shiftService.getShifts(
                sampleDate,
                sampleDate.minusDays(1),
                sampleDate.plusDays(1),
                10L,
                ServiceType.KHAM_DA_KHOA,
                ShiftStatus.OPEN,
                "Phòng 101"
        );
        assertThat(filteredMatch).hasSize(1);

        List<ShiftResponse> filteredRoomMismatch = shiftService.getShifts(
                sampleDate,
                null,
                null,
                10L,
                ServiceType.KHAM_DA_KHOA,
                null,
                "Phòng 999"
        );
        assertThat(filteredRoomMismatch).isEmpty();

        List<ShiftResponse> filteredRoomAll = shiftService.getShifts(
                sampleDate,
                null,
                null,
                10L,
                ServiceType.KHAM_DA_KHOA,
                null,
                "ALL"
        );
        assertThat(filteredRoomAll).hasSize(1);

        List<ShiftResponse> filteredOtherFilters = shiftService.getShifts(
                sampleDate.plusDays(5),
                null,
                null,
                99L,
                ServiceType.TU_VAN_TAM_LY,
                ShiftStatus.ENDED,
                null
        );
        assertThat(filteredOtherFilters).isEmpty();
    }

    @Test
    @DisplayName("Lấy danh sách ca khám còn chỗ (GET /api/shifts/available) theo ngày & loại dịch vụ thành công")
    void getAvailableShifts_Success() {
        Shift fullShift = new Shift(
                sampleStaff,
                sampleDate,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                ServiceType.KHAM_DA_KHOA,
                "Phòng 101",
                5
        );
        fullShift.setId(101L);
        fullShift.setBookedSlots(5);
        fullShift.setStatus(ShiftStatus.OPEN); // Dù status OPEN nhưng bookedSlots == maxSlots vẫn bị loại

        Shift endedShift = new Shift(
                sampleStaff,
                sampleDate,
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                ServiceType.KHAM_DA_KHOA,
                "Phòng 101",
                5
        );
        endedShift.setId(102L);
        endedShift.setBookedSlots(1);
        endedShift.setStatus(ShiftStatus.ENDED);

        when(shiftRepository.findAllWithDoctor()).thenReturn(List.of(sampleShift, fullShift, endedShift));

        List<ShiftResponse> available = shiftService.getAvailableShifts(sampleDate, ServiceType.KHAM_DA_KHOA);
        assertThat(available).hasSize(1);
        assertThat(available.get(0).getId()).isEqualTo(100L);
        assertThat(available.get(0).getAvailableSlots()).isEqualTo(7);

        List<ShiftResponse> wrongService = shiftService.getAvailableShifts(sampleDate, ServiceType.TU_VAN_TAM_LY);
        assertThat(wrongService).isEmpty();
    }

    @Test
    @DisplayName("Lấy danh sách bác sĩ đang hoạt động cho Dropdown thành công")
    void getAvailableDoctors_Success() {
        when(userRepository.searchUsers("", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE))
                .thenReturn(List.of(sampleDoctor));

        List<DoctorOptionResponse> doctors = shiftService.getAvailableDoctors();

        assertThat(doctors).hasSize(1);
        assertThat(doctors.get(0).getId()).isEqualTo(10L);
        assertThat(doctors.get(0).getName()).isEqualTo("BS. Nguyễn Văn Y");
        assertThat(doctors.get(0).getFullName()).isEqualTo("BS. Nguyễn Văn Y");
        assertThat(doctors.get(0).getRole()).isEqualTo(Role.NHAN_VIEN_Y_TE);
    }

    @Test
    @DisplayName("Lọc ca khám với ngày kết thúc nhỏ hơn ngày bắt đầu ném lỗi 400")
    void getShifts_InvalidDateRange_ThrowsException() {
        LocalDate from = LocalDate.of(2026, 10, 30);
        LocalDate to = LocalDate.of(2026, 10, 25);

        assertThatThrownBy(() -> shiftService.getShifts(null, from, to, null, null, null, null))
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
    @DisplayName("Tạo ca khám mới thành công với doctorId và room hợp lệ")
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
        when(medicalStaffRepository.findById(10L)).thenReturn(Optional.of(sampleStaff));
        when(shiftRepository.existsOverlappingShift(10L, sampleDate, LocalTime.of(9, 0), LocalTime.of(10, 0)))
                .thenReturn(false);
        when(shiftRepository.existsOverlappingRoom("Phòng Tư vấn 2", sampleDate, LocalTime.of(9, 0), LocalTime.of(10, 0)))
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
        assertThat(response.getBookedSlots()).isZero();
        assertThat(response.getStatus()).isEqualTo(ShiftStatus.OPEN);
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
        when(medicalStaffRepository.findById(10L)).thenReturn(Optional.of(sampleStaff));
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
        when(medicalStaffRepository.findById(10L)).thenReturn(Optional.of(sampleStaff));
        when(shiftRepository.existsOverlappingShift(10L, sampleDate, LocalTime.of(8, 30), LocalTime.of(9, 30)))
                .thenReturn(true);

        assertThatThrownBy(() -> shiftService.createShift(request, "admin01"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã có ca khám trùng khung giờ");

        verify(shiftRepository, never()).save(any());
    }

    @Test
    @DisplayName("Chặn tạo và cập nhật ca khám khi trùng lịch Phòng khám trong cùng khung giờ và ngày (409)")
    void createAndUpdateShift_OverlappingRoom_ThrowsDuplicateResourceException() {
        ShiftRequest createReq = new ShiftRequest(
                10L, sampleDate, LocalTime.of(8, 0), LocalTime.of(11, 0), ServiceType.KHAM_DA_KHOA, "P.101 - Phòng y tế", 10
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleDoctor));
        when(medicalStaffRepository.findById(10L)).thenReturn(Optional.of(sampleStaff));
        when(shiftRepository.existsOverlappingShift(10L, sampleDate, LocalTime.of(8, 0), LocalTime.of(11, 0)))
                .thenReturn(false);
        when(shiftRepository.existsOverlappingRoom("P.101 - Phòng y tế", sampleDate, LocalTime.of(8, 0), LocalTime.of(11, 0)))
                .thenReturn(true);

        assertThatThrownBy(() -> shiftService.createShift(createReq, "admin01"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Phòng khám 'P.101 - Phòng y tế' đã có ca khám trùng khung giờ");

        // Khi cập nhật ca khám sang phòng đang bận
        when(shiftRepository.findByIdWithDoctor(100L)).thenReturn(Optional.of(sampleShift));
        when(shiftRepository.existsOverlappingShiftExcludingId(100L, 10L, sampleDate, LocalTime.of(8, 0), LocalTime.of(11, 0)))
                .thenReturn(false);
        when(shiftRepository.existsOverlappingRoomExcludingId(100L, "P.101 - Phòng y tế", sampleDate, LocalTime.of(8, 0), LocalTime.of(11, 0)))
                .thenReturn(true);

        assertThatThrownBy(() -> shiftService.updateShift(100L, createReq, "admin01"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Phòng khám 'P.101 - Phòng y tế' đã có ca khám trùng khung giờ");
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
    @DisplayName("Cập nhật ca khám thành công và tự động chuyển trạng thái FULL khi đủ chỗ")
    void updateShift_Success_And_AutoStatus() {
        ShiftRequest request = new ShiftRequest(
                null,
                sampleDate,
                LocalTime.of(8, 0),
                LocalTime.of(9, 30),
                ServiceType.KHAM_DA_KHOA,
                "Phòng 102",
                3 // Bằng đúng bookedSlots = 3 -> Tự chuyển sang FULL
        );

        when(shiftRepository.findByIdWithDoctor(100L)).thenReturn(Optional.of(sampleShift));
        when(shiftRepository.existsOverlappingShiftExcludingId(
                100L, 10L, sampleDate, LocalTime.of(8, 0), LocalTime.of(9, 30)
        )).thenReturn(false);
        when(shiftRepository.existsOverlappingRoomExcludingId(
                100L, "Phòng 102", sampleDate, LocalTime.of(8, 0), LocalTime.of(9, 30)
        )).thenReturn(false);
        when(shiftRepository.save(any(Shift.class))).thenAnswer(inv -> inv.getArgument(0));

        ShiftResponse response = shiftService.updateShift(100L, request, "bs01");

        assertThat(response.getStatus()).isEqualTo(ShiftStatus.FULL);
        assertThat(response.getAvailableSlots()).isZero();
        assertThat(response.getRoom()).isEqualTo("Phòng 102");
    }

    @Test
    @DisplayName("Chặn cập nhật giảm số lượng tối đa thấp hơn số lịch hẹn đã đặt")
    void updateShift_MaxQuantityLessThanBooked_ThrowsBadRequest() {
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
        when(medicalStaffRepository.findById(10L)).thenReturn(Optional.of(sampleStaff));

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
        request.setStatus(ShiftStatus.ENDED);

        when(shiftRepository.findByIdWithDoctor(100L)).thenReturn(Optional.of(sampleShift));
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleDoctor));
        when(medicalStaffRepository.findById(10L)).thenReturn(Optional.of(sampleStaff));

        assertThatThrownBy(() -> shiftService.updateShift(100L, request, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Vui lòng hủy hoặc dời tất cả lịch hẹn trước khi hủy ca khám");
    }

    @Test
    @DisplayName("Hủy ca khám thành công khi chưa có lịch hẹn (bookedSlots = 0)")
    void updateShift_CancelWithNoBookings_Success() {
        sampleShift.setBookedSlots(0);
        ShiftRequest request = new ShiftRequest(
                10L, sampleDate, LocalTime.of(8, 0), LocalTime.of(9, 0), ServiceType.KHAM_DA_KHOA, "P1", 10
        );
        request.setStatus(ShiftStatus.ENDED);

        when(shiftRepository.findByIdWithDoctor(100L)).thenReturn(Optional.of(sampleShift));
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleDoctor));
        when(medicalStaffRepository.findById(10L)).thenReturn(Optional.of(sampleStaff));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(inv -> inv.getArgument(0));

        ShiftResponse response = shiftService.updateShift(100L, request, "admin01");

        assertThat(response.getStatus()).isEqualTo(ShiftStatus.ENDED);
    }

    @Test
    @DisplayName("Cập nhật ca khám chặn trùng khung giờ của cùng Bác sĩ trong ngày (409)")
    void updateShift_OverlappingDoctor_ThrowsDuplicateResourceException() {
        ShiftRequest request = new ShiftRequest(
                10L, sampleDate, LocalTime.of(8, 0), LocalTime.of(10, 0), ServiceType.KHAM_DA_KHOA, null, 10
        );

        when(shiftRepository.findByIdWithDoctor(100L)).thenReturn(Optional.of(sampleShift));
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleDoctor));
        when(medicalStaffRepository.findById(10L)).thenReturn(Optional.of(sampleStaff));
        when(shiftRepository.existsOverlappingShiftExcludingId(100L, 10L, sampleDate, LocalTime.of(8, 0), LocalTime.of(10, 0)))
                .thenReturn(true);

        assertThatThrownBy(() -> shiftService.updateShift(100L, request, "admin01"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã có ca khám trùng khung giờ");
    }

    @Test
    @DisplayName("Tự động khởi tạo và lưu MedicalStaff nếu bác sĩ chưa có trong bảng medical_staff")
    void createShift_AutoProvisionMedicalStaff_Success() {
        ShiftRequest request = new ShiftRequest(
                10L, sampleDate, LocalTime.of(10, 0), LocalTime.of(11, 0), ServiceType.KHAM_DA_KHOA, null, 5
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleDoctor));
        when(medicalStaffRepository.findById(10L)).thenReturn(Optional.empty());
        when(medicalStaffRepository.save(any(MedicalStaff.class))).thenAnswer(inv -> inv.getArgument(0));
        when(shiftRepository.existsOverlappingShift(10L, sampleDate, LocalTime.of(10, 0), LocalTime.of(11, 0)))
                .thenReturn(false);
        when(shiftRepository.save(any(Shift.class))).thenAnswer(inv -> inv.getArgument(0));

        ShiftResponse response = shiftService.createShift(request, "admin01");

        assertThat(response).isNotNull();
        verify(medicalStaffRepository).save(any(MedicalStaff.class));
    }

    @Test
    @DisplayName("Chặn tạo ca khi không truyền doctorId và tài khoản đang đăng nhập không phải bác sĩ")
    void createShift_LoggedInUserNotMedicalStaff_ThrowsBadRequest() {
        User adminUser = new User("admin01", "pwd", "Admin", Role.BAN_QUAN_LY, UserStatus.ACTIVE);
        when(userRepository.findByUsername("admin01")).thenReturn(Optional.of(adminUser));

        ShiftRequest request = new ShiftRequest(
                null, sampleDate, LocalTime.of(8, 0), LocalTime.of(9, 0), ServiceType.KHAM_DA_KHOA, null, 10
        );

        assertThatThrownBy(() -> shiftService.createShift(request, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Bác sĩ phụ trách không được để trống");
    }

    @Test
    @DisplayName("Chặn cập nhật ca khi bác sĩ fallback có role sai hoặc bị khóa")
    void updateShift_FallbackDoctorRoleOrStatusInvalid_ThrowsBadRequest() {
        User invalidRoleUser = new User("other", "pwd", "Other", Role.SINH_VIEN, UserStatus.ACTIVE);
        MedicalStaff invalidStaff = MedicalStaff.builder().id(50L).user(invalidRoleUser).build();
        sampleShift.setStaff(invalidStaff);

        ShiftRequest request = new ShiftRequest(
                null, sampleDate, LocalTime.of(8, 0), LocalTime.of(9, 0), ServiceType.KHAM_DA_KHOA, null, 10
        );

        when(shiftRepository.findByIdWithDoctor(100L)).thenReturn(Optional.of(sampleShift));

        assertThatThrownBy(() -> shiftService.updateShift(100L, request, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không phải là Bác sĩ");

        User lockedDoctor = new User("locked", "pwd", "Locked", Role.NHAN_VIEN_Y_TE, UserStatus.LOCKED);
        MedicalStaff lockedStaff = MedicalStaff.builder().id(51L).user(lockedDoctor).build();
        sampleShift.setStaff(lockedStaff);

        assertThatThrownBy(() -> shiftService.updateShift(100L, request, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đang bị khóa");
    }

    @Test
    @DisplayName("Chặn xóa ca khám khi đã có lịch hẹn, cho phép xóa khi chưa có lịch hẹn")
    void deleteShift_ChecksBookedQuantity() {
        when(shiftRepository.findByIdWithDoctor(100L)).thenReturn(Optional.of(sampleShift));

        assertThatThrownBy(() -> shiftService.deleteShift(100L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ca khám đã có 3 lịch hẹn. Vui lòng hủy hoặc dời tất cả lịch hẹn trước khi xóa ca khám");

        verify(shiftRepository, never()).delete(any());

        sampleShift.setBookedSlots(0);
        shiftService.deleteShift(100L);

        verify(shiftRepository).delete(sampleShift);
    }

    @Test
    @DisplayName("Lọc danh sách ca khám bao phủ toàn bộ các điều kiện ngày, bác sĩ, dịch vụ, trạng thái, phòng")
    void getShifts_CompleteFilterBranches() {
        Shift shiftNullRoomAndStaff = new Shift();
        shiftNullRoomAndStaff.setId(102L);
        shiftNullRoomAndStaff.setShiftDate(LocalDate.of(2026, 10, 20));
        shiftNullRoomAndStaff.setStartTime(LocalTime.of(8, 0));
        shiftNullRoomAndStaff.setEndTime(LocalTime.of(9, 0));
        shiftNullRoomAndStaff.setShiftType(ServiceType.TU_VAN_TAM_LY);
        shiftNullRoomAndStaff.setStatus(ShiftStatus.FULL);
        shiftNullRoomAndStaff.setMaxSlots(10);
        shiftNullRoomAndStaff.setBookedSlots(10);

        when(shiftRepository.findAllWithDoctor()).thenReturn(List.of(sampleShift, shiftNullRoomAndStaff));

        // fromDate filter eliminates shiftNullRoomAndStaff (2026-10-20 is before 2026-10-25)
        List<ShiftResponse> resFrom = shiftService.getShifts(null, LocalDate.of(2026, 10, 25), null, null, null, null, null);
        assertThat(resFrom).hasSize(1);
        assertThat(resFrom.get(0).getId()).isEqualTo(100L);

        // toDate filter eliminates sampleShift (2026-10-28 is after 2026-10-22)
        List<ShiftResponse> resTo = shiftService.getShifts(null, null, LocalDate.of(2026, 10, 22), null, null, null, null);
        assertThat(resTo).hasSize(1);
        assertThat(resTo.get(0).getId()).isEqualTo(102L);

        // doctorId filter eliminates shift with null staff
        List<ShiftResponse> resDoctor = shiftService.getShifts(null, null, null, 10L, null, null, null);
        assertThat(resDoctor).hasSize(1);

        // room filter eliminates shift with null room
        List<ShiftResponse> resRoom = shiftService.getShifts(null, null, null, null, null, null, "101");
        assertThat(resRoom).hasSize(1);
    }

    @Test
    @DisplayName("Cập nhật ca khám khi bookedSlots là null và phòng là chuỗi rỗng")
    void updateShift_NullBookedSlotsAndBlankRoom() {
        sampleShift.setBookedSlots(null);
        ShiftRequest request = new ShiftRequest(
                10L, sampleDate, LocalTime.of(8, 0), LocalTime.of(9, 30), ServiceType.KHAM_DA_KHOA, "   ", 10
        );

        when(shiftRepository.findByIdWithDoctor(100L)).thenReturn(Optional.of(sampleShift));
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleDoctor));
        when(medicalStaffRepository.findById(10L)).thenReturn(Optional.of(sampleStaff));
        when(shiftRepository.existsOverlappingShiftExcludingId(100L, 10L, sampleDate, LocalTime.of(8, 0), LocalTime.of(9, 30)))
                .thenReturn(false);
        when(shiftRepository.save(any(Shift.class))).thenAnswer(inv -> inv.getArgument(0));

        ShiftResponse response = shiftService.updateShift(100L, request, "admin01");

        assertThat(response.getRoom()).isNull();
        assertThat(response.getStatus()).isEqualTo(ShiftStatus.OPEN);
    }
}
