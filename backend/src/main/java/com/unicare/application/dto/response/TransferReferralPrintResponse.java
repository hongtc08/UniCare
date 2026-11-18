/**
 * DTO trả về dữ liệu chuẩn / bản in Giấy giới thiệu chuyển viện (GET /api/medical-records/{id}/transfer/print).
 */
package com.unicare.application.dto.response;

import com.unicare.domain.entity.AcademicClass;
import com.unicare.domain.entity.Faculty;
import com.unicare.domain.entity.HealthInsurance;
import com.unicare.domain.entity.HealthRecord;
import com.unicare.domain.entity.MedicalCheckup;
import com.unicare.domain.entity.MedicalStaff;
import com.unicare.domain.entity.PrescribedMedicine;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.TransferRecord;
import com.unicare.domain.entity.User;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransferReferralPrintResponse {

    public static final String REFERRING_UNIT_NAME = "TRẠM Y TẾ TRƯỜNG ĐẠI HỌC - UNICARE";

    private String referringUnitName = REFERRING_UNIT_NAME;
    private Long transferId;
    private String recordCode;
    private Long checkupId;
    private String checkupCode;
    private String targetHospital;
    private String reasonCategory;
    private String reasonCategoryLabel;
    private String transferReason;
    private String currentCondition;
    private String firstAidNotes;
    private String refusalNotes;
    private OffsetDateTime transferredAt;
    private boolean readOnly = true;

    // Thông tin hành chính & học vụ Sinh viên
    private Long studentId;
    private String studentCode;
    private String studentName;
    private LocalDate dateOfBirth;
    private String gender;
    private String studentPhone;
    private String studentEmail;
    private String classCode;
    private String className;
    private String facultyName;

    // Thông tin Thẻ BHYT
    private String insuranceCardNumber;
    private String insuranceInitialHospital;
    private LocalDate insuranceEffectiveDate;
    private LocalDate insuranceExpirationDate;

    // Thông tin Sinh hiệu & Tiền sử y tế
    private Double height;
    private Double weight;
    private String bloodType;
    private Double bmi;
    private String allergies;
    private String underlyingDiseases;

    // Thông tin Lâm sàng & Phiếu khám tại trường
    private String checkupType;
    private Boolean isEmergency;
    private String emergencyLevel;
    private String emergencyType;
    private String symptoms;
    private String diagnosisNotes;
    private String treatmentSuggestions;
    private String diseaseGroupName;
    private List<PrescribedMedicineResponse> prescribedMedicines = Collections.emptyList();
    private OffsetDateTime checkupCreatedAt;
    private OffsetDateTime checkupCompletedAt;
    private String checkupStatus;

    // Thông tin Bác sĩ ký chuyển viện
    private Long staffId;
    private String staffCode;
    private String doctorName;
    private String doctorTitle;
    private String doctorSpecialty;

    public TransferReferralPrintResponse() {
    }

    public static TransferReferralPrintResponse fromEntities(
            MedicalCheckup checkup,
            TransferRecord transfer,
            HealthRecord healthRecord,
            HealthInsurance insurance
    ) {
        if (checkup == null || transfer == null) {
            return null;
        }

        TransferReferralPrintResponse res = new TransferReferralPrintResponse();
        res.setReferringUnitName(REFERRING_UNIT_NAME);
        res.setTransferId(transfer.getId());
        res.setRecordCode(transfer.getRecordCode());
        res.setCheckupId(checkup.getId());
        res.setCheckupCode(checkup.getCheckupCode());
        res.setTargetHospital(transfer.getTargetHospital());
        res.setReasonCategory(transfer.getReasonCategory());
        res.setReasonCategoryLabel(resolveReasonCategoryLabel(transfer.getReasonCategory()));
        res.setTransferReason(transfer.getTransferReason());
        res.setCurrentCondition(transfer.getCurrentCondition());
        res.setFirstAidNotes(transfer.getFirstAidNotes());
        res.setRefusalNotes(transfer.getRefusalNotes());
        res.setTransferredAt(transfer.getTransferredAt());
        res.setReadOnly(true);

        Student student = checkup.getStudent();
        if (student != null) {
            res.setStudentId(student.getId());
            res.setStudentCode(student.getStudentCode());
            res.setDateOfBirth(student.getDateOfBirth());
            res.setGender(student.getGender());

            User studentUser = student.getUser();
            if (studentUser != null) {
                res.setStudentName(studentUser.getFullName());
                res.setStudentPhone(studentUser.getPhoneNumber());
                res.setStudentEmail(studentUser.getEmail());
            }

            AcademicClass academicClass = student.getAcademicClass();
            if (academicClass != null) {
                res.setClassCode(academicClass.getClassCode());
                res.setClassName(academicClass.getClassName());
                Faculty faculty = academicClass.getFaculty();
                if (faculty != null) {
                    res.setFacultyName(faculty.getFacultyName());
                }
            }
        }

        if (insurance != null) {
            res.setInsuranceCardNumber(insurance.getCardNumber());
            res.setInsuranceInitialHospital(insurance.getInitialHospital());
            res.setInsuranceEffectiveDate(insurance.getEffectiveDate());
            res.setInsuranceExpirationDate(insurance.getExpirationDate());
        }

        if (healthRecord != null) {
            res.setHeight(healthRecord.getHeight() != null ? healthRecord.getHeight().doubleValue() : null);
            res.setWeight(healthRecord.getWeight() != null ? healthRecord.getWeight().doubleValue() : null);
            res.setBloodType(healthRecord.getBloodType());
            res.setBmi(healthRecord.getBmi() != null ? healthRecord.getBmi().doubleValue() : null);
            res.setAllergies(healthRecord.getAllergies());
            res.setUnderlyingDiseases(healthRecord.getUnderlyingDiseases());
        }

        res.setCheckupType(checkup.getCheckupType());
        res.setIsEmergency(checkup.getIsEmergency());
        res.setEmergencyLevel(checkup.getEmergencyLevel());
        res.setEmergencyType(checkup.getEmergencyType());
        res.setSymptoms(checkup.getSymptoms());
        res.setDiagnosisNotes(checkup.getDiagnosisNotes());
        res.setTreatmentSuggestions(checkup.getTreatmentSuggestions());
        res.setDiseaseGroupName(checkup.getDiseaseGroup() != null ? checkup.getDiseaseGroup().getGroupName() : null);
        res.setCheckupCreatedAt(checkup.getCreatedAt());
        res.setCheckupCompletedAt(checkup.getCompletedAt());
        res.setCheckupStatus(checkup.getStatus());

        if (checkup.getPrescribedMedicines() != null) {
            res.setPrescribedMedicines(
                    checkup.getPrescribedMedicines().stream()
                            .sorted(Comparator.comparing(PrescribedMedicine::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                            .map(PrescribedMedicineResponse::fromEntity)
                            .toList()
            );
        }

        MedicalStaff staff = checkup.getStaff();
        if (staff != null) {
            res.setStaffId(staff.getId());
            res.setStaffCode(staff.getStaffCode());
            res.setDoctorTitle(staff.getTitle());
            res.setDoctorSpecialty(staff.getSpecialty());
            if (staff.getUser() != null) {
                res.setDoctorName(staff.getUser().getFullName());
            }
        }

        return res;
    }

    public static String resolveReasonCategoryLabel(String category) {
        if (category == null) {
            return null;
        }
        return switch (category.toUpperCase()) {
            case "QUA_TAI" -> "Quá tải";
            case "THIEU_THIET_BI" -> "Thiếu thiết bị / thuốc chuyên khoa";
            case "CAP_CUU_NANG" -> "Cấp cứu nặng vượt khả năng chuyên môn";
            default -> category;
        };
    }

    public String getReferringUnitName() {
        return referringUnitName;
    }

    public void setReferringUnitName(String referringUnitName) {
        this.referringUnitName = referringUnitName;
    }

    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public String getRecordCode() {
        return recordCode;
    }

    public void setRecordCode(String recordCode) {
        this.recordCode = recordCode;
    }

    public Long getCheckupId() {
        return checkupId;
    }

    public void setCheckupId(Long checkupId) {
        this.checkupId = checkupId;
    }

    public String getCheckupCode() {
        return checkupCode;
    }

    public void setCheckupCode(String checkupCode) {
        this.checkupCode = checkupCode;
    }

    public String getTargetHospital() {
        return targetHospital;
    }

    public void setTargetHospital(String targetHospital) {
        this.targetHospital = targetHospital;
    }

    public String getReasonCategory() {
        return reasonCategory;
    }

    public void setReasonCategory(String reasonCategory) {
        this.reasonCategory = reasonCategory;
    }

    public String getReasonCategoryLabel() {
        return reasonCategoryLabel;
    }

    public void setReasonCategoryLabel(String reasonCategoryLabel) {
        this.reasonCategoryLabel = reasonCategoryLabel;
    }

    public String getTransferReason() {
        return transferReason;
    }

    public void setTransferReason(String transferReason) {
        this.transferReason = transferReason;
    }

    public String getCurrentCondition() {
        return currentCondition;
    }

    public void setCurrentCondition(String currentCondition) {
        this.currentCondition = currentCondition;
    }

    public String getFirstAidNotes() {
        return firstAidNotes;
    }

    public void setFirstAidNotes(String firstAidNotes) {
        this.firstAidNotes = firstAidNotes;
    }

    public String getRefusalNotes() {
        return refusalNotes;
    }

    public void setRefusalNotes(String refusalNotes) {
        this.refusalNotes = refusalNotes;
    }

    public OffsetDateTime getTransferredAt() {
        return transferredAt;
    }

    public void setTransferredAt(OffsetDateTime transferredAt) {
        this.transferredAt = transferredAt;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStudentPhone() {
        return studentPhone;
    }

    public void setStudentPhone(String studentPhone) {
        this.studentPhone = studentPhone;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getInsuranceCardNumber() {
        return insuranceCardNumber;
    }

    public void setInsuranceCardNumber(String insuranceCardNumber) {
        this.insuranceCardNumber = insuranceCardNumber;
    }

    public String getInsuranceInitialHospital() {
        return insuranceInitialHospital;
    }

    public void setInsuranceInitialHospital(String insuranceInitialHospital) {
        this.insuranceInitialHospital = insuranceInitialHospital;
    }

    public LocalDate getInsuranceEffectiveDate() {
        return insuranceEffectiveDate;
    }

    public void setInsuranceEffectiveDate(LocalDate insuranceEffectiveDate) {
        this.insuranceEffectiveDate = insuranceEffectiveDate;
    }

    public LocalDate getInsuranceExpirationDate() {
        return insuranceExpirationDate;
    }

    public void setInsuranceExpirationDate(LocalDate insuranceExpirationDate) {
        this.insuranceExpirationDate = insuranceExpirationDate;
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

    public String getEmergencyLevel() {
        return emergencyLevel;
    }

    public void setEmergencyLevel(String emergencyLevel) {
        this.emergencyLevel = emergencyLevel;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
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

    public String getDiseaseGroupName() {
        return diseaseGroupName;
    }

    public void setDiseaseGroupName(String diseaseGroupName) {
        this.diseaseGroupName = diseaseGroupName;
    }

    public List<PrescribedMedicineResponse> getPrescribedMedicines() {
        return prescribedMedicines;
    }

    public void setPrescribedMedicines(List<PrescribedMedicineResponse> prescribedMedicines) {
        this.prescribedMedicines = prescribedMedicines != null ? prescribedMedicines : Collections.emptyList();
    }

    public OffsetDateTime getCheckupCreatedAt() {
        return checkupCreatedAt;
    }

    public void setCheckupCreatedAt(OffsetDateTime checkupCreatedAt) {
        this.checkupCreatedAt = checkupCreatedAt;
    }

    public OffsetDateTime getCheckupCompletedAt() {
        return checkupCompletedAt;
    }

    public void setCheckupCompletedAt(OffsetDateTime checkupCompletedAt) {
        this.checkupCompletedAt = checkupCompletedAt;
    }

    public String getCheckupStatus() {
        return checkupStatus;
    }

    public void setCheckupStatus(String checkupStatus) {
        this.checkupStatus = checkupStatus;
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
}
