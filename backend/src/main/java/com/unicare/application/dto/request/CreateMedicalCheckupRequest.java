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

    @JsonAlias({"checkup_type", "loaiPhieu", "type"})
    private String checkupType;

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
