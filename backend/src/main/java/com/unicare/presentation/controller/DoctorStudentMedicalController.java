/**
 * REST Controller cung cấp API tra cứu Hồ sơ Y tế Tổng hợp và Lịch sử Tâm lý (DASS-21 & Nhật ký Tâm trạng)
 * của Sinh viên phục vụ Bác sĩ / Chuyên gia tâm lý trong ca khám / tư vấn (/api/doctor/students).
 * Kiểm soát bảo mật: Chỉ Nhân viên y tế (NHAN_VIEN_Y_TE) mới được phép truy cập; chặn tuyệt đối Ban quản lý (BAN_QUAN_LY) và Sinh viên.
 */
package com.unicare.presentation.controller;

import com.unicare.application.dto.response.Dass21ResultResponse;
import com.unicare.application.dto.response.MoodLogHistoryResponse;
import com.unicare.application.dto.response.StudentMedicalOverviewResponse;
import com.unicare.application.dto.response.StudentMentalHealthHistoryResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.service.DoctorMentalHealthService;
import com.unicare.application.service.HealthProfileService;
import com.unicare.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctor/students")
@PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
public class DoctorStudentMedicalController {

    private final HealthProfileService healthProfileService;
    private final DoctorMentalHealthService doctorMentalHealthService;

    public DoctorStudentMedicalController(HealthProfileService healthProfileService) {
        this(healthProfileService, null);
    }

    @Autowired
    public DoctorStudentMedicalController(
            HealthProfileService healthProfileService,
            DoctorMentalHealthService doctorMentalHealthService
    ) {
        this.healthProfileService = healthProfileService;
        this.doctorMentalHealthService = doctorMentalHealthService;
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

    /**
     * Tra cứu tổng hợp Lịch sử kết quả DASS-21 (chuỗi điểm qua các lần test) và Nhật ký tâm trạng của Sinh viên
     * phục vụ Bác sĩ / Chuyên gia tâm lý khi thực hiện ca khám / tư vấn.
     * GET /api/doctor/students/{studentId}/mental-health-history (hoặc /{studentId}/mental-health)
     */
    @GetMapping({"/{studentId}/mental-health-history", "/{studentId}/mental-health"})
    public ResponseEntity<ApiResponse<StudentMentalHealthHistoryResponse>> getStudentMentalHealthHistory(
            @PathVariable Long studentId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        StudentMentalHealthHistoryResponse response = doctorMentalHealthService.getStudentMentalHealthHistory(
                currentUsername,
                studentId,
                period,
                fromDate,
                toDate
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Tra cứu lịch sử DASS-21 và nhật ký tâm trạng của sinh viên thành công", response));
    }

    /**
     * Tra cứu riêng lịch sử kết quả DASS-21 của Sinh viên phục vụ Bác sĩ / Chuyên gia tâm lý.
     * GET /api/doctor/students/{studentId}/dass21-history (hoặc /{studentId}/dass21)
     */
    @GetMapping({"/{studentId}/dass21-history", "/{studentId}/dass21"})
    public ResponseEntity<ApiResponse<List<Dass21ResultResponse>>> getStudentDass21History(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        List<Dass21ResultResponse> results = doctorMentalHealthService.getStudentDass21History(
                currentUsername,
                studentId,
                fromDate,
                toDate
        );
        return ResponseEntity.ok(ApiResponse.success("Tra cứu lịch sử kết quả DASS-21 của sinh viên thành công", results));
    }

    /**
     * Tra cứu riêng Nhật ký tâm trạng của Sinh viên phục vụ Bác sĩ / Chuyên gia tâm lý.
     * GET /api/doctor/students/{studentId}/mood-logs
     */
    @GetMapping("/{studentId}/mood-logs")
    public ResponseEntity<ApiResponse<MoodLogHistoryResponse>> getStudentMoodLogsForDoctor(
            @PathVariable Long studentId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        MoodLogHistoryResponse history = doctorMentalHealthService.getStudentMoodLogsForDoctor(
                currentUsername,
                studentId,
                period,
                fromDate,
                toDate
        );
        return ResponseEntity.ok(ApiResponse.success("Tra cứu nhật ký tâm trạng của sinh viên thành công", history));
    }
}
