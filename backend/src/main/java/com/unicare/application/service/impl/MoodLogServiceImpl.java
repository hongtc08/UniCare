package com.unicare.application.service.impl;

import com.unicare.application.dto.request.MoodLogRequest;
import com.unicare.application.dto.response.MoodLogHistoryResponse;
import com.unicare.application.dto.response.MoodLogResponse;
import com.unicare.application.service.MoodLogService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.MoodLog;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.MoodLevel;
import com.unicare.domain.enums.Role;
import com.unicare.domain.repository.MoodLogRepository;
import com.unicare.domain.repository.StudentRepository;
import com.unicare.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Triển khai nghiệp vụ Nhật ký Tâm trạng Hằng ngày (Mood Log) cho Sinh viên.
 */
@Service
public class MoodLogServiceImpl implements MoodLogService {

    public static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final int MAX_NOTES_LENGTH = 2000;
    public static final String PERIOD_WEEK = "WEEK";
    public static final String PERIOD_MONTH = "MONTH";
    public static final String PERIOD_CUSTOM = "CUSTOM";

    private static final long FUTURE_TOLERANCE_MINUTES = 5L;

    private final MoodLogRepository moodLogRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Autowired
    public MoodLogServiceImpl(
            MoodLogRepository moodLogRepository,
            StudentRepository studentRepository,
            UserRepository userRepository) {
        this(moodLogRepository, studentRepository, userRepository, Clock.system(VIETNAM_ZONE));
    }

    public MoodLogServiceImpl(
            MoodLogRepository moodLogRepository,
            StudentRepository studentRepository,
            UserRepository userRepository,
            Clock clock) {
        this.moodLogRepository = moodLogRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.clock = clock != null ? clock : Clock.system(VIETNAM_ZONE);
    }

    @Override
    @Transactional
    public MoodLogResponse createMoodLog(MoodLogRequest request, String currentUsername) {
        String cleanNotes = validateAndNormalizeRequest(request);
        User currentUser = resolveCurrentStudentUser(currentUsername);

        OffsetDateTime effectiveLogTime = resolveEffectiveLogTime(request.getLogTime(), OffsetDateTime.now(clock));
        LocalDate targetDate = effectiveLogTime.atZoneSameInstant(VIETNAM_ZONE).toLocalDate();
        DayRange dayRange = dayRangeOf(targetDate);

        if (moodLogRepository.existsByStudentIdAndLogTimeGreaterThanEqualAndLogTimeLessThan(
                currentUser.getId(), dayRange.start(), dayRange.end())) {
            throw new DuplicateResourceException(
                    "Bạn đã ghi nhật ký tâm trạng trong ngày " + targetDate + ". Vui lòng cập nhật bản ghi hiện có.");
        }

        Student student = resolveOrCreateStudent(currentUser);

        MoodLog moodLog = MoodLog.builder()
                .student(student)
                .moodLevel(request.getMoodLevel())
                .notes(cleanNotes)
                .logTime(effectiveLogTime)
                .build();

        MoodLog saved = moodLogRepository.save(moodLog);
        return MoodLogResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public MoodLogResponse updateMoodLog(Long id, MoodLogRequest request, String currentUsername) {
        if (id == null) {
            throw new BadRequestException("Vui lòng cung cấp mã ID nhật ký tâm trạng cần cập nhật");
        }
        String cleanNotes = validateAndNormalizeRequest(request);
        User currentUser = resolveCurrentStudentUser(currentUsername);

        MoodLog moodLog = moodLogRepository.findByIdWithStudent(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhật ký tâm trạng với mã ID: " + id));

        validateOwnership(moodLog, currentUser);

        if (request.getLogTime() != null) {
            OffsetDateTime effectiveLogTime = resolveEffectiveLogTime(request.getLogTime(), OffsetDateTime.now(clock));
            LocalDate targetDate = effectiveLogTime.atZoneSameInstant(VIETNAM_ZONE).toLocalDate();
            DayRange dayRange = dayRangeOf(targetDate);

            if (moodLogRepository.existsByStudentIdAndLogTimeGreaterThanEqualAndLogTimeLessThanAndIdNot(
                    currentUser.getId(), dayRange.start(), dayRange.end(), id)) {
                throw new DuplicateResourceException(
                        "Bạn đã có nhật ký tâm trạng khác trong ngày " + targetDate
                                + ". Vui lòng chọn ngày khác hoặc cập nhật trực tiếp bản ghi đó.");
            }
            moodLog.setLogTime(effectiveLogTime);
        }

        moodLog.setMoodLevel(request.getMoodLevel());
        moodLog.setNotes(cleanNotes);

        MoodLog saved = moodLogRepository.save(moodLog);
        return MoodLogResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public MoodLogResponse updateMoodLogByRequest(MoodLogRequest request, String currentUsername) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu nhật ký tâm trạng không được để trống");
        }
        if (request.getId() != null) {
            return updateMoodLog(request.getId(), request, currentUsername);
        }

        String cleanNotes = validateAndNormalizeRequest(request);
        User currentUser = resolveCurrentStudentUser(currentUsername);

        OffsetDateTime effectiveLogTime = null;
        LocalDate targetDate;
        if (request.getLogTime() != null) {
            effectiveLogTime = resolveEffectiveLogTime(request.getLogTime(), OffsetDateTime.now(clock));
            targetDate = effectiveLogTime.atZoneSameInstant(VIETNAM_ZONE).toLocalDate();
        } else {
            targetDate = LocalDate.now(clock);
        }

        DayRange dayRange = dayRangeOf(targetDate);
        MoodLog moodLog = moodLogRepository.findByStudentIdAndLogTimeRange(
                        currentUser.getId(), dayRange.start(), dayRange.end())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy nhật ký tâm trạng trong ngày " + targetDate + " để cập nhật"));

        validateOwnership(moodLog, currentUser);

        moodLog.setMoodLevel(request.getMoodLevel());
        moodLog.setNotes(cleanNotes);
        if (effectiveLogTime != null) {
            moodLog.setLogTime(effectiveLogTime);
        }

        MoodLog saved = moodLogRepository.save(moodLog);
        return MoodLogResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MoodLogHistoryResponse getMyMoodLogs(
            String currentUsername,
            Long requestedStudentId,
            String period,
            LocalDate date,
            Integer year,
            Integer month,
            LocalDate fromDate,
            LocalDate toDate) {
        User currentUser = resolveCurrentStudentUser(currentUsername);

        if (requestedStudentId != null && !requestedStudentId.equals(currentUser.getId())) {
            throw new AccessDeniedException("Sinh viên chỉ được phép xem nhật ký tâm trạng của chính mình");
        }

        QueryRange range = resolveQueryRange(period, date, year, month, fromDate, toDate);
        OffsetDateTime startTime = range.fromDate().atStartOfDay(VIETNAM_ZONE).toOffsetDateTime();
        OffsetDateTime endTime = range.toDate().plusDays(1).atStartOfDay(VIETNAM_ZONE).toOffsetDateTime();

        List<MoodLog> entities = moodLogRepository.findByStudentIdAndLogTimeRange(
                currentUser.getId(), startTime, endTime);

        List<MoodLogResponse> logs = (entities == null || entities.isEmpty())
                ? Collections.emptyList()
                : entities.stream().map(MoodLogResponse::fromEntity).toList();

        return MoodLogHistoryResponse.builder()
                .period(range.period())
                .fromDate(range.fromDate())
                .toDate(range.toDate())
                .totalLogs(logs.size())
                .averageMoodLevel(calculateAverageMoodLevel(logs))
                .logs(logs)
                .build();
    }

    private String validateAndNormalizeRequest(MoodLogRequest request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu nhật ký tâm trạng không được để trống");
        }
        if (request.getMoodLevel() == null) {
            throw new BadRequestException("Mức tâm trạng không được để trống");
        }
        if (!MoodLevel.isValid(request.getMoodLevel())) {
            throw new BadRequestException("Mức tâm trạng phải nằm trong thang điểm từ 1 đến 5");
        }
        if (request.getNotes() == null) {
            return null;
        }
        String trimmed = request.getNotes().trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > MAX_NOTES_LENGTH) {
            throw new BadRequestException("Ghi chú cảm xúc không được vượt quá " + MAX_NOTES_LENGTH + " ký tự");
        }
        return trimmed;
    }

    private User resolveCurrentStudentUser(String currentUsername) {
        if (currentUsername == null || currentUsername.isBlank()) {
            throw new AccessDeniedException("Yêu cầu đăng nhập để thực hiện thao tác này");
        }
        User currentUser = userRepository.findByUsername(currentUsername.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + currentUsername));

        if (currentUser.getRole() != Role.SINH_VIEN) {
            throw new AccessDeniedException("Chỉ tài khoản sinh viên mới có quyền thao tác nhật ký tâm trạng cá nhân");
        }
        return currentUser;
    }

    private void validateOwnership(MoodLog moodLog, User currentUser) {
        if (moodLog.getStudent() == null || !currentUser.getId().equals(moodLog.getStudent().getId())) {
            throw new AccessDeniedException("Sinh viên chỉ được phép cập nhật nhật ký tâm trạng của chính mình");
        }
    }

    private Student resolveOrCreateStudent(User user) {
        return studentRepository.findById(user.getId())
                .orElseGet(() -> {
                    Student newStudent = new Student(user.getId(), user, user.getUsername(), null);
                    return studentRepository.save(newStudent);
                });
    }

    private OffsetDateTime resolveEffectiveLogTime(OffsetDateTime requestedLogTime, OffsetDateTime now) {
        if (requestedLogTime == null) {
            return now;
        }
        if (requestedLogTime.isAfter(now.plusMinutes(FUTURE_TOLERANCE_MINUTES))) {
            throw new BadRequestException("Thời điểm ghi nhật ký không được ở trong tương lai");
        }
        return requestedLogTime.atZoneSameInstant(VIETNAM_ZONE).toOffsetDateTime();
    }

    private DayRange dayRangeOf(LocalDate date) {
        OffsetDateTime start = date.atStartOfDay(VIETNAM_ZONE).toOffsetDateTime();
        OffsetDateTime end = date.plusDays(1).atStartOfDay(VIETNAM_ZONE).toOffsetDateTime();
        return new DayRange(start, end);
    }

    private QueryRange resolveQueryRange(
            String period,
            LocalDate date,
            Integer year,
            Integer month,
            LocalDate fromDate,
            LocalDate toDate) {
        if (fromDate != null || toDate != null) {
            if (fromDate == null || toDate == null) {
                throw new BadRequestException("Vui lòng truyền đầy đủ cả ngày bắt đầu (fromDate) và ngày kết thúc (toDate)");
            }
            if (toDate.isBefore(fromDate)) {
                throw new BadRequestException("Ngày kết thúc không được nhỏ hơn ngày bắt đầu");
            }
            return new QueryRange(PERIOD_CUSTOM, fromDate, toDate);
        }

        String normalizedPeriod = (period == null || period.isBlank())
                ? PERIOD_WEEK
                : period.trim().toUpperCase(Locale.ROOT);

        if (PERIOD_WEEK.equals(normalizedPeriod)) {
            LocalDate refDate = date != null ? date : LocalDate.now(clock);
            LocalDate monday = refDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate sunday = monday.plusDays(6);
            return new QueryRange(PERIOD_WEEK, monday, sunday);
        }

        if (PERIOD_MONTH.equals(normalizedPeriod)) {
            if (month != null && (month < 1 || month > 12)) {
                throw new BadRequestException("Tháng không hợp lệ (chỉ chấp nhận từ 1 đến 12)");
            }
            if (year != null && (year < 1900 || year > 2100)) {
                throw new BadRequestException("Năm không hợp lệ (chỉ chấp nhận từ 1900 đến 2100)");
            }
            LocalDate baseDate = date != null ? date : LocalDate.now(clock);
            int targetYear = year != null ? year : baseDate.getYear();
            int targetMonth = month != null ? month : baseDate.getMonthValue();
            YearMonth yearMonth = YearMonth.of(targetYear, targetMonth);
            return new QueryRange(PERIOD_MONTH, yearMonth.atDay(1), yearMonth.atEndOfMonth());
        }

        throw new BadRequestException("Giá trị period không hợp lệ. Chỉ chấp nhận WEEK hoặc MONTH");
    }

    private Double calculateAverageMoodLevel(List<MoodLogResponse> logs) {
        if (logs == null || logs.isEmpty()) {
            return null;
        }
        long sum = 0L;
        int count = 0;
        for (MoodLogResponse log : logs) {
            if (log.getMoodLevel() != null) {
                sum += log.getMoodLevel();
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private record DayRange(OffsetDateTime start, OffsetDateTime end) {
    }

    private record QueryRange(String period, LocalDate fromDate, LocalDate toDate) {
    }
}
