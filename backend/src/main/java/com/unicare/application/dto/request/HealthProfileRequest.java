/**
 * DTO nhận dữ liệu yêu cầu tạo mới hoặc cập nhật hồ sơ sức khỏe sinh học.
 */
package com.unicare.application.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class HealthProfileRequest {

    private Long studentId;

    @NotNull(message = "Chiều cao không được để trống")
    @Positive(message = "Chiều cao phải lớn hơn 0")
    @DecimalMax(value = "300.0", message = "Chiều cao không hợp lệ (tối đa 300 cm)")
    private Double height;

    @NotNull(message = "Cân nặng không được để trống")
    @Positive(message = "Cân nặng phải lớn hơn 0")
    @DecimalMax(value = "500.0", message = "Cân nặng không hợp lệ (tối đa 500 kg)")
    private Double weight;

    @Pattern(
            regexp = "^(?i)\\s*(A|B|AB|O)?\\s*$",
            message = "Nhóm máu chỉ chấp nhận các giá trị: A, B, AB, O"
    )
    private String bloodType;

    private String allergies;

    private String underlyingDiseases;

    private String vaccinationHistory;

    private String medicalHistory;

    public HealthProfileRequest() {
    }

    public HealthProfileRequest(
            Double height,
            Double weight,
            String bloodType,
            String allergies,
            String underlyingDiseases,
            String vaccinationHistory
    ) {
        this.height = height;
        this.weight = weight;
        this.bloodType = bloodType;
        this.allergies = allergies;
        this.underlyingDiseases = underlyingDiseases;
        this.vaccinationHistory = vaccinationHistory;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getUnderlyingDiseases() {
        return underlyingDiseases;
    }

    public void setUnderlyingDiseases(String underlyingDiseases) {
        this.underlyingDiseases = underlyingDiseases;
    }

    public String getVaccinationHistory() {
        return vaccinationHistory;
    }

    public void setVaccinationHistory(String vaccinationHistory) {
        this.vaccinationHistory = vaccinationHistory;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
}
