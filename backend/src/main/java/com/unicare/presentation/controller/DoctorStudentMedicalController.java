/**
 * REST Controller cung cấp API tra cứu Hồ sơ Y tế Tổng hợp của Sinh viên phục vụ Bác sĩ trong ca khám (/api/doctor/students).
 * Kiểm soát bảo mật: Chỉ Nhân viên y tế (NHAN_VIEN_Y_TE) mới được phép truy cập; chặn Ban quản lý (BAN_QUAN_LY) và Sinh viên.
 */
package com.unicare.presentation.controller;

import com.unicare.application.dto.response.StudentMedicalOverviewResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.service.HealthProfileService;
import com.unicare.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/doctor/students")
@PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
public class DoctorStudentMedicalController {

    private final HealthProfileService healthProfileService;

    public DoctorStudentMedicalController(HealthProfileService healthProfileService) {
        this.healthProfileService = healthProfileService;
    }

    /**
     * Tra cứu nhanh danh sách Sinh viên theo MSSV, Họ tên hoặc Mã/Tên lớp phục vụ Tiếp nhận khẩn cấp / cấp cứu.
     * GET /api/doctor/students/search?keyword=...
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<StudentPersonalInfoResponse>>> searchStudentsForEmergency(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String q,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        String effectiveKeyword = (keyword != null && !keyword.isBlank()) ? keyword : q;
        List<StudentPersonalInfoResponse> results = healthProfileService.searchStudentsForDoctor(currentUsername, effectiveKeyword);
        return ResponseEntity.ok(ApiResponse.success("Tra cứu nhanh danh sách sinh viên thành công", results));
    }

    /**
     * Tra cứu Hồ sơ Y tế Tổng hợp của Sinh viên phục vụ Bác sĩ xem nhanh khi đang tiếp nhận/khám.
     * Trả về: Thông tin SV (Lớp, Khoa), Chỉ số sức khỏe & BMI, Tiền sử/Dị ứng/Bệnh nền, Thẻ BHYT và danh sách các lần khám trước đó.
     * GET /api/doctor/students/{studentId}/medical-overview
     */
    @GetMapping("/{studentId}/medical-overview")
    public ResponseEntity<ApiResponse<StudentMedicalOverviewResponse>> getStudentMedicalOverview(
            @PathVariable Long studentId,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        StudentMedicalOverviewResponse overview = healthProfileService.getStudentMedicalOverview(currentUsername, studentId);
        return ResponseEntity.ok(ApiResponse.success("Tra cứu hồ sơ y tế tổng hợp của sinh viên thành công", overview));
    }
}
