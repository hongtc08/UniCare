package com.unicare.presentation.controller;

import com.unicare.application.dto.request.HealthInsuranceRequest;
import com.unicare.application.dto.response.HealthInsuranceResponse;
import com.unicare.application.service.HealthInsuranceService;
import com.unicare.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/health-insurances")
public class HealthInsuranceController {

    private final HealthInsuranceService healthInsuranceService;

    public HealthInsuranceController(HealthInsuranceService healthInsuranceService) {
        this.healthInsuranceService = healthInsuranceService;
    }

    /**
     * Lấy danh sách thẻ BHYT (Quản lý/Y tế xem tất cả và lọc; Sinh viên chỉ xem thẻ của chính mình)
     * GET /api/health-insurances?search=...&status=...
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<List<HealthInsuranceResponse>>> getHealthInsurances(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : null;
        List<HealthInsuranceResponse> list = healthInsuranceService.getHealthInsurances(search, status, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách thẻ BHYT thành công", list));
    }

    /**
     * Xem chi tiết 1 thẻ BHYT theo ID
     * GET /api/health-insurances/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<HealthInsuranceResponse>> getHealthInsuranceById(
            @PathVariable Long id,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : null;
        HealthInsuranceResponse response = healthInsuranceService.getHealthInsuranceById(id, currentUsername);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Xem thông tin thẻ BHYT của sinh viên theo ID sinh viên
     * GET /api/health-insurances/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<HealthInsuranceResponse>> getHealthInsuranceByStudentId(
            @PathVariable Long studentId,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : null;
        HealthInsuranceResponse response = healthInsuranceService.getHealthInsuranceByStudentId(studentId, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin thẻ BHYT sinh viên thành công", response));
    }

    /**
     * Thêm mới thẻ BHYT cho sinh viên
     * POST /api/health-insurances
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<HealthInsuranceResponse>> createHealthInsurance(
            @Valid @RequestBody HealthInsuranceRequest request,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : null;
        HealthInsuranceResponse response = healthInsuranceService.createHealthInsurance(request, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm thẻ BHYT thành công", response));
    }

    /**
     * Cập nhật thông tin thẻ BHYT
     * PUT /api/health-insurances/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<HealthInsuranceResponse>> updateHealthInsurance(
            @PathVariable Long id,
            @Valid @RequestBody HealthInsuranceRequest request,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : null;
        HealthInsuranceResponse response = healthInsuranceService.updateHealthInsurance(id, request, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thẻ BHYT thành công", response));
    }

    /**
     * Xóa thẻ BHYT
     * DELETE /api/health-insurances/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<Void>> deleteHealthInsurance(
            @PathVariable Long id,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : null;
        healthInsuranceService.deleteHealthInsurance(id, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Xóa thẻ BHYT thành công", null));
    }
}

