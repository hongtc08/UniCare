package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HealthInsuranceRequest {

    @NotBlank(message = "Mã thẻ BHYT không được để trống")
    @Size(min = 10, max = 15, message = "Mã thẻ BHYT phải có độ dài từ 10 đến 15 ký tự")
    @Pattern(
            regexp = "^(?i)(?:\\d{10}|[A-Z]{2}\\d{13})$",
            message = "Mã thẻ BHYT không đúng định dạng chuẩn Việt Nam (gồm 10 chữ số hoặc 15 ký tự: 2 chữ cái đầu và 13 chữ số)"
    )
    private String cardNumber;

    @NotNull(message = "Mã ID sinh viên (studentId) không được để trống")
    private Long studentId;

    @NotNull(message = "Ngày bắt đầu hiệu lực không được để trống")
    private LocalDate effectiveDate;

    @NotNull(message = "Ngày hết hạn không được để trống")
    private LocalDate expirationDate;

    @NotBlank(message = "Nơi đăng ký khám chữa bệnh ban đầu không được để trống")
    @Size(max = 150, message = "Nơi đăng ký khám chữa bệnh ban đầu không được vượt quá 150 ký tự")
    private String initialHospital;

    public HealthInsuranceRequest() {
    }

    public HealthInsuranceRequest(
            String cardNumber,
            Long studentId,
            LocalDate effectiveDate,
            LocalDate expirationDate,
            String initialHospital
    ) {
        this.cardNumber = cardNumber;
        this.studentId = studentId;
        this.effectiveDate = effectiveDate;
        this.expirationDate = expirationDate;
        this.initialHospital = initialHospital;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getInitialHospital() {
        return initialHospital;
    }

    public void setInitialHospital(String initialHospital) {
        this.initialHospital = initialHospital;
    }
}
