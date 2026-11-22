package com.unicare.application.service;

import com.unicare.application.dto.request.MoodLogRequest;
import com.unicare.application.dto.response.MoodLogHistoryResponse;
import com.unicare.application.dto.response.MoodLogResponse;

import java.time.LocalDate;

/**
 * Service nghiệp vụ quản lý Nhật ký Tâm trạng Hằng ngày (Mood Log) của Sinh viên.
 */
public interface MoodLogService {

    MoodLogResponse createMoodLog(MoodLogRequest request, String currentUsername);

    MoodLogResponse updateMoodLog(Long id, MoodLogRequest request, String currentUsername);

    MoodLogResponse updateMoodLogByRequest(MoodLogRequest request, String currentUsername);

    MoodLogHistoryResponse getMyMoodLogs(
            String currentUsername,
            Long requestedStudentId,
            String period,
            LocalDate date,
            Integer year,
            Integer month,
            LocalDate fromDate,
            LocalDate toDate
    );
}
