package com.unicare.application.service.impl;

import com.unicare.application.dto.response.Dass21ResultResponse;
import com.unicare.application.dto.response.Dass21ScorePointResponse;
import com.unicare.application.dto.response.MoodLogHistoryResponse;
import com.unicare.application.dto.response.MoodLogResponse;
import com.unicare.application.dto.response.StudentMentalHealthHistoryResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.service.DoctorMentalHealthService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.Dass21Result;
import com.unicare.domain.entity.MoodLog;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.repository.Dass21ResultRepository;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Triển khai nghiệp vụ tra cứu Lịch sử kết quả DASS-21 & Nhật ký Tâm trạng của Sinh viên
 * phục vụ Bác sĩ / Chuyên gia tâm lý trong ca khám / tư vấn.
 */
@Service
public class DoctorMentalHealthServiceImpl implements DoctorMentalHealthService {

    public static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final String PERIOD_WEEK = "WEEK";
    public static final String PERIOD_MONTH = "MONTH";
    public static final String PERIOD_ALL = "ALL";
    public static final String PERIOD_CUSTOM = "CUSTOM";

    private static final Comparator<Dass21Result> CHRONOLOGICAL_COMPARATOR = Comparator
            .comparing(Dass21Result::getTestDate, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(Dass21Result::getId, Comparator.nullsFirst(Comparator.naturalOrder()));

    private final Dass21ResultRepository dass21ResultRepository;
    private final MoodLogRepository moodLogRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final Clock clock;

    @Autowired
    public DoctorMentalHealthServiceImpl(
            Dass21ResultRepository dass21ResultRepository,
            MoodLogRepository moodLogRepository,
            UserRepository userRepository,
            StudentRepository studentRepository
    ) {
        this(dass21ResultRepository, moodLogRepository, userRepository, studentRepository, Clock.system(VIETNAM_ZONE));
    }

    public DoctorMentalHealthServiceImpl(
            Dass21ResultRepository dass21ResultRepository,
            MoodLogRepository moodLogRepository,
            UserRepository userRepository,
            StudentRepository studentRepository,
            Clock clock
    ) {
        this.dass21ResultRepository = dass21ResultRepository;
        this.moodLogRepository = moodLogRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.clock = clock != null ? clock : Clock.system(VIETNAM_ZONE);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentMentalHealthHistoryResponse getStudentMentalHealthHistory(
            String currentUsername,
            Long targetStudentId,
            String period,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        User targetUser = authorizeDoctorAndResolveStudent(currentUsername, targetStudentId);
        validateOptionalDateOrder(fromDate, toDate);

        Long studentId = targetUser.getId();
        Student student = studentRepository.findByIdWithDetails(studentId)
                .or(() -> studentRepository.findById(studentId))
                .orElse(null);

        StudentPersonalInfoResponse studentInfo = StudentPersonalInfoResponse.fromEntities(targetUser, student);

        List<Dass21Result> dassEntities = queryDass21Entities(studentId, fromDate, toDate);
        List<Dass21ResultResponse> dassResultsDesc = dassEntities.stream()
                .map(Dass21ResultResponse::fromEntity)
                .toList();
        Dass21ResultResponse latestDass21 = dassResultsDesc.isEmpty() ? null : dassResultsDesc.get(0);
        List<Dass21ScorePointResponse> dassScoreTrendAsc = dassEntities.stream()
                .sorted(CHRONOLOGICAL_COMPARATOR)
                .map(Dass21ScorePointResponse::fromEntity)
                .toList();

        MoodLogHistoryResponse moodHistory = buildMoodHistory(studentId, period, fromDate, toDate);

        return new StudentMentalHealthHistoryResponse(
                studentInfo,
                dassResultsDesc.size(),
                latestDass21,
                dassScoreTrendAsc,
                dassResultsDesc,
                moodHistory
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dass21ResultResponse> getStudentDass21History(
            String currentUsername,
            Long targetStudentId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        User targetUser = authorizeDoctorAndResolveStudent(currentUsername, targetStudentId);
        validateOptionalDateOrder(fromDate, toDate);

        return queryDass21Entities(targetUser.getId(), fromDate, toDate).stream()
                .map(Dass21ResultResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MoodLogHistoryResponse getStudentMoodLogsForDoctor(
            String currentUsername,
            Long targetStudentId,
            String period,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        User targetUser = authorizeDoctorAndResolveStudent(currentUsername, targetStudentId);
        validateOptionalDateOrder(fromDate, toDate);

        return buildMoodHistory(targetUser.getId(), period, fromDate, toDate);
    }

    private User authorizeDoctorAndResolveStudent(String currentUsername, Long targetStudentId) {
        if (currentUsername == null || currentUsername.isBlank()) {
            throw new AccessDeniedException("Yêu cầu đăng nhập để thực hiện thao tác này");
        }

        User currentUser = userRepository.findByUsername(currentUsername.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tài khoản đang đăng nhập: " + currentUsername));

        if (currentUser.getRole() == Role.BAN_QUAN_LY) {
            throw new AccessDeniedException(
                    "Ban quản lý không được phép truy cập lịch sử DASS-21 và nhật ký tâm trạng cá nhân của sinh viên");
        }

        if (currentUser.getRole() != Role.NHAN_VIEN_Y_TE) {
            throw new AccessDeniedException(
                    "Chỉ Bác sĩ / Chuyên gia tâm lý mới có quyền tra cứu lịch sử DASS-21 và nhật ký tâm trạng của sinh viên");
        }

        if (targetStudentId == null) {
            throw new BadRequestException(
                    "Vui lòng truyền mã ID sinh viên (studentId) để tra cứu lịch sử tâm lý");
        }

        User targetUser = userRepository.findById(targetStudentId)
                .or(() -> userRepository.findByUsername(String.valueOf(targetStudentId)))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy sinh viên với mã ID/MSSV: " + targetStudentId));

        if (targetUser.getRole() != Role.SINH_VIEN) {
            throw new BadRequestException(
                    "Tài khoản mục tiêu (ID: " + targetStudentId + ") không phải là sinh viên");
        }

        return targetUser;
    }

    private List<Dass21Result> queryDass21Entities(Long studentId, LocalDate fromDate, LocalDate toDate) {
        List<Dass21Result> rawList;
        if (fromDate != null || toDate != null) {
            LocalDate effectiveFrom = fromDate != null ? fromDate : LocalDate.of(2000, 1, 1);
            LocalDate effectiveTo = toDate != null ? toDate : LocalDate.now(clock);
            OffsetDateTime startTime = effectiveFrom.atStartOfDay(VIETNAM_ZONE).toOffsetDateTime();
            OffsetDateTime endTime = effectiveTo.plusDays(1).atStartOfDay(VIETNAM_ZONE).toOffsetDateTime();
            rawList = dass21ResultRepository.findByStudentIdAndTestDateRange(studentId, startTime, endTime);
        } else {
            rawList = dass21ResultRepository.findByStudentIdWithStudentOrderByTestDateDesc(studentId);
        }
        return rawList != null ? rawList : Collections.emptyList();
    }

    private MoodLogHistoryResponse buildMoodHistory(
            Long studentId,
            String period,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        String effectivePeriod;
        LocalDate resolvedFrom;
        LocalDate resolvedTo;
        List<MoodLog> entities;

        if (fromDate != null || toDate != null) {
            effectivePeriod = PERIOD_CUSTOM;
            resolvedFrom = fromDate != null ? fromDate : LocalDate.of(2000, 1, 1);
            resolvedTo = toDate != null ? toDate : LocalDate.now(clock);
            OffsetDateTime startTime = resolvedFrom.atStartOfDay(VIETNAM_ZONE).toOffsetDateTime();
            OffsetDateTime endTime = resolvedTo.plusDays(1).atStartOfDay(VIETNAM_ZONE).toOffsetDateTime();
            entities = moodLogRepository.findByStudentIdAndLogTimeRange(studentId, startTime, endTime);
        } else {
            String normalized = (period == null || period.isBlank())
                    ? PERIOD_MONTH
                    : period.trim().toUpperCase(Locale.ROOT);

            LocalDate today = LocalDate.now(clock);
            switch (normalized) {
                case PERIOD_WEEK, "LAST_7_DAYS", "7D" -> {
                    effectivePeriod = PERIOD_WEEK;
                    resolvedFrom = today.minusDays(6);
                    resolvedTo = today;
                    entities = moodLogRepository.findByStudentIdAndLogTimeRange(
                            studentId,
                            resolvedFrom.atStartOfDay(VIETNAM_ZONE).toOffsetDateTime(),
                            resolvedTo.plusDays(1).atStartOfDay(VIETNAM_ZONE).toOffsetDateTime()
                    );
                }
                case PERIOD_MONTH, "LAST_30_DAYS", "30D" -> {
                    effectivePeriod = PERIOD_MONTH;
                    resolvedFrom = today.minusDays(29);
                    resolvedTo = today;
                    entities = moodLogRepository.findByStudentIdAndLogTimeRange(
                            studentId,
                            resolvedFrom.atStartOfDay(VIETNAM_ZONE).toOffsetDateTime(),
                            resolvedTo.plusDays(1).atStartOfDay(VIETNAM_ZONE).toOffsetDateTime()
                    );
                }
                case PERIOD_ALL -> {
                    effectivePeriod = PERIOD_ALL;
                    entities = moodLogRepository.findAllByStudentIdWithStudentOrderByLogTimeDesc(studentId);
                    resolvedFrom = null;
                    resolvedTo = null;
                }
                default -> throw new BadRequestException(
                        "Giá trị period không hợp lệ: " + period + ". Chỉ chấp nhận WEEK, MONTH hoặc ALL");
            }
        }

        List<MoodLogResponse> logs = (entities == null || entities.isEmpty())
                ? Collections.emptyList()
                : entities.stream().map(MoodLogResponse::fromEntity).toList();

        if (PERIOD_ALL.equals(effectivePeriod) && !logs.isEmpty()) {
            resolvedTo = logs.get(0).getLogDate();
            resolvedFrom = logs.get(logs.size() - 1).getLogDate();
        }

        return MoodLogHistoryResponse.builder()
                .period(effectivePeriod)
                .fromDate(resolvedFrom)
                .toDate(resolvedTo)
                .totalLogs(logs.size())
                .averageMoodLevel(calculateAverageMoodLevel(logs))
                .logs(logs)
                .build();
    }

    private void validateOptionalDateOrder(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new BadRequestException("Ngày kết thúc không được nhỏ hơn ngày bắt đầu");
        }
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
}
