package com.unicare.presentation.controller;

import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.service.MedicalCheckupService;
import com.unicare.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * REST Controller cung cấp API xem dòng thời gian lịch sử khám bệnh, đơn thuốc & chuyển viện phía Sinh viên (/api/student).
 */
@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('SINH_VIEN')")
public class StudentMedicalHistoryController {

    private final MedicalCheckupService medicalCheckupService;

    public StudentMedicalHistoryController(MedicalCheckupService medicalCheckupService) {
        this.medicalCheckupService = medicalCheckupService;
    }

    /**
     * Lấy dòng thời gian (Timeline) toàn bộ lịch sử Khám đa khoa, Tư vấn tâm lý, Khám định kỳ
     * đã hoàn tất của chính Sinh viên đang đăng nhập.
     * GET /api/student/medical-history
     */
    @GetMapping("/medical-history")
    public ResponseEntity<ApiResponse<List<MedicalCheckupDetailResponse>>> getMyMedicalHistory(
            @RequestParam(name = "studentId", required = false) Long studentId,
            @RequestParam(name = "student_id", required = false) Long studentIdSnake,
            Principal principal) {
        if (studentId != null && studentIdSnake != null && !studentId.equals(studentIdSnake)) {
            throw new AccessDeniedException("Sinh viên tuyệt đối không được phép xem lịch sử khám của sinh viên khác");
        }
        Long requestedStudentId = studentId != null ? studentId : studentIdSnake;
        String currentUsername = principal != null ? principal.getName() : null;

        List<MedicalCheckupDetailResponse> timeline =
                medicalCheckupService.getStudentMedicalHistory(currentUsername, requestedStudentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử khám bệnh thành công", timeline));
    }
}
