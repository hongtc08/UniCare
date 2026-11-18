package com.unicare.presentation.controller;

import com.unicare.application.dto.request.CreateMedicalCheckupRequest;
import com.unicare.application.dto.request.CreateTransferRecordRequest;
import com.unicare.application.dto.request.UpdateEmergencyProgressRequest;
import com.unicare.application.dto.request.UpdateMedicalCheckupRequest;
import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.EmergencyCasePageResponse;
import com.unicare.application.dto.response.EmergencyStatsResponse;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.dto.response.TransferReferralPrintResponse;
import com.unicare.application.service.MedicalCheckupService;
import com.unicare.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
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

    /**
     * Lập Hồ sơ Chuyển viện trên bảng transfer_records:
     * Lưu bệnh viện tuyến trên, phân loại lý do (QUA_TAI, THIEU_THIET_BI, CAP_CUU_NANG), lý do chi tiết,
     * tình trạng hiện tại, biện pháp sơ cứu, ghi chú từ chối (nếu có), thời gian chuyển;
     * khóa chỉ đọc hồ sơ chuyển viện và tự động kết thúc ca khám tại trường.
     * POST /api/medical-records/{id}/transfer
     */
    @PostMapping("/{id}/transfer")
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<MedicalCheckupDetailResponse>> createTransferRecord(
            @PathVariable Long id,
            @Valid @RequestBody CreateTransferRecordRequest request,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        MedicalCheckupDetailResponse response = medicalCheckupService.createTransferRecord(id, request, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lập hồ sơ chuyển viện và hoàn tất ca khám tại trường thành công", response));
    }

    /**
     * Kết xuất dữ liệu chuẩn / bản in để xuất Giấy giới thiệu chuyển viện.
     * GET /api/medical-records/{id}/transfer (và /transfer/print, /transfer/referral-paper)
     */
    @GetMapping({"/{id}/transfer", "/{id}/transfer/print", "/{id}/transfer/referral-paper"})
    @PreAuthorize("hasAnyRole('SINH_VIEN', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<TransferReferralPrintResponse>> getTransferReferralPaper(
            @PathVariable Long id,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        TransferReferralPrintResponse response = medicalCheckupService.getTransferReferralPaper(id, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu giấy giới thiệu chuyển viện thành công", response));
    }

    /**
     * Tạo phiếu tiếp nhận ca khẩn cấp (lưu và trả về mức độ khẩn cấp, loại cấp cứu, người báo cáo/liên hệ).
     * POST /api/medical-records/emergencies
     */
    @PostMapping("/emergencies")
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<MedicalCheckupDetailResponse>> createEmergencyIntake(
            @Valid @RequestBody CreateMedicalCheckupRequest request,
            Principal principal) {
        if (request != null && request.getIsEmergency() == null) {
            request.setIsEmergency(true);
        }
        String currentUsername = principal != null ? principal.getName() : null;
        MedicalCheckupDetailResponse response = medicalCheckupService.createCheckup(request, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tiếp nhận ca khẩn cấp thành công", response));
    }

    /**
     * Lấy danh sách ca cấp cứu đã lưu, hỗ trợ lọc và phân trang.
     * Mỗi ca có mã ca, tên và MSSV, giờ tiếp nhận, loại cấp cứu, bước hiện tại và mức độ khẩn cấp.
     * GET /api/medical-records/emergencies
     */
    @GetMapping("/emergencies")
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<EmergencyCasePageResponse>> getEmergencyCases(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String emergencyLevel,
            @RequestParam(required = false) String emergencyType,
            @RequestParam(required = false) String currentStep,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        EmergencyCasePageResponse response = medicalCheckupService.getEmergencyCases(
                keyword, emergencyLevel, emergencyType, currentStep, status, date, page, size, currentUsername
        );
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ca cấp cứu thành công", response));
    }

    /**
     * Số liệu thống kê ca cấp cứu: số ca đang xử lý, đang sơ cứu, đang chuyển viện và đã kết thúc hôm nay.
     * GET /api/medical-records/emergencies/stats
     */
    @GetMapping("/emergencies/stats")
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<EmergencyStatsResponse>> getEmergencyStats(Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        EmergencyStatsResponse response = medicalCheckupService.getEmergencyStats(currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Lấy số liệu thống kê ca cấp cứu thành công", response));
    }

    /**
     * Cập nhật tiến độ ca cấp cứu: lưu bước xử lý hiện tại khi chuyển sang sơ cứu, chuyển viện hoặc kết thúc.
     * PUT /api/medical-records/{id}/progress (và /step)
     */
    @PutMapping({"/{id}/progress", "/{id}/step"})
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<MedicalCheckupDetailResponse>> updateEmergencyProgress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmergencyProgressRequest request,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        MedicalCheckupDetailResponse response = medicalCheckupService.updateEmergencyProgress(id, request, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tiến độ ca cấp cứu thành công", response));
    }
}
