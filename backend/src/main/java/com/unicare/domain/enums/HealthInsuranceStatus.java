package com.unicare.domain.enums;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public enum HealthInsuranceStatus {
    DANG_HIEU_LUC("Đang hiệu lực"),
    SAP_HET_HAN("Sắp hết hạn"),
    DA_HET_HAN("Đã hết hạn");

    private final String description;

    HealthInsuranceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Tính toán trạng thái thẻ BHYT động:
     * - Ngày hết hạn < hôm nay -> DA_HET_HAN
     * - Còn hạn < 30 ngày -> SAP_HET_HAN
     * - Còn hạn >= 30 ngày -> DANG_HIEU_LUC
     */
    public static HealthInsuranceStatus calculate(LocalDate expirationDate, LocalDate today) {
        if (expirationDate == null) {
            return DA_HET_HAN;
        }
        LocalDate referenceDate = today != null ? today : LocalDate.now();

        if (expirationDate.isBefore(referenceDate)) {
            return DA_HET_HAN;
        }

        long daysRemaining = ChronoUnit.DAYS.between(referenceDate, expirationDate);
        if (daysRemaining < 30) {
            return SAP_HET_HAN;
        }

        return DANG_HIEU_LUC;
    }
}
