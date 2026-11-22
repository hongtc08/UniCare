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

    @JsonAlias({"periodic_result", "ketLuanDinhKy", "ket_luan_dinh_ky", "ketQuaDinhKy", "ket_qua_dinh_ky", "periodicCheckupResult"})
    private String periodicResult;

    @JsonAlias({"is_periodic", "laKhamDinhKy", "la_kham_dinh_ky", "khamDinhKy", "periodic"})
    private Boolean isPeriodic;

    @JsonAlias({"trieuChung", "trieu_chung", "reasonSymptoms", "reason_symptoms", "bieuHien", "bieu_hien", "psychologicalSymptoms", "psychological_symptoms", "manifestations"})
    private String symptoms;

    @JsonAlias({"diagnosis_notes", "chanDoan", "chan_doan", "diagnosis", "tomTatTuVan", "tom_tat_tu_van", "counselingSummary", "counseling_summary", "consultationSummary", "consultation_summary"})
    private String diagnosisNotes;

    @JsonAlias({"treatment_suggestions", "bienPhapSoCuu", "bien_phap_so_cuu", "firstAid", "first_aid", "deXuatGiaiToa", "de_xuat_giai_toa", "deXuatGiaiTo", "reliefSuggestions", "relief_suggestions", "counselingRecommendations"})
    private String treatmentSuggestions;

    @JsonAlias({"disease_group_id", "nhomBenhId"})
    private Integer diseaseGroupId;

    @JsonAlias({"trangThai"})
    private String status;
}
