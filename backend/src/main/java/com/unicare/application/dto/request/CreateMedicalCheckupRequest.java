package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

/**
 * DTO nhận dữ liệu Tiếp nhận sinh viên (từ lịch hẹn CONFIRMED hoặc Tiếp nhận khẩn cấp/cấp cứu)
 * và khởi tạo Phiếu khám trên bảng phẳng medical_checkups (POST /api/medical-records).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateMedicalCheckupRequest {

    @JsonAlias({"appointment_id", "lichHenId"})
    private Long appointmentId;

    @JsonAlias({"student_id", "sinhVienId"})
    private Long studentId;

    @JsonAlias({"student_code", "maSV", "mssv"})
    private String studentCode;

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

    @JsonAlias({"checkup_type", "loaiPhieu", "type"})
    private String checkupType;

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
