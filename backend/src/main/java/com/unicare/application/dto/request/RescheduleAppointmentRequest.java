package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RescheduleAppointmentRequest {

    @NotNull(message = "Vui lòng chọn ca khám mới (newShiftId)")
    private Long newShiftId;

    @NotBlank(message = "Lý do dời lịch hẹn không được để trống")
    @Size(max = 500, message = "Lý do dời lịch hẹn không được vượt quá 500 ký tự")
    private String reason;
}
