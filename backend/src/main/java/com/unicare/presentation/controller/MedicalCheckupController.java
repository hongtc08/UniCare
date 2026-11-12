package com.unicare.presentation.controller;

import com.unicare.application.dto.request.CreateMedicalCheckupRequest;
import com.unicare.application.dto.request.UpdateMedicalCheckupRequest;
import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.service.MedicalCheckupService;
import com.unicare.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST Controller cung cấp các API Tiếp nhận (Theo hẹn / Khẩn cấp), Lập Phiếu khám Bất biến Read-only,
 * Tra cứu chi tiết Phiếu khám và Kê đơn thuốc Điện tử (/api/medical-records).
 */
@RestController
@RequestMapping("/api/medical-records")
public class MedicalCheckupController {

    private final MedicalCheckupService medicalCheckupService;

    public MedicalCheckupController(MedicalCheckupService medicalCheckupService) {
        this.medicalCheckupService = medicalCheckupService;
    }

    /**
     * Tra cứu nhanh danh sách Sinh viên theo MSSV, Họ tên hoặc Mã/Tên lớp phục vụ Tiếp nhận khẩn cấp / cấp cứu.
     * GET /api/medical-records/students/search?keyword=...
     */
    @GetMapping("/students/search")
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<List<StudentPersonalInfoResponse>>> searchStudentsForIntake(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String q,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        String effectiveKeyword = (keyword != null && !keyword.isBlank()) ? keyword : q;
        List<StudentPersonalInfoResponse> results = medicalCheckupService.searchStudentsForIntake(effectiveKeyword, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Tra cứu nhanh danh sách sinh viên thành công", results));
    }

    /**
     * Tiếp nhận sinh viên (từ lịch hẹn CONFIRMED hoặc Tiếp nhận khẩn cấp/cấp cứu)
     * và lập Phiếu khám trên bảng phẳng medical_checkups.
     * POST /api/medical-records
     */
    @PostMapping
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<MedicalCheckupDetailResponse>> createCheckup(
            @Valid @RequestBody CreateMedicalCheckupRequest request,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        MedicalCheckupDetailResponse response = medicalCheckupService.createCheckup(request, currentUsername);
        String message = "COMPLETED".equalsIgnoreCase(response.getStatus())
                ? "Tiếp nhận và hoàn tất phiếu khám thành công"
                : "Tiếp nhận sinh viên và tạo phiếu khám thành công";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, response));
    }

    /**
     * Cập nhật nội dung chuyên môn của Phiếu khám (triệu chứng, chẩn đoán, biện pháp sơ cứu, nhóm bệnh dịch tễ)
     * và quản lý trạng thái DRAFT (lưu nháp) / COMPLETED (hoàn tất ca khám, khóa cứng Read-only vĩnh viễn).
     * PUT /api/medical-records/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<MedicalCheckupDetailResponse>> updateCheckup(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMedicalCheckupRequest request,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        MedicalCheckupDetailResponse response = medicalCheckupService.updateCheckup(id, request, currentUsername);
        String message = "COMPLETED".equalsIgnoreCase(response.getStatus())
                ? "Hoàn tất ca khám và khóa phiếu khám thành công"
                : "Lưu nháp phiếu khám thành công";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * Hoàn tất ca khám trực tiếp: chuyển trạng thái sang COMPLETED, đổi lịch hẹn sang COMPLETED,
     * ghi nhận completed_at và khóa cứng Read-only vĩnh viễn.
     * PUT /api/medical-records/{id}/complete
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<MedicalCheckupDetailResponse>> completeCheckup(
            @PathVariable Long id,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        MedicalCheckupDetailResponse response = medicalCheckupService.completeCheckup(id, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Hoàn tất ca khám và khóa phiếu khám thành công", response));
    }

    /**
     * Xóa phiếu khám đang ở trạng thái nháp (DRAFT).
     * Chặn cứng vĩnh viễn mọi thao tác xóa nếu phiếu đã ở trạng thái COMPLETED.
     * DELETE /api/medical-records/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<Void>> deleteCheckup(
            @PathVariable Long id,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        medicalCheckupService.deleteCheckup(id, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Xóa phiếu khám nháp thành công", null));
    }

    /**
     * Lấy thông tin Phiếu khám theo mã lịch hẹn (appointmentId).
     * GET /api/medical-records/by-appointment/{appointmentId}
     */
    @GetMapping("/by-appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('SINH_VIEN', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<MedicalCheckupDetailResponse>> getCheckupByAppointment(
            @PathVariable Long appointmentId,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        MedicalCheckupDetailResponse response = medicalCheckupService.getCheckupByAppointment(appointmentId, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin phiếu khám theo lịch hẹn thành công", response));
    }

    /**
     * Lấy chi tiết Phiếu khám kèm Đơn thuốc theo ID.
     * GET /api/medical-records/{id}
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
     * PUT /api/medical-records/{id}/prescriptions
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
