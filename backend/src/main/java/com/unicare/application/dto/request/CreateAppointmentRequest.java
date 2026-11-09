package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO nhận dữ liệu yêu cầu đặt lịch hẹn khám bệnh mới (POST /api/appointments).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAppointmentRequest {

    @NotNull(message = "Vui lòng chọn ca khám (shiftId)")
    @JsonAlias({"shift_id", "caKhamId", "slotId"})
    private Long shiftId;

    @Size(max = 1000, message = "Triệu chứng / lý do khám không được vượt quá 1000 ký tự")
    @JsonAlias({"reason_symptoms", "symptoms", "reason", "lyDoKham", "trieuChung"})
    private String reasonSymptoms;
}
