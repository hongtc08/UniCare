/**
 * DTO trả về Hồ sơ Y tế Tổng hợp của Sinh viên phục vụ Bác sĩ tra cứu nhanh trong ca khám.
 * Bao gồm: Thông tin SV (Lớp, Khoa), Chỉ số sức khỏe & BMI, Tiền sử/Dị ứng/Bệnh nền, Thẻ BHYT và danh sách các lần khám trước đó.
 */
package com.unicare.application.dto.response;

import java.util.Collections;
import java.util.List;

public class StudentMedicalOverviewResponse {

    private StudentPersonalInfoResponse studentInfo;
    private HealthProfileResponse healthProfile;
    private HealthInsuranceResponse healthInsurance;
    private List<MedicalCheckupSummaryResponse> previousCheckups = Collections.emptyList();

    public StudentMedicalOverviewResponse() {
    }

    public StudentMedicalOverviewResponse(
            StudentPersonalInfoResponse studentInfo,
            HealthProfileResponse healthProfile,
            HealthInsuranceResponse healthInsurance,
            List<MedicalCheckupSummaryResponse> previousCheckups) {
        this.studentInfo = studentInfo;
        this.healthProfile = healthProfile;
        this.healthInsurance = healthInsurance;
        this.previousCheckups = previousCheckups != null ? previousCheckups : Collections.emptyList();
    }

    public StudentPersonalInfoResponse getStudentInfo() {
        return studentInfo;
    }

    public void setStudentInfo(StudentPersonalInfoResponse studentInfo) {
        this.studentInfo = studentInfo;
    }

    public HealthProfileResponse getHealthProfile() {
        return healthProfile;
    }

    public void setHealthProfile(HealthProfileResponse healthProfile) {
        this.healthProfile = healthProfile;
    }

    public HealthInsuranceResponse getHealthInsurance() {
        return healthInsurance;
    }

    public void setHealthInsurance(HealthInsuranceResponse healthInsurance) {
        this.healthInsurance = healthInsurance;
    }

    public List<MedicalCheckupSummaryResponse> getPreviousCheckups() {
        return previousCheckups;
    }

    public void setPreviousCheckups(List<MedicalCheckupSummaryResponse> previousCheckups) {
        this.previousCheckups = previousCheckups != null ? previousCheckups : Collections.emptyList();
    }

    /**
     * Alias cho Frontend nếu truy cập qua trường checkupHistory.
     */
    public List<MedicalCheckupSummaryResponse> getCheckupHistory() {
        return previousCheckups;
    }
}
