package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * DTO nhận dữ liệu tạo mới hoặc cập nhật nhật ký tâm trạng hằng ngày của sinh viên.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoodLogRequest {

    private Long id;

    @NotNull(message = "Mức tâm trạng không được để trống")
    @Min(value = 1, message = "Mức tâm trạng phải nằm trong thang điểm từ 1 đến 5")
    @Max(value = 5, message = "Mức tâm trạng phải nằm trong thang điểm từ 1 đến 5")
    @JsonAlias({"mood_level", "level"})
    private Integer moodLevel;

    @Size(max = 2000, message = "Ghi chú cảm xúc không được vượt quá 2000 ký tự")
    @JsonAlias({"note", "ghiChu"})
    private String notes;

    @JsonAlias({"log_time", "loggedAt"})
    private OffsetDateTime logTime;
}
