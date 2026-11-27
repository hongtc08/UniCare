package com.unicare.application.service;

import com.unicare.application.dto.response.Dass21ResultResponse;
import com.unicare.application.dto.response.Dass21ScorePointResponse;
import com.unicare.application.dto.response.MoodLogHistoryResponse;
import com.unicare.application.dto.response.StudentMentalHealthHistoryResponse;
import com.unicare.application.service.impl.DoctorMentalHealthServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.AcademicClass;
import com.unicare.domain.entity.Dass21Result;
import com.unicare.domain.entity.Faculty;
import com.unicare.domain.entity.MoodLog;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;
import com.unicare.domain.repository.Dass21ResultRepository;
import com.unicare.domain.repository.MoodLogRepository;
import com.unicare.domain.repository.StudentRepository;
import com.unicare.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorMentalHealthServiceImplTest {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final ZoneOffset VN_OFFSET = ZoneOffset.ofHours(7);

    @Mock
    private Dass21ResultRepository dass21ResultRepository;

    @Mock
    private MoodLogRepository moodLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    private DoctorMentalHealthServiceImpl doctorMentalHealthService;

    private User specialistUser;
    private User adminUser;
    private User studentUser;
    private User otherStudentUser;
    private Student studentEntity;

    private Dass21Result olderTest;
    private Dass21Result newerTest;
    private MoodLog logDay1;
    private MoodLog logDay2;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-11-26T03:00:00Z"), VIETNAM_ZONE);
        doctorMentalHealthService = new DoctorMentalHealthServiceImpl(
                dass21ResultRepository,
                moodLogRepository,
                userRepository,
                studentRepository,
                fixedClock
        );

        specialistUser = new User("CG001", "pwd", "ThS. Nguyễn Tâm Lý", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        specialistUser.setId(5L);

        adminUser = new User("admin", "pwd", "Quản Trị Viên", Role.BAN_QUAN_LY, UserStatus.ACTIVE);
        adminUser.setId(1L);

        studentUser = new User("24520579", "pwd", "Tăng Chấn Hồng", Role.SINH_VIEN, UserStatus.ACTIVE);
        studentUser.setId(2L);
        studentUser.setEmail("24520579@gm.uit.edu.vn");
        studentUser.setPhoneNumber("0901234567");

        otherStudentUser = new User("24520999", "pwd", "Trần Văn Khác", Role.SINH_VIEN, UserStatus.ACTIVE);
        otherStudentUser.setId(9L);

        Faculty faculty = new Faculty(1, "CNPM", "Công nghệ Phần mềm");
        AcademicClass academicClass = new AcademicClass(10, "SE104.O11", "Kỹ thuật phần mềm K19", "K19", faculty);

        studentEntity = Student.builder()
                .id(2L)
                .user(studentUser)
                .studentCode("24520579")
                .academicClass(academicClass)
                .dateOfBirth(LocalDate.of(2006, 5, 15))
                .gender("Nam")
                .build();

        olderTest = Dass21Result.builder()
                .id(101L)
                .student(studentEntity)
                .testDate(OffsetDateTime.of(2026, 11, 1, 9, 0, 0, 0, VN_OFFSET))
                .answers(List.of(1, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2))
                .depressionScore(18)
                .anxietyScore(14)
                .stressScore(22)
                .depressionLevel("Vừa")
                .anxietyLevel("Vừa")
                .stressLevel("Vừa")
                .build();

        newerTest = Dass21Result.builder()
                .id(102L)
                .student(studentEntity)
                .testDate(OffsetDateTime.of(2026, 11, 20, 14, 30, 0, 0, VN_OFFSET))
                .answers(List.of(0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0))
                .depressionScore(10)
                .anxietyScore(8)
                .stressScore(12)
                .depressionLevel("Nhẹ")
                .anxietyLevel("Nhẹ")
                .stressLevel("Bình thường")
                .build();

        logDay1 = MoodLog.builder()
                .id(201L)
                .student(studentEntity)
                .moodLevel(4)
                .notes("Ngủ ngon hơn sau buổi tư vấn")
                .logTime(OffsetDateTime.of(2026, 11, 25, 20, 0, 0, 0, VN_OFFSET))
                .build();

        logDay2 = MoodLog.builder()
                .id(202L)
                .student(studentEntity)
                .moodLevel(3)
                .notes("Áp lực bài tập cuối kỳ")
                .logTime(OffsetDateTime.of(2026, 11, 22, 21, 0, 0, 0, VN_OFFSET))
                .build();
    }

    @Test
    @DisplayName("Chuyên gia tâm lý xem tổng hợp lịch sử DASS-21 (chuỗi điểm ASC + danh sách DESC) và Nhật ký tâm trạng thành công")
    void getStudentMentalHealthHistory_Success() {
        when(userRepository.findByUsername("CG001")).thenReturn(Optional.of(specialistUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(studentEntity));
        when(dass21ResultRepository.findByStudentIdWithStudentOrderByTestDateDesc(2L))
                .thenReturn(List.of(newerTest, olderTest));
        when(moodLogRepository.findByStudentIdAndLogTimeRange(eq(2L), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(logDay1, logDay2));

        StudentMentalHealthHistoryResponse response = doctorMentalHealthService.getStudentMentalHealthHistory(
                "CG001", 2L, null, null, null
        );

        assertThat(response).isNotNull();
        assertThat(response.getStudentInfo()).isNotNull();
        assertThat(response.getStudentInfo().getStudentCode()).isEqualTo("24520579");
        assertThat(response.getStudentInfo().getFullName()).isEqualTo("Tăng Chấn Hồng");
        assertThat(response.getStudentInfo().getClassCode()).isEqualTo("SE104.O11");

        assertThat(response.getTotalDass21Tests()).isEqualTo(2);
        assertThat(response.getLatestDass21()).isNotNull();
        assertThat(response.getLatestDass21().getId()).isEqualTo(102L);
        assertThat(response.getLatestDass21().getDepressionScore()).isEqualTo(10);
        assertThat(response.getLatestDass21().getTotalScore()).isEqualTo(30);

        // Danh sách chi tiết sắp xếp giảm dần (mới nhất trước)
        assertThat(response.getDass21Results())
                .extracting(Dass21ResultResponse::getId)
                .containsExactly(102L, 101L);

        // Chuỗi điểm qua các lần test sắp xếp tăng dần theo thời gian (cũ -> mới) phục vụ biểu đồ xu hướng
        assertThat(response.getDass21ScoreTrend())
                .extracting(Dass21ScorePointResponse::getId)
                .containsExactly(101L, 102L);
        assertThat(response.getDass21ScoreTrend().get(0).getDepressionScore()).isEqualTo(18);
        assertThat(response.getDass21ScoreTrend().get(1).getDepressionScore()).isEqualTo(10);

        // Nhật ký tâm trạng (mặc định MONTH: 30 ngày gần nhất)
        MoodLogHistoryResponse moodHistory = response.getMoodHistory();
        assertThat(moodHistory).isNotNull();
        assertThat(moodHistory.getPeriod()).isEqualTo("MONTH");
        assertThat(moodHistory.getFromDate()).isEqualTo(LocalDate.of(2026, 10, 28));
        assertThat(moodHistory.getToDate()).isEqualTo(LocalDate.of(2026, 11, 26));
        assertThat(moodHistory.getTotalLogs()).isEqualTo(2);
        assertThat(moodHistory.getAverageMoodLevel()).isEqualTo(3.5);
        assertThat(moodHistory.getLogs()).hasSize(2);
    }

    @Test
    @DisplayName("Sinh viên chưa làm bài DASS-21 và chưa ghi nhật ký tâm trạng trả về dữ liệu rỗng an toàn (không lỗi 404)")
    void getStudentMentalHealthHistory_EmptyData_AndFallbackLookup() {
        DoctorMentalHealthServiceImpl defaultClockService = new DoctorMentalHealthServiceImpl(
                dass21ResultRepository, moodLogRepository, userRepository, studentRepository
        );
        DoctorMentalHealthServiceImpl nullClockService = new DoctorMentalHealthServiceImpl(
                dass21ResultRepository, moodLogRepository, userRepository, studentRepository, null
        );
        assertThat(defaultClockService).isNotNull();

        when(userRepository.findByUsername("CG001")).thenReturn(Optional.of(specialistUser));
        when(userRepository.findById(24520579L)).thenReturn(Optional.empty());
        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByIdWithDetails(2L)).thenReturn(Optional.empty());
        when(studentRepository.findById(2L)).thenReturn(Optional.of(studentEntity));
        when(dass21ResultRepository.findByStudentIdWithStudentOrderByTestDateDesc(2L)).thenReturn(null);
        when(moodLogRepository.findByStudentIdAndLogTimeRange(eq(2L), any(), any())).thenReturn(null);

        StudentMentalHealthHistoryResponse emptyRes = nullClockService.getStudentMentalHealthHistory(
                "CG001", 24520579L, "   ", null, null
        );

        assertThat(emptyRes.getTotalDass21Tests()).isZero();
        assertThat(emptyRes.getLatestDass21()).isNull();
        assertThat(emptyRes.getDass21ScoreTrend()).isEmpty();
        assertThat(emptyRes.getDass21Results()).isEmpty();
        assertThat(emptyRes.getMoodHistory().getTotalLogs()).isZero();
        assertThat(emptyRes.getMoodHistory().getAverageMoodLevel()).isNull();
    }

    @Test
    @DisplayName("Lọc riêng lịch sử DASS-21 và Nhật ký tâm trạng theo WEEK, MONTH, ALL và khoảng ngày CUSTOM")
    void getStudentDass21AndMoodLogs_AllFilterModes() {
        when(userRepository.findByUsername("CG001")).thenReturn(Optional.of(specialistUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(studentUser));

        // 1. getStudentDass21History: không lọc ngày, chỉ có fromDate, chỉ có toDate, có cả hai
        when(dass21ResultRepository.findByStudentIdWithStudentOrderByTestDateDesc(2L))
                .thenReturn(List.of(newerTest, olderTest));
        when(dass21ResultRepository.findByStudentIdAndTestDateRange(eq(2L), any(), any()))
                .thenReturn(List.of(newerTest));

        assertThat(doctorMentalHealthService.getStudentDass21History("CG001", 2L, null, null)).hasSize(2);
        assertThat(doctorMentalHealthService.getStudentDass21History("CG001", 2L, LocalDate.of(2026, 11, 10), null)).hasSize(1);
        assertThat(doctorMentalHealthService.getStudentDass21History("CG001", 2L, null, LocalDate.of(2026, 11, 25))).hasSize(1);
        assertThat(doctorMentalHealthService.getStudentDass21History(
                "CG001", 2L, LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 25))).hasSize(1);

        // 2. getStudentMoodLogsForDoctor: WEEK / 7D
        when(moodLogRepository.findByStudentIdAndLogTimeRange(eq(2L), any(), any()))
                .thenReturn(List.of(logDay1));
        MoodLogHistoryResponse weekRes = doctorMentalHealthService.getStudentMoodLogsForDoctor(
                "CG001", 2L, "WEEK", null, null
        );
        assertThat(weekRes.getPeriod()).isEqualTo("WEEK");
        assertThat(weekRes.getFromDate()).isEqualTo(LocalDate.of(2026, 11, 20));
        assertThat(weekRes.getToDate()).isEqualTo(LocalDate.of(2026, 11, 26));
        assertThat(weekRes.getAverageMoodLevel()).isEqualTo(4.0);

        // 3. ALL có dữ liệu & ALL rỗng
        when(moodLogRepository.findAllByStudentIdWithStudentOrderByLogTimeDesc(2L))
                .thenReturn(List.of(logDay1, logDay2));
        MoodLogHistoryResponse allRes = doctorMentalHealthService.getStudentMoodLogsForDoctor(
                "CG001", 2L, "ALL", null, null
        );
        assertThat(allRes.getPeriod()).isEqualTo("ALL");
        assertThat(allRes.getFromDate()).isEqualTo(LocalDate.of(2026, 11, 22));
        assertThat(allRes.getToDate()).isEqualTo(LocalDate.of(2026, 11, 25));

        MoodLog nullLevelLog = MoodLog.builder().id(203L).student(studentEntity).moodLevel(null).build();
        when(moodLogRepository.findAllByStudentIdWithStudentOrderByLogTimeDesc(2L))
                .thenReturn(List.of(nullLevelLog));
        MoodLogHistoryResponse nullLevelRes = doctorMentalHealthService.getStudentMoodLogsForDoctor(
                "CG001", 2L, "ALL", null, null
        );
        assertThat(nullLevelRes.getAverageMoodLevel()).isNull();

        when(moodLogRepository.findAllByStudentIdWithStudentOrderByLogTimeDesc(2L))
                .thenReturn(List.of());
        MoodLogHistoryResponse emptyAllRes = doctorMentalHealthService.getStudentMoodLogsForDoctor(
                "CG001", 2L, "ALL", null, null
        );
        assertThat(emptyAllRes.getFromDate()).isNull();
        assertThat(emptyAllRes.getToDate()).isNull();

        // 4. CUSTOM khoảng ngày (fromDate only, toDate only, both)
        MoodLogHistoryResponse customFromOnly = doctorMentalHealthService.getStudentMoodLogsForDoctor(
                "CG001", 2L, null, LocalDate.of(2026, 11, 1), null
        );
        assertThat(customFromOnly.getPeriod()).isEqualTo("CUSTOM");
        assertThat(customFromOnly.getToDate()).isEqualTo(LocalDate.of(2026, 11, 26));

        MoodLogHistoryResponse customToOnly = doctorMentalHealthService.getStudentMoodLogsForDoctor(
                "CG001", 2L, null, null, LocalDate.of(2026, 11, 20)
        );
        assertThat(customToOnly.getPeriod()).isEqualTo("CUSTOM");
        assertThat(customToOnly.getFromDate()).isEqualTo(LocalDate.of(2000, 1, 1));
    }

    @Test
    @DisplayName("Phân quyền nghiêm ngặt: Chặn tuyệt đối Ban quản lý (BAN_QUAN_LY), các Sinh viên khác và người chưa đăng nhập")
    void strictAuthorization_BlocksAdminAndStudents() {
        // 1. Chưa đăng nhập
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentMentalHealthHistory(null, 2L, null, null, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Yêu cầu đăng nhập");
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentMentalHealthHistory("   ", 2L, null, null, null))
                .isInstanceOf(AccessDeniedException.class);

        // 2. Tài khoản đăng nhập không tồn tại
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentDass21History("ghost", 2L, null, null))
                .isInstanceOf(ResourceNotFoundException.class);

        // 3. Chặn tuyệt đối Ban quản lý (ADMIN / BAN_QUAN_LY)
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentMentalHealthHistory("admin", 2L, null, null, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Ban quản lý không được phép truy cập");
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentDass21History("admin", 2L, null, null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentMoodLogsForDoctor("admin", 2L, null, null, null))
                .isInstanceOf(AccessDeniedException.class);

        // 4. Chặn tuyệt đối các Sinh viên khác (và mọi tài khoản SINH_VIEN gọi vào endpoint của Bác sĩ)
        when(userRepository.findByUsername("24520999")).thenReturn(Optional.of(otherStudentUser));
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentMentalHealthHistory("24520999", 2L, null, null, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ Bác sĩ / Chuyên gia tâm lý");
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentDass21History("24520999", 2L, null, null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentMoodLogsForDoctor("24520999", 2L, null, null, null))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(dass21ResultRepository, moodLogRepository);
    }

    @Test
    @DisplayName("Validate tham số lỗi đầu vào: studentId rỗng/không tồn tại/không phải SV, ngày kết thúc < ngày bắt đầu, period sai")
    void validationErrors_InvalidStudentAndQueryParams() {
        when(userRepository.findByUsername("CG001")).thenReturn(Optional.of(specialistUser));

        // 1. studentId == null
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentMentalHealthHistory("CG001", null, null, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("studentId");

        // 2. Không tìm thấy sinh viên
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        when(userRepository.findByUsername("999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentMentalHealthHistory("CG001", 999L, null, null, null))
                .isInstanceOf(ResourceNotFoundException.class);

        // 3. Tài khoản mục tiêu không phải SINH_VIEN (VD: truyền ID của Admin)
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentMentalHealthHistory("CG001", 1L, null, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không phải là sinh viên");

        // 4. toDate < fromDate
        when(userRepository.findById(2L)).thenReturn(Optional.of(studentUser));
        LocalDate from = LocalDate.of(2026, 11, 20);
        LocalDate to = LocalDate.of(2026, 11, 10);
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentMentalHealthHistory("CG001", 2L, null, from, to))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ngày kết thúc không được nhỏ hơn ngày bắt đầu");

        // 5. period không hợp lệ
        assertThatThrownBy(() -> doctorMentalHealthService.getStudentMoodLogsForDoctor("CG001", 2L, "YEAR", null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Giá trị period không hợp lệ");
    }

    @Test
    @DisplayName("Kiểm tra toàn bộ nhánh biên của các DTO Dass21ResultResponse, Dass21ScorePointResponse, StudentMentalHealthHistoryResponse")
    void mentalHealthDtos_EdgeBranches() {
        assertThat(Dass21ResultResponse.fromEntity(null)).isNull();
        assertThat(Dass21ScorePointResponse.fromEntity(null)).isNull();

        // Dass21Result với các trường null, student không có studentCode (fallback sang username), student không có user
        Student studentBlankCode = Student.builder().id(2L).user(studentUser).studentCode("   ").build();
        Dass21Result partialEntity = Dass21Result.builder()
                .id(301L)
                .student(studentBlankCode)
                .answers(null)
                .depressionScore(null)
                .anxietyScore(null)
                .stressScore(null)
                .build();
        Dass21ResultResponse partialRes = Dass21ResultResponse.fromEntity(partialEntity);
        assertThat(partialRes.getStudentCode()).isEqualTo("24520579");
        assertThat(partialRes.getAnswers()).isEmpty();
        assertThat(partialRes.getTotalScore()).isZero();

        Student studentNoUser = Student.builder().id(3L).user(null).studentCode(null).build();
        Dass21Result noUserEntity = Dass21Result.builder().id(302L).student(studentNoUser).build();
        Dass21ResultResponse noUserRes = Dass21ResultResponse.fromEntity(noUserEntity);
        assertThat(noUserRes.getStudentCode()).isNull();
        assertThat(noUserRes.getStudentName()).isNull();

        Dass21Result noStudentEntity = Dass21Result.builder().id(303L).student(null).build();
        assertThat(Dass21ResultResponse.fromEntity(noStudentEntity).getStudentId()).isNull();

        Dass21ScorePointResponse partialPoint = Dass21ScorePointResponse.fromEntity(partialEntity);
        assertThat(partialPoint.getTotalScore()).isZero();
        assertThat(partialPoint.getTestDate()).isNotNull();
        assertThat(partialPoint.getDepressionLevel()).isNull();
        assertThat(partialPoint.getAnxietyLevel()).isNull();
        assertThat(partialPoint.getStressLevel()).isNull();
        assertThat(partialPoint.getAnxietyScore()).isNull();
        assertThat(partialPoint.getStressScore()).isNull();

        // Direct setters & getters trên các DTO
        Dass21ResultResponse directRes = new Dass21ResultResponse();
        directRes.setAnswers(null);
        assertThat(directRes.getAnswers()).isEmpty();
        assertThat(directRes.getStudentName()).isNull();
        assertThat(directRes.getTestDate()).isNull();
        assertThat(directRes.getAnxietyScore()).isNull();
        assertThat(directRes.getStressScore()).isNull();
        assertThat(directRes.getDepressionLevel()).isNull();
        assertThat(directRes.getAnxietyLevel()).isNull();
        assertThat(directRes.getStressLevel()).isNull();

        StudentMentalHealthHistoryResponse emptyHistory = new StudentMentalHealthHistoryResponse();
        emptyHistory.setStudentInfo(null);
        emptyHistory.setTotalDass21Tests(0);
        emptyHistory.setLatestDass21(null);
        emptyHistory.setDass21ScoreTrend(null);
        emptyHistory.setDass21Results(null);
        emptyHistory.setMoodHistory(null);
        assertThat(emptyHistory.getStudentInfo()).isNull();
        assertThat(emptyHistory.getDass21ScoreTrend()).isEmpty();
        assertThat(emptyHistory.getDass21Results()).isEmpty();
        assertThat(emptyHistory.getMoodHistory()).isNull();

        StudentMentalHealthHistoryResponse nullListCtor = new StudentMentalHealthHistoryResponse(
                null, 0, null, null, null, null
        );
        assertThat(nullListCtor.getDass21ScoreTrend()).isEmpty();
        assertThat(nullListCtor.getDass21Results()).isEmpty();

        verifyNoInteractions(dass21ResultRepository);
        verify(moodLogRepository, org.mockito.Mockito.never()).findById(any());
    }
}
