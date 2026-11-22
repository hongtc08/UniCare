package com.unicare.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * DTO trả về lịch sử nhật ký tâm trạng theo tuần, tháng hoặc khoảng ngày tùy chọn kèm thông tin tổng hợp.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodLogHistoryResponse {

    private String period;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int totalLogs;
    private Double averageMoodLevel;

    @Builder.Default
    private List<MoodLogResponse> logs = Collections.emptyList();
}
