package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * DTO nhận dữ liệu cập nhật nội dung chuyên môn và quản lý trạng thái Phiếu khám
 * (DRAFT: Lưu nháp cho phép sửa tiếp; COMPLETED: Hoàn tất ca khám và khóa Read-only vĩnh viễn)
 * trên bảng phẳng medical_checkups (PUT /api/medical-records/{id}).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateMedicalCheckupRequest {

    @JsonAlias({"checkup_type", "loaiPhieu", "type"})
    private String checkupType;

    @JsonAlias({"is_emergency", "laKhanCap", "emergency"})
    private Boolean isEmergency;

    @JsonAlias({"emergency_level", "urgencyLevel", "urgency_level", "mucDoKhanCap", "severity", "level"})
    private String emergencyLevel;

    @JsonAlias({"emergency_type", "loaiCapCuu", "incidentType", "incident_type"})
    private String emergencyType;

    @JsonAlias({"reporter_contact", "reportedBy", "reported_by", "nguoiBaoCao", "contactInfo", "reporter"})
    private String reporterContact;

    @JsonAlias({"current_step", "workflowStep", "workflow_step", "step", "buocHienTai"})
    private String currentStep;

    @JsonAlias({"periodic_result", "ketLuanDinhKy"})
    private String periodicResult;

    @JsonAlias({"trieuChung", "reasonSymptoms"})
    private String symptoms;

    @JsonAlias({"diagnosis_notes", "chanDoan", "diagnosis"})
    private String diagnosisNotes;

    @JsonAlias({"treatment_suggestions", "bienPhapSoCuu", "firstAid", "deXuatGiaiToa"})
    private String treatmentSuggestions;

    @JsonAlias({"disease_group_id", "nhomBenhId"})
    private Integer diseaseGroupId;

    @JsonAlias({"trangThai"})
    private String status;
}
