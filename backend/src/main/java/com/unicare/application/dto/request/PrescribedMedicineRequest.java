package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Thông tin một dòng thuốc kê đơn.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrescribedMedicineRequest {

    @NotBlank(message = "Tên thuốc không được để trống")
    @Size(max = 100, message = "Tên thuốc không được vượt quá 100 ký tự")
    private String medicineName;

    @NotNull(message = "Số lượng thuốc không được để trống")
    @Min(value = 1, message = "Số lượng thuốc phải lớn hơn hoặc bằng 1")
    private Integer quantity;

    @NotBlank(message = "Hướng dẫn liều dùng không được để trống")
    @Size(max = 200, message = "Hướng dẫn liều dùng không được vượt quá 200 ký tự")
    private String dosageInstruction;
}
