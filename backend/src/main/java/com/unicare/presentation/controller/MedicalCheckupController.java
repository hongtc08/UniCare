package com.unicare.presentation.controller;

import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.service.MedicalCheckupService;
import com.unicare.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * API chi tiết phiếu khám và kê đơn thuốc
 */
@RestController
@RequestMapping("/api/medical-records")
public class MedicalCheckupController {

    private final MedicalCheckupService medicalCheckupService;

    public MedicalCheckupController(MedicalCheckupService medicalCheckupService) {
        this.medicalCheckupService = medicalCheckupService;
    }

    /**
     * Lấy chi tiết Phiếu khám kèm Đơn thuốc theo ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SINH_VIEN', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<MedicalCheckupDetailResponse>> getCheckupDetail(
            @PathVariable Long id,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        MedicalCheckupDetailResponse response = medicalCheckupService.getCheckupDetail(id, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết phiếu khám thành công", response));
    }

    /**
     * Bác sĩ kê đơn / cập nhật danh sách thuốc kê đơn đi kèm Phiếu khám.
     */
    @PutMapping("/{id}/prescriptions")
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<MedicalCheckupDetailResponse>> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePrescriptionRequest request,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        MedicalCheckupDetailResponse response = medicalCheckupService.updatePrescription(id, request, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đơn thuốc thành công", response));
    }
}
