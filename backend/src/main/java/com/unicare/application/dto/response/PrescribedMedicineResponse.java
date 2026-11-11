package com.unicare.application.dto.response;

import com.unicare.domain.entity.PrescribedMedicine;
import lombok.*;

/**
 * Thông tin thuốc kê đơn trả về.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescribedMedicineResponse {

    private Long id;
    private String medicineName;
    private Integer quantity;
    private String dosageInstruction;

    public static PrescribedMedicineResponse fromEntity(PrescribedMedicine entity) {
        if (entity == null) {
            return null;
        }
        return PrescribedMedicineResponse.builder()
                .id(entity.getId())
                .medicineName(entity.getMedicineName())
                .quantity(entity.getQuantity())
                .dosageInstruction(entity.getDosageInstruction())
                .build();
    }
}
