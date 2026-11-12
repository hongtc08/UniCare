/**
 * DTO trả về thông tin tóm tắt của mỗi lần khám bệnh trước đó trong hồ sơ y tế tổng hợp của sinh viên.
 */
package com.unicare.application.dto.response;

import com.unicare.domain.entity.Appointment;
import com.unicare.domain.entity.DiseaseGroup;
import com.unicare.domain.entity.MedicalCheckup;
import com.unicare.domain.entity.MedicalStaff;

import java.time.OffsetDateTime;

public class MedicalCheckupSummaryResponse {

    private Long id;
    private String checkupCode;
    private String checkupType;
    private Boolean isEmergency;
    private String periodicResult;
    private String symptoms;
    private String diagnosisNotes;
    private String treatmentSuggestions;
    private Integer diseaseGroupId;
    private String diseaseGroupName;
    private Long staffId;
    private String staffCode;
    private String doctorName;
    private String doctorTitle;
    private String doctorSpecialty;
    private Long appointmentId;
    private String appointmentCode;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime completedAt;

    public MedicalCheckupSummaryResponse() {
    }

    public static MedicalCheckupSummaryResponse fromEntity(MedicalCheckup entity) {
        if (entity == null) {
            return null;
        }
        MedicalCheckupSummaryResponse response = new MedicalCheckupSummaryResponse();
        response.setId(entity.getId());
        response.setCheckupCode(entity.getCheckupCode());
        response.setCheckupType(entity.getCheckupType());
        response.setIsEmergency(entity.getIsEmergency());
        response.setPeriodicResult(entity.getPeriodicResult());
        response.setSymptoms(entity.getSymptoms());
        response.setDiagnosisNotes(entity.getDiagnosisNotes());
        response.setTreatmentSuggestions(entity.getTreatmentSuggestions());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setCompletedAt(entity.getCompletedAt());

        DiseaseGroup diseaseGroup = entity.getDiseaseGroup();
        if (diseaseGroup != null) {
            response.setDiseaseGroupId(diseaseGroup.getId());
            response.setDiseaseGroupName(diseaseGroup.getGroupName());
        }

        MedicalStaff staff = entity.getStaff();
        if (staff != null) {
            response.setStaffId(staff.getId());
            response.setStaffCode(staff.getStaffCode());
            response.setDoctorTitle(staff.getTitle());
            response.setDoctorSpecialty(staff.getSpecialty());
            if (staff.getUser() != null) {
                response.setDoctorName(staff.getUser().getFullName());
            }
        }

        Appointment appointment = entity.getAppointment();
        if (appointment != null) {
            response.setAppointmentId(appointment.getId());
            response.setAppointmentCode(appointment.getAppointmentCode());
        }

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCheckupCode() {
        return checkupCode;
    }

    public void setCheckupCode(String checkupCode) {
        this.checkupCode = checkupCode;
    }

    public String getCheckupType() {
        return checkupType;
    }

    public void setCheckupType(String checkupType) {
        this.checkupType = checkupType;
    }

    public Boolean getIsEmergency() {
        return isEmergency;
    }

    public void setIsEmergency(Boolean isEmergency) {
        this.isEmergency = isEmergency;
    }

    public String getPeriodicResult() {
        return periodicResult;
    }

    public void setPeriodicResult(String periodicResult) {
        this.periodicResult = periodicResult;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosisNotes() {
        return diagnosisNotes;
    }

    public void setDiagnosisNotes(String diagnosisNotes) {
        this.diagnosisNotes = diagnosisNotes;
    }

    public String getTreatmentSuggestions() {
        return treatmentSuggestions;
    }

    public void setTreatmentSuggestions(String treatmentSuggestions) {
        this.treatmentSuggestions = treatmentSuggestions;
    }

    public Integer getDiseaseGroupId() {
        return diseaseGroupId;
    }

    public void setDiseaseGroupId(Integer diseaseGroupId) {
        this.diseaseGroupId = diseaseGroupId;
    }

    public String getDiseaseGroupName() {
        return diseaseGroupName;
    }

    public void setDiseaseGroupName(String diseaseGroupName) {
        this.diseaseGroupName = diseaseGroupName;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorTitle() {
        return doctorTitle;
    }

    public void setDoctorTitle(String doctorTitle) {
        this.doctorTitle = doctorTitle;
    }

    public String getDoctorSpecialty() {
        return doctorSpecialty;
    }

    public void setDoctorSpecialty(String doctorSpecialty) {
        this.doctorSpecialty = doctorSpecialty;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAppointmentCode() {
        return appointmentCode;
    }

    public void setAppointmentCode(String appointmentCode) {
        this.appointmentCode = appointmentCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
