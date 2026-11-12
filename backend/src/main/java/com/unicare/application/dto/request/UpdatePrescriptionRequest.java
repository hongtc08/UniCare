package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Danh sách thuốc kê đơn cần cập nhật (có thể null)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePrescriptionRequest {

    @Builder.Default
    private List<@Valid PrescribedMedicineRequest> medicines = new ArrayList<>();
}
