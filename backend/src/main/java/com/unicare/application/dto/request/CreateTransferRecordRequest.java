/**
 * DTO nhận dữ liệu Lập Hồ sơ Chuyển viện trên bảng transfer_records (POST /api/medical-records/{id}/transfer).
 */
package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTransferRecordRequest {

    @NotBlank(message = "Bệnh viện tuyến trên tiếp nhận không được để trống")
    @Size(max = 150, message = "Tên bệnh viện tuyến trên không được vượt quá 150 ký tự")
    @JsonAlias({"target_hospital", "benhVienTuyenTren", "hospitalName"})
    private String targetHospital;

    @NotBlank(message = "Phân loại lý do chuyển viện không được để trống")
    @JsonAlias({"reason_category", "phanLoaiLyDo", "category"})
    private String reasonCategory;

    @NotBlank(message = "Lý do chuyển viện chi tiết không được để trống")
    @JsonAlias({"transfer_reason", "lyDoChiTiet", "reason"})
    private String transferReason;

    @NotBlank(message = "Tình trạng hiện tại của sinh viên không được để trống")
    @JsonAlias({"current_condition", "tinhTrangHienTai", "condition"})
    private String currentCondition;

    @NotBlank(message = "Biện pháp sơ cứu đã thực hiện không được để trống")
    @JsonAlias({"first_aid_notes", "bienPhapSoCuu", "firstAid"})
    private String firstAidNotes;

    @JsonAlias({"refusal_notes", "ghiChuTuChoi"})
    private String refusalNotes;

    @JsonAlias({"transferred_at", "thoiGianChuyen"})
    private OffsetDateTime transferredAt;

    public CreateTransferRecordRequest() {
    }

    public CreateTransferRecordRequest(
            String targetHospital,
            String reasonCategory,
            String transferReason,
            String currentCondition,
            String firstAidNotes,
            String refusalNotes,
            OffsetDateTime transferredAt
    ) {
        this.targetHospital = targetHospital;
        this.reasonCategory = reasonCategory;
        this.transferReason = transferReason;
        this.currentCondition = currentCondition;
        this.firstAidNotes = firstAidNotes;
        this.refusalNotes = refusalNotes;
        this.transferredAt = transferredAt;
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
}
