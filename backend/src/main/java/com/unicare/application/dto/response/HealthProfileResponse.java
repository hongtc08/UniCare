/**
 * DTO trả về thông tin hồ sơ sức khỏe sinh học và kết quả tính chỉ số BMI.
 */
package com.unicare.application.dto.response;

import com.unicare.domain.entity.HealthRecord;
import com.unicare.domain.entity.User;

import java.time.OffsetDateTime;

public class HealthProfileResponse {

    private Long id;
    private Long studentId;
    private String username;
    private String fullName;
    private Double height;
    private Double weight;
    private String bloodType;
    private Double bmi;
    private String bmiClassification;
    private String allergies;
    private String underlyingDiseases;
    private String vaccinationHistory;
    private String medicalHistory;
    private OffsetDateTime updatedAt;

    public HealthProfileResponse() {
    }

    public static HealthProfileResponse fromEntity(HealthRecord record) {
        if (record == null) {
            return null;
        }
        HealthProfileResponse response = new HealthProfileResponse();
        response.setId(record.getId());

        User student = record.getStudent();
        if (student != null) {
            response.setStudentId(student.getId());
            response.setUsername(student.getUsername());
            response.setFullName(student.getFullName());
        }

        response.setHeight(record.getHeight() != null ? record.getHeight().doubleValue() : null);
        response.setWeight(record.getWeight() != null ? record.getWeight().doubleValue() : null);
        response.setBloodType(record.getBloodType());
        response.setBmi(record.getBmi() != null ? record.getBmi().doubleValue() : null);
        response.setBmiClassification(record.getBmiClassification());
        response.setAllergies(record.getAllergies());
        response.setUnderlyingDiseases(record.getUnderlyingDiseases());
        response.setVaccinationHistory(record.getVaccinationHistory());
        response.setMedicalHistory(record.getMedicalHistory());
        response.setUpdatedAt(record.getUpdatedAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public Double getBmi() {
        return bmi;
    }

    public void setBmi(Double bmi) {
        this.bmi = bmi;
    }

    public String getBmiClassification() {
        return bmiClassification;
    }

    public void setBmiClassification(String bmiClassification) {
        this.bmiClassification = bmiClassification;
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

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
