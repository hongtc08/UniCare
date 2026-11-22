package com.unicare.presentation.controller;

import com.unicare.application.dto.request.MoodLogRequest;
import com.unicare.application.dto.response.MoodLogHistoryResponse;
import com.unicare.application.dto.response.MoodLogResponse;
import com.unicare.application.service.MoodLogService;
import com.unicare.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;

/**
 * REST Controller cung cấp các API Nhật ký Tâm trạng Hằng ngày (Mood Log) của Sinh viên (/api/mental-health/mood-logs).
 */
@RestController
@RequestMapping("/api/mental-health/mood-logs")
@PreAuthorize("hasRole('SINH_VIEN')")
public class MoodLogController {

    private final MoodLogService moodLogService;

    public MoodLogController(MoodLogService moodLogService) {
        this.moodLogService = moodLogService;
    }

    /**
     * Tạo mới bản ghi nhật ký tâm trạng hằng ngày cho sinh viên đang đăng nhập.
     * POST /api/mental-health/mood-logs
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MoodLogResponse>> createMoodLog(
            @Valid @RequestBody MoodLogRequest request,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        MoodLogResponse response = moodLogService.createMoodLog(request, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ghi nhận nhật ký tâm trạng thành công", response));
    }

    /**
     * Cập nhật bản ghi nhật ký tâm trạng theo mã ID (chỉ chính sinh viên sở hữu mới được sửa).
     * PUT /api/mental-health/mood-logs/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MoodLogResponse>> updateMoodLog(
            @PathVariable Long id,
            @Valid @RequestBody MoodLogRequest request,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        MoodLogResponse response = moodLogService.updateMoodLog(id, request, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhật ký tâm trạng thành công", response));
    }

    /**
     * Cập nhật nhật ký tâm trạng theo ID trong body hoặc theo ngày hiện tại / ngày logTime.
     * PUT /api/mental-health/mood-logs
     */
    @PutMapping
    public ResponseEntity<ApiResponse<MoodLogResponse>> updateMoodLogByRequest(
            @Valid @RequestBody MoodLogRequest request,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        MoodLogResponse response = moodLogService.updateMoodLogByRequest(request, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhật ký tâm trạng thành công", response));
    }

    /**
     * Truy vấn lịch sử nhật ký tâm trạng theo tuần (WEEK), tháng (MONTH) hoặc khoảng ngày (fromDate..toDate)
     * cho chính sinh viên đang đăng nhập.
     * GET /api/mental-health/mood-logs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<MoodLogHistoryResponse>> getMyMoodLogs(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(name = "studentId", required = false) Long studentId,
            @RequestParam(name = "student_id", required = false) Long studentIdSnake,
            Principal principal) {
        if (studentId != null && studentIdSnake != null && !studentId.equals(studentIdSnake)) {
            throw new AccessDeniedException("Sinh viên chỉ được phép xem nhật ký tâm trạng của chính mình");
        }
        Long requestedStudentId = studentId != null ? studentId : studentIdSnake;
        String effectivePeriod = (period != null && !period.isBlank()) ? period : range;
        String currentUsername = principal != null ? principal.getName() : null;

        MoodLogHistoryResponse response = moodLogService.getMyMoodLogs(
                currentUsername,
                requestedStudentId,
                effectivePeriod,
                date,
                year,
                month,
                fromDate,
                toDate
        );
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử tâm trạng thành công", response));
    }
}
