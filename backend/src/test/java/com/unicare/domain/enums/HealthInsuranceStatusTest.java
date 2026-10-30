package com.unicare.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HealthInsuranceStatusTest {

    private final LocalDate today = LocalDate.of(2026, 10, 30);

    @Test
    @DisplayName("Ngày hết hạn < hôm nay -> Trạng thái DA_HET_HAN")
    void calculate_Expired_ReturnsDaHetHan() {
        LocalDate expiredYesterday = today.minusDays(1);
        LocalDate expiredLongAgo = today.minusMonths(6);

        assertThat(HealthInsuranceStatus.calculate(expiredYesterday, today))
                .isEqualTo(HealthInsuranceStatus.DA_HET_HAN);
        assertThat(HealthInsuranceStatus.DA_HET_HAN.getDescription()).isEqualTo("Đã hết hạn");

        assertThat(HealthInsuranceStatus.calculate(expiredLongAgo, today))
                .isEqualTo(HealthInsuranceStatus.DA_HET_HAN);
    }

    @Test
    @DisplayName("Còn hạn < 30 ngày (bao gồm ngày hôm nay) -> Trạng thái SAP_HET_HAN")
    void calculate_ExpiresInLessThan30Days_ReturnsSapHetHan() {
        LocalDate expiresToday = today;
        LocalDate expiresTomorrow = today.plusDays(1);
        LocalDate expires29DaysLater = today.plusDays(29);

        assertThat(HealthInsuranceStatus.calculate(expiresToday, today))
                .isEqualTo(HealthInsuranceStatus.SAP_HET_HAN);
        assertThat(HealthInsuranceStatus.SAP_HET_HAN.getDescription()).isEqualTo("Sắp hết hạn");

        assertThat(HealthInsuranceStatus.calculate(expiresTomorrow, today))
                .isEqualTo(HealthInsuranceStatus.SAP_HET_HAN);

        assertThat(HealthInsuranceStatus.calculate(expires29DaysLater, today))
                .isEqualTo(HealthInsuranceStatus.SAP_HET_HAN);
    }

    @Test
    @DisplayName("Còn hạn >= 30 ngày -> Trạng thái DANG_HIEU_LUC")
    void calculate_ExpiresIn30DaysOrMore_ReturnsDangHieuLuc() {
        LocalDate expiresExactly30DaysLater = today.plusDays(30);
        LocalDate expires1YearLater = today.plusYears(1);

        assertThat(HealthInsuranceStatus.calculate(expiresExactly30DaysLater, today))
                .isEqualTo(HealthInsuranceStatus.DANG_HIEU_LUC);
        assertThat(HealthInsuranceStatus.DANG_HIEU_LUC.getDescription()).isEqualTo("Đang hiệu lực");

        assertThat(HealthInsuranceStatus.calculate(expires1YearLater, today))
                .isEqualTo(HealthInsuranceStatus.DANG_HIEU_LUC);
    }

    @Test
    @DisplayName("Ngày hết hạn là null -> Mặc định trả về DA_HET_HAN")
    void calculate_NullDate_ReturnsDaHetHan() {
        assertThat(HealthInsuranceStatus.calculate(null, today))
                .isEqualTo(HealthInsuranceStatus.DA_HET_HAN);
    }
}
