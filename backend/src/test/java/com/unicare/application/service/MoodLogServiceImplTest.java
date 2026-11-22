package com.unicare.application.service;

import com.unicare.application.dto.request.MoodLogRequest;
import com.unicare.application.dto.response.MoodLogHistoryResponse;
import com.unicare.application.dto.response.MoodLogResponse;
import com.unicare.application.service.impl.MoodLogServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.MoodLog;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.MoodLevel;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;
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
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoodLogServiceImplTest {

    @Mock
    private MoodLogRepository moodLogRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    private MoodLogServiceImpl moodLogService;

    private User studentUser;
    private User otherStudentUser;
    private User doctorUser;
    private User adminUser;
    private Student studentEntity;
    private Student otherStudentEntity;

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        // 2026-11-22 14:00:00+07:00 (Chủ nhật)
        fixedClock = Clock.fixed(Instant.parse("2026-11-22T07:00:00Z"), MoodLogServiceImpl.VIETNAM_ZONE);
        moodLogService = new MoodLogServiceImpl(moodLogRepository, studentRepository, userRepository, fixedClock);

        studentUser = new User("24520579", "hashed_pwd", "Tăng Chấn Hồng", Role.SINH_VIEN, UserStatus.ACTIVE);
        studentUser.setId(2L);

        otherStudentUser = new User("24521234", "hashed_pwd", "Nguyễn Văn A", Role.SINH_VIEN, UserStatus.ACTIVE);
        otherStudentUser.setId(5L);

        doctorUser = new User("NV001", "hashed_pwd", "Bác Sĩ A", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        doctorUser.setId(3L);

        adminUser = new User("admin", "hashed_pwd", "Ban Quản Lý", Role.BAN_QUAN_LY, UserStatus.ACTIVE);
        adminUser.setId(1L);

        studentEntity = Student.builder()
                .id(2L)
                .user(studentUser)
                .studentCode("24520579")
                .build();

        otherStudentEntity = Student.builder()
                .id(5L)
                .user(otherStudentUser)
                .studentCode("24521234")
                .build();

        lenient().when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(moodLogRepository.save(any(MoodLog.class))).thenAnswer(inv -> {
            MoodLog log = inv.getArgument(0);
            if (log.getId() == null) {
                log.setId(100L);
            }
            return log;
        });
    }

    @Test
    @DisplayName("POST - Tạo nhật ký tâm trạng thành công với logTime mặc định và tự tạo Student nếu chưa có")
    void createMoodLog_Success_DefaultTimeAndAutoCreateStudent() {
        MoodLogRequest request = MoodLogRequest.builder()
                .moodLevel(4)
                .notes("  Hoàn thành bài tập lớn đúng hạn  ")
                .build();

        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        when(moodLogRepository.existsByStudentIdAndLogTimeGreaterThanEqualAndLogTimeLessThan(
                eq(2L),
                eq(OffsetDateTime.parse("2026-11-22T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2026-11-23T00:00:00+07:00"))
        )).thenReturn(false);
        when(studentRepository.findById(2L)).thenReturn(Optional.empty());

        MoodLogResponse response = moodLogService.createMoodLog(request, "24520579");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStudentId()).isEqualTo(2L);
        assertThat(response.getStudentCode()).isEqualTo("24520579");
        assertThat(response.getStudentName()).isEqualTo("Tăng Chấn Hồng");
        assertThat(response.getMoodLevel()).isEqualTo(4);
        assertThat(response.getMoodLabel()).isEqualTo("Vui vẻ");
        assertThat(response.getNotes()).isEqualTo("Hoàn thành bài tập lớn đúng hạn");
        assertThat(response.getLogTime()).isEqualTo(OffsetDateTime.parse("2026-11-22T14:00:00+07:00"));
        assertThat(response.getLogDate()).isEqualTo(LocalDate.of(2026, 11, 22));
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("POST - Quy đổi logTime từ UTC sang giờ Việt Nam (+07:00) và chuẩn hóa notes rỗng thành null")
    void createMoodLog_Success_CustomUtcTimeAndBlankNotes() {
        // 2026-11-20T20:30:00Z tương đương 2026-11-21T03:30:00+07:00 -> thuộc ngày 2026-11-21
        MoodLogRequest request = MoodLogRequest.builder()
                .moodLevel(3)
                .notes("   ")
                .logTime(OffsetDateTime.of(2026, 11, 20, 20, 30, 0, 0, ZoneOffset.UTC))
                .build();

        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        when(moodLogRepository.existsByStudentIdAndLogTimeGreaterThanEqualAndLogTimeLessThan(
                eq(2L),
                eq(OffsetDateTime.parse("2026-11-21T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2026-11-22T00:00:00+07:00"))
        )).thenReturn(false);
        when(studentRepository.findById(2L)).thenReturn(Optional.of(studentEntity));

        MoodLogResponse response = moodLogService.createMoodLog(request, "24520579");

        assertThat(response.getMoodLevel()).isEqualTo(3);
        assertThat(response.getMoodLabel()).isEqualTo("Bình thường");
        assertThat(response.getNotes()).isNull();
        assertThat(response.getLogDate()).isEqualTo(LocalDate.of(2026, 11, 21));
        assertThat(response.getLogTime().getOffset()).isEqualTo(ZoneOffset.ofHours(7));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("POST - Chặn ghi trùng nhật ký thứ hai trong cùng một ngày (409 Conflict)")
    void createMoodLog_DuplicateSameDay_ThrowsConflict() {
        MoodLogRequest request = MoodLogRequest.builder()
                .moodLevel(5)
                .notes("Tuyệt vời")
                .build();

        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        when(moodLogRepository.existsByStudentIdAndLogTimeGreaterThanEqualAndLogTimeLessThan(
                eq(2L),
                eq(OffsetDateTime.parse("2026-11-22T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2026-11-23T00:00:00+07:00"))
        )).thenReturn(true);

        assertThatThrownBy(() -> moodLogService.createMoodLog(request, "24520579"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("2026-11-22");
    }

    @Test
    @DisplayName("POST - Validate chặt đầu vào, thời điểm tương lai và phân quyền SINH_VIEN")
    void createMoodLog_ValidationAndAuthorizationErrors() {
        assertThatThrownBy(() -> moodLogService.createMoodLog(null, "24520579"))
                .isInstanceOf(BadRequestException.class);

        MoodLogRequest nullLevel = MoodLogRequest.builder().moodLevel(null).build();
        assertThatThrownBy(() -> moodLogService.createMoodLog(nullLevel, "24520579"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không được để trống");

        MoodLogRequest levelOutOfRange = MoodLogRequest.builder().moodLevel(6).build();
        assertThatThrownBy(() -> moodLogService.createMoodLog(levelOutOfRange, "24520579"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("1 đến 5");

        MoodLogRequest tooLongNotes = MoodLogRequest.builder()
                .moodLevel(2)
                .notes("A".repeat(2001))
                .build();
        assertThatThrownBy(() -> moodLogService.createMoodLog(tooLongNotes, "24520579"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("2000 ký tự");

        MoodLogRequest validRequest = MoodLogRequest.builder().moodLevel(4).build();
        assertThatThrownBy(() -> moodLogService.createMoodLog(validRequest, null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> moodLogService.createMoodLog(validRequest, "   "))
                .isInstanceOf(AccessDeniedException.class);

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> moodLogService.createMoodLog(validRequest, "ghost"))
                .isInstanceOf(ResourceNotFoundException.class);

        when(userRepository.findByUsername("NV001")).thenReturn(Optional.of(doctorUser));
        assertThatThrownBy(() -> moodLogService.createMoodLog(validRequest, "NV001"))
                .isInstanceOf(AccessDeniedException.class);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertThatThrownBy(() -> moodLogService.createMoodLog(validRequest, "admin"))
                .isInstanceOf(AccessDeniedException.class);

        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        MoodLogRequest futureRequest = MoodLogRequest.builder()
                .moodLevel(4)
                .logTime(OffsetDateTime.parse("2026-11-22T14:10:00+07:00"))
                .build();
        assertThatThrownBy(() -> moodLogService.createMoodLog(futureRequest, "24520579"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("tương lai");
    }

    @Test
    @DisplayName("PUT /{id} - Cập nhật tâm trạng giữ nguyên logTime hoặc đổi sang ngày khác chưa có bản ghi")
    void updateMoodLog_Success() {
        OffsetDateTime originalTime = OffsetDateTime.parse("2026-11-22T09:00:00+07:00");
        MoodLog existing = MoodLog.builder()
                .id(10L)
                .student(studentEntity)
                .moodLevel(2)
                .notes("Hơi mệt")
                .logTime(originalTime)
                .build();

        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        when(moodLogRepository.findByIdWithStudent(10L)).thenReturn(Optional.of(existing));

        MoodLogRequest updateKeepTime = MoodLogRequest.builder()
                .moodLevel(4)
                .notes("  Nghỉ ngơi xong thấy khỏe hơn  ")
                .build();

        MoodLogResponse res1 = moodLogService.updateMoodLog(10L, updateKeepTime, "24520579");
        assertThat(res1.getMoodLevel()).isEqualTo(4);
        assertThat(res1.getMoodLabel()).isEqualTo("Vui vẻ");
        assertThat(res1.getNotes()).isEqualTo("Nghỉ ngơi xong thấy khỏe hơn");
        assertThat(res1.getLogTime()).isEqualTo(originalTime);

        OffsetDateTime newTime = OffsetDateTime.parse("2026-11-21T20:00:00+07:00");
        when(moodLogRepository.existsByStudentIdAndLogTimeGreaterThanEqualAndLogTimeLessThanAndIdNot(
                eq(2L),
                eq(OffsetDateTime.parse("2026-11-21T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2026-11-22T00:00:00+07:00")),
                eq(10L)
        )).thenReturn(false);

        MoodLogRequest updateWithNewDate = MoodLogRequest.builder()
                .moodLevel(5)
                .notes("Tuyệt vời")
                .logTime(newTime)
                .build();
        MoodLogResponse res2 = moodLogService.updateMoodLog(10L, updateWithNewDate, "24520579");
        assertThat(res2.getMoodLevel()).isEqualTo(5);
        assertThat(res2.getLogTime()).isEqualTo(newTime);
        assertThat(res2.getLogDate()).isEqualTo(LocalDate.of(2026, 11, 21));
    }

    @Test
    @DisplayName("PUT /{id} - Chặn sửa nhật ký của người khác, không tìm thấy ID hoặc dời sang ngày đã có bản ghi")
    void updateMoodLog_Errors() {
        MoodLogRequest validRequest = MoodLogRequest.builder().moodLevel(3).notes("Ổn").build();

        assertThatThrownBy(() -> moodLogService.updateMoodLog(null, validRequest, "24520579"))
                .isInstanceOf(BadRequestException.class);

        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        when(moodLogRepository.findByIdWithStudent(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> moodLogService.updateMoodLog(999L, validRequest, "24520579"))
                .isInstanceOf(ResourceNotFoundException.class);

        MoodLog otherLog = MoodLog.builder()
                .id(20L)
                .student(otherStudentEntity)
                .moodLevel(3)
                .logTime(OffsetDateTime.parse("2026-11-22T08:00:00+07:00"))
                .build();
        when(moodLogRepository.findByIdWithStudent(20L)).thenReturn(Optional.of(otherLog));
        assertThatThrownBy(() -> moodLogService.updateMoodLog(20L, validRequest, "24520579"))
                .isInstanceOf(AccessDeniedException.class);

        MoodLog orphanLog = MoodLog.builder().id(21L).student(null).moodLevel(3).build();
        when(moodLogRepository.findByIdWithStudent(21L)).thenReturn(Optional.of(orphanLog));
        assertThatThrownBy(() -> moodLogService.updateMoodLog(21L, validRequest, "24520579"))
                .isInstanceOf(AccessDeniedException.class);

        MoodLog ownLog = MoodLog.builder()
                .id(10L)
                .student(studentEntity)
                .moodLevel(3)
                .logTime(OffsetDateTime.parse("2026-11-22T08:00:00+07:00"))
                .build();
        when(moodLogRepository.findByIdWithStudent(10L)).thenReturn(Optional.of(ownLog));
        when(moodLogRepository.existsByStudentIdAndLogTimeGreaterThanEqualAndLogTimeLessThanAndIdNot(
                eq(2L),
                eq(OffsetDateTime.parse("2026-11-20T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2026-11-21T00:00:00+07:00")),
                eq(10L)
        )).thenReturn(true);

        MoodLogRequest moveToExistingDay = MoodLogRequest.builder()
                .moodLevel(4)
                .logTime(OffsetDateTime.parse("2026-11-20T18:00:00+07:00"))
                .build();
        assertThatThrownBy(() -> moodLogService.updateMoodLog(10L, moveToExistingDay, "24520579"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("2026-11-20");
    }

    @Test
    @DisplayName("PUT /api/mental-health/mood-logs - Cập nhật theo id trong body hoặc theo bản ghi trong ngày")
    void updateMoodLogByRequest_SuccessAndErrors() {
        assertThatThrownBy(() -> moodLogService.updateMoodLogByRequest(null, "24520579"))
                .isInstanceOf(BadRequestException.class);

        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));

        // 1. Có id trong body -> delegate update theo ID
        MoodLog existingById = MoodLog.builder()
                .id(15L)
                .student(studentEntity)
                .moodLevel(2)
                .logTime(OffsetDateTime.parse("2026-11-19T10:00:00+07:00"))
                .build();
        when(moodLogRepository.findByIdWithStudent(15L)).thenReturn(Optional.of(existingById));
        MoodLogResponse byIdRes = moodLogService.updateMoodLogByRequest(
                MoodLogRequest.builder().id(15L).moodLevel(4).notes("Cập nhật theo ID").build(),
                "24520579"
        );
        assertThat(byIdRes.getId()).isEqualTo(15L);
        assertThat(byIdRes.getMoodLevel()).isEqualTo(4);

        // 2. Không có id & không có logTime -> cập nhật bản ghi hôm nay (2026-11-22)
        MoodLog todayLog = MoodLog.builder()
                .id(16L)
                .student(studentEntity)
                .moodLevel(1)
                .logTime(OffsetDateTime.parse("2026-11-22T09:00:00+07:00"))
                .build();
        when(moodLogRepository.findByStudentIdAndLogTimeRange(
                eq(2L),
                eq(OffsetDateTime.parse("2026-11-22T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2026-11-23T00:00:00+07:00"))
        )).thenReturn(List.of(todayLog));

        MoodLogResponse todayRes = moodLogService.updateMoodLogByRequest(
                MoodLogRequest.builder().moodLevel(5).notes("Đã giải quyết xong").build(),
                "24520579"
        );
        assertThat(todayRes.getId()).isEqualTo(16L);
        assertThat(todayRes.getMoodLevel()).isEqualTo(5);
        assertThat(todayRes.getMoodLabel()).isEqualTo("Tuyệt vời");

        // 3. Không có id nhưng có logTime -> tìm bản ghi ngày 2026-11-21 và cập nhật luôn logTime
        OffsetDateTime newLogTime = OffsetDateTime.parse("2026-11-21T21:15:00+07:00");
        MoodLog yesterdayLog = MoodLog.builder()
                .id(17L)
                .student(studentEntity)
                .moodLevel(3)
                .logTime(OffsetDateTime.parse("2026-11-21T08:00:00+07:00"))
                .build();
        when(moodLogRepository.findByStudentIdAndLogTimeRange(
                eq(2L),
                eq(OffsetDateTime.parse("2026-11-21T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2026-11-22T00:00:00+07:00"))
        )).thenReturn(List.of(yesterdayLog));

        MoodLogResponse yesterdayRes = moodLogService.updateMoodLogByRequest(
                MoodLogRequest.builder().moodLevel(4).logTime(newLogTime).build(),
                "24520579"
        );
        assertThat(yesterdayRes.getLogTime()).isEqualTo(newLogTime);

        // 4. Không có bản ghi nào trong ngày -> 404 Not Found
        when(moodLogRepository.findByStudentIdAndLogTimeRange(
                eq(2L),
                eq(OffsetDateTime.parse("2026-11-20T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2026-11-21T00:00:00+07:00"))
        )).thenReturn(Collections.emptyList());
        assertThatThrownBy(() -> moodLogService.updateMoodLogByRequest(
                MoodLogRequest.builder()
                        .moodLevel(3)
                        .logTime(OffsetDateTime.parse("2026-11-20T10:00:00+07:00"))
                        .build(),
                "24520579"
        )).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("2026-11-20");
    }

    @Test
    @DisplayName("GET - Truy vấn lịch sử theo tuần (mặc định & theo ngày), theo tháng (mặc định, year/month, năm nhuận) và khoảng ngày")
    void getMyMoodLogs_WeekMonthCustom_Success() {
        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));

        MoodLog log1 = MoodLog.builder()
                .id(3L)
                .student(studentEntity)
                .moodLevel(4)
                .notes("Chủ nhật thư giãn")
                .logTime(OffsetDateTime.parse("2026-11-22T10:00:00+07:00"))
                .build();
        MoodLog log2 = MoodLog.builder()
                .id(2L)
                .student(studentEntity)
                .moodLevel(3)
                .notes("Giữa tuần bình thường")
                .logTime(OffsetDateTime.parse("2026-11-18T20:00:00+07:00"))
                .build();
        MoodLog log3 = MoodLog.builder()
                .id(1L)
                .student(studentEntity)
                .moodLevel(4)
                .notes("Đầu tuần nhiều năng lượng")
                .logTime(OffsetDateTime.parse("2026-11-16T08:30:00+07:00"))
                .build();

        // 1. Mặc định WEEK chứa 2026-11-22 -> 2026-11-16 (T2) .. 2026-11-22 (CN)
        when(moodLogRepository.findByStudentIdAndLogTimeRange(
                eq(2L),
                eq(OffsetDateTime.parse("2026-11-16T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2026-11-23T00:00:00+07:00"))
        )).thenReturn(List.of(log1, log2, log3));

        MoodLogHistoryResponse weekRes = moodLogService.getMyMoodLogs(
                "24520579", 2L, null, null, null, null, null, null
        );
        assertThat(weekRes.getPeriod()).isEqualTo("WEEK");
        assertThat(weekRes.getFromDate()).isEqualTo(LocalDate.of(2026, 11, 16));
        assertThat(weekRes.getToDate()).isEqualTo(LocalDate.of(2026, 11, 22));
        assertThat(weekRes.getTotalLogs()).isEqualTo(3);
        // (4 + 3 + 4) / 3 = 3.666... -> 3.7
        assertThat(weekRes.getAverageMoodLevel()).isEqualTo(3.7);
        assertThat(weekRes.getLogs()).hasSize(3);

        // 2. WEEK với ngày cụ thể 2026-11-04 (Thứ 4) -> 2026-11-02 .. 2026-11-08
        when(moodLogRepository.findByStudentIdAndLogTimeRange(
                eq(2L),
                eq(OffsetDateTime.parse("2026-11-02T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2026-11-09T00:00:00+07:00"))
        )).thenReturn(Collections.emptyList());

        MoodLogHistoryResponse emptyWeekRes = moodLogService.getMyMoodLogs(
                "24520579", null, "week", LocalDate.of(2026, 11, 4), null, null, null, null
        );
        assertThat(emptyWeekRes.getPeriod()).isEqualTo("WEEK");
        assertThat(emptyWeekRes.getFromDate()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(emptyWeekRes.getToDate()).isEqualTo(LocalDate.of(2026, 11, 8));
        assertThat(emptyWeekRes.getTotalLogs()).isZero();
        assertThat(emptyWeekRes.getAverageMoodLevel()).isNull();
        assertThat(emptyWeekRes.getLogs()).isEmpty();

        // 3. MONTH mặc định (tháng 11/2026 -> 01..30)
        when(moodLogRepository.findByStudentIdAndLogTimeRange(
                eq(2L),
                eq(OffsetDateTime.parse("2026-11-01T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2026-12-01T00:00:00+07:00"))
        )).thenReturn(List.of(log1));

        MoodLogHistoryResponse currentMonthRes = moodLogService.getMyMoodLogs(
                "24520579", null, "MONTH", null, null, null, null, null
        );
        assertThat(currentMonthRes.getPeriod()).isEqualTo("MONTH");
        assertThat(currentMonthRes.getFromDate()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(currentMonthRes.getToDate()).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(currentMonthRes.getTotalLogs()).isEqualTo(1);
        assertThat(currentMonthRes.getAverageMoodLevel()).isEqualTo(4.0);

        // 4. MONTH với year & month (Tháng 2/2026 -> 01..28)
        when(moodLogRepository.findByStudentIdAndLogTimeRange(
                eq(2L),
                eq(OffsetDateTime.parse("2026-02-01T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2026-03-01T00:00:00+07:00"))
        )).thenReturn(Collections.emptyList());

        MoodLogHistoryResponse febRes = moodLogService.getMyMoodLogs(
                "24520579", null, "MONTH", null, 2026, 2, null, null
        );
        assertThat(febRes.getFromDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(febRes.getToDate()).isEqualTo(LocalDate.of(2026, 2, 28));

        // 5. MONTH với date năm nhuận (2024-02-15 -> 01..29)
        when(moodLogRepository.findByStudentIdAndLogTimeRange(
                eq(2L),
                eq(OffsetDateTime.parse("2024-02-01T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2024-03-01T00:00:00+07:00"))
        )).thenReturn(Collections.emptyList());

        MoodLogHistoryResponse leapFebRes = moodLogService.getMyMoodLogs(
                "24520579", null, "MONTH", LocalDate.of(2024, 2, 15), null, null, null, null
        );
        assertThat(leapFebRes.getFromDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(leapFebRes.getToDate()).isEqualTo(LocalDate.of(2024, 2, 29));

        // 6. CUSTOM với fromDate & toDate
        when(moodLogRepository.findByStudentIdAndLogTimeRange(
                eq(2L),
                eq(OffsetDateTime.parse("2026-10-25T00:00:00+07:00")),
                eq(OffsetDateTime.parse("2026-11-05T00:00:00+07:00"))
        )).thenReturn(List.of(log2));

        MoodLogHistoryResponse customRes = moodLogService.getMyMoodLogs(
                "24520579", null, null, null, null, null,
                LocalDate.of(2026, 10, 25), LocalDate.of(2026, 11, 4)
        );
        assertThat(customRes.getPeriod()).isEqualTo("CUSTOM");
        assertThat(customRes.getFromDate()).isEqualTo(LocalDate.of(2026, 10, 25));
        assertThat(customRes.getToDate()).isEqualTo(LocalDate.of(2026, 11, 4));
        assertThat(customRes.getTotalLogs()).isEqualTo(1);
    }

    @Test
    @DisplayName("GET - Chặn IDOR và validate các tham số lọc kỳ truy vấn không hợp lệ")
    void getMyMoodLogs_IdorAndInvalidParams() {
        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));

        assertThatThrownBy(() -> moodLogService.getMyMoodLogs(
                "24520579", 99L, "WEEK", null, null, null, null, null))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> moodLogService.getMyMoodLogs(
                "24520579", null, null, null, null, null, LocalDate.of(2026, 11, 1), null))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> moodLogService.getMyMoodLogs(
                "24520579", null, null, null, null, null, null, LocalDate.of(2026, 11, 10)))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> moodLogService.getMyMoodLogs(
                "24520579", null, null, null, null, null, LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 10)))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> moodLogService.getMyMoodLogs(
                "24520579", null, "YEAR", null, null, null, null, null))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> moodLogService.getMyMoodLogs(
                "24520579", null, "MONTH", null, 2026, 13, null, null))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> moodLogService.getMyMoodLogs(
                "24520579", null, "MONTH", null, 1800, 5, null, null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Kiểm tra các nhánh biên của MoodLevel, constructor 3 tham số và MoodLogResponse")
    void moodLevelAndResponseEdgeCases() {
        MoodLogServiceImpl defaultClockService = new MoodLogServiceImpl(
                moodLogRepository, studentRepository, userRepository
        );
        assertThat(defaultClockService).isNotNull();

        assertThat(MoodLevel.fromLevel(null)).isEmpty();
        assertThat(MoodLevel.fromLevel(0)).isEmpty();
        assertThat(MoodLevel.fromLevel(1)).contains(MoodLevel.VERY_BAD);
        assertThat(MoodLevel.labelOf(2)).isEqualTo("Tệ");
        assertThat(MoodLevel.labelOf(99)).isNull();
        assertThat(MoodLevel.isValid(5)).isTrue();
        assertThat(MoodLevel.isValid(6)).isFalse();

        assertThat(MoodLogResponse.fromEntity(null)).isNull();

        Student studentWithBlankCode = Student.builder()
                .id(2L)
                .user(studentUser)
                .studentCode("   ")
                .build();
        MoodLog logWithoutTime = MoodLog.builder()
                .id(50L)
                .student(studentWithBlankCode)
                .moodLevel(1)
                .logTime(null)
                .build();
        MoodLogResponse resFallback = MoodLogResponse.fromEntity(logWithoutTime);
        assertThat(resFallback.getStudentCode()).isEqualTo("24520579");
        assertThat(resFallback.getMoodLabel()).isEqualTo("Rất tệ");
        assertThat(resFallback.getLogDate()).isNull();

        MoodLog logWithoutStudent = MoodLog.builder().id(51L).moodLevel(3).build();
        MoodLogResponse resNoStudent = MoodLogResponse.fromEntity(logWithoutStudent);
        assertThat(resNoStudent.getStudentId()).isNull();
        assertThat(resNoStudent.getStudentCode()).isNull();
        assertThat(resNoStudent.getStudentName()).isNull();
    }
}
