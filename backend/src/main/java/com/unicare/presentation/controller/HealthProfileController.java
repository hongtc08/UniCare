/**
 * REST Controller cung cấp các API xem và cập nhật hồ sơ sức khỏe sinh học & thông tin cá nhân sinh viên (/api/health-profile).
 */
package com.unicare.presentation.controller;

import com.unicare.application.dto.request.HealthProfileRequest;
import com.unicare.application.dto.request.StudentPersonalInfoRequest;
import com.unicare.application.dto.response.HealthProfileResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.service.HealthProfileService;
import com.unicare.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/health-profile")
@PreAuthorize("hasAnyRole('SINH_VIEN', 'NHAN_VIEN_Y_TE')")
public class HealthProfileController {

    private final HealthProfileService healthProfileService;

    public HealthProfileController(HealthProfileService healthProfileService) {
        this.healthProfileService = healthProfileService;
    }

    /**
     * Xem thông tin cá nhân & học vụ của sinh viên (Họ tên, Ngày sinh, Giới tính, SĐT, Email, Lớp, Khoa)
     * GET /api/health-profile/personal-info (hoặc /api/health-profile/student-info)
     */
    @GetMapping({"/personal-info", "/student-info"})
    public ResponseEntity<ApiResponse<StudentPersonalInfoResponse>> getStudentPersonalInfo(
            @RequestParam(name = "studentId", required = false) Long studentId,
            @RequestParam(name = "student_id", required = false) Long studentIdSnake,
            @RequestParam(name = "id", required = false) Long idParam,
            Principal principal
    ) {
        Long resolvedId = resolveParamId(studentId, studentIdSnake, idParam);
        String currentUsername = principal != null ? principal.getName() : null;
        StudentPersonalInfoResponse response = healthProfileService.getStudentPersonalInfo(currentUsername, resolvedId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin cá nhân sinh viên thành công", response));
    }

    /**
     * Xem thông tin cá nhân & học vụ của sinh viên theo mã ID sinh viên
     * GET /api/health-profile/personal-info/{studentId}
     */
    @GetMapping({"/personal-info/{studentId}", "/student-info/{studentId}"})
    public ResponseEntity<ApiResponse<StudentPersonalInfoResponse>> getStudentPersonalInfoByStudentId(
            @PathVariable Long studentId,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : null;
        StudentPersonalInfoResponse response = healthProfileService.getStudentPersonalInfo(currentUsername, studentId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin cá nhân sinh viên thành công", response));
    }

    /**
     * Cập nhật thông tin cá nhân của sinh viên đang đăng nhập
     * PUT /api/health-profile/personal-info (hoặc /api/health-profile/student-info)
     */
    @PutMapping({"/personal-info", "/student-info"})
    public ResponseEntity<ApiResponse<StudentPersonalInfoResponse>> updateStudentPersonalInfo(
            @RequestParam(name = "studentId", required = false) Long studentId,
            @RequestParam(name = "student_id", required = false) Long studentIdSnake,
            @RequestParam(name = "id", required = false) Long idParam,
            @Valid @RequestBody StudentPersonalInfoRequest request,
            Principal principal
    ) {
        Long resolvedId = resolveParamId(studentId, studentIdSnake, idParam);
        String currentUsername = principal != null ? principal.getName() : null;
        StudentPersonalInfoResponse response = healthProfileService.updateStudentPersonalInfo(
                currentUsername, resolvedId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân sinh viên thành công", response));
    }

    /**
     * Cập nhật thông tin cá nhân của sinh viên theo mã ID sinh viên
     * PUT /api/health-profile/personal-info/{studentId}
     */
    @PutMapping({"/personal-info/{studentId}", "/student-info/{studentId}"})
    public ResponseEntity<ApiResponse<StudentPersonalInfoResponse>> updateStudentPersonalInfoByStudentId(
            @PathVariable Long studentId,
            @Valid @RequestBody StudentPersonalInfoRequest request,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : null;
        StudentPersonalInfoResponse response = healthProfileService.updateStudentPersonalInfo(
                currentUsername, studentId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân sinh viên thành công", response));
    }

    /**
     * Xem hồ sơ sức khỏe sinh học
     * GET /api/health-profile (SV tự xem của mình, hoặc Bác sĩ truyền ?studentId={id})
     */
    @GetMapping
    public ResponseEntity<ApiResponse<HealthProfileResponse>> getHealthProfile(
            @RequestParam(name = "studentId", required = false) Long studentId,
            @RequestParam(name = "student_id", required = false) Long studentIdSnake,
            @RequestParam(name = "id", required = false) Long idParam,
            Principal principal
    ) {
        Long resolvedId = resolveParamId(studentId, studentIdSnake, idParam);
        String currentUsername = principal != null ? principal.getName() : null;
        HealthProfileResponse response = healthProfileService.getHealthProfile(currentUsername, resolvedId);
        String message = response != null
                ? "Lấy hồ sơ sức khỏe thành công"
                : "Sinh viên chưa cập nhật hồ sơ sức khỏe";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * Xem hồ sơ sức khỏe sinh học theo mã ID sinh viên
     * GET /api/health-profile/{studentId}
     */
    @GetMapping("/{studentId}")
    public ResponseEntity<ApiResponse<HealthProfileResponse>> getHealthProfileByStudentId(
            @PathVariable Long studentId,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : null;
        HealthProfileResponse response = healthProfileService.getHealthProfile(currentUsername, studentId);
        String message = response != null
                ? "Lấy hồ sơ sức khỏe thành công"
                : "Sinh viên chưa cập nhật hồ sơ sức khỏe";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * Tạo mới hoặc cập nhật hồ sơ sức khỏe sinh học & tự động tính BMI
     * PUT /api/health-profile (SV tự cập nhật, hoặc Bác sĩ truyền ?studentId={id})
     */
    @PutMapping
    public ResponseEntity<ApiResponse<HealthProfileResponse>> updateHealthProfile(
            @RequestParam(name = "studentId", required = false) Long studentId,
            @RequestParam(name = "student_id", required = false) Long studentIdSnake,
            @RequestParam(name = "id", required = false) Long idParam,
            @Valid @RequestBody HealthProfileRequest request,
            Principal principal
    ) {
        Long resolvedId = resolveParamId(studentId, studentIdSnake, idParam);
        String currentUsername = principal != null ? principal.getName() : null;
        HealthProfileResponse response = healthProfileService.upsertHealthProfile(currentUsername, resolvedId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ sức khỏe thành công", response));
    }

    /**
     * Tạo mới hoặc cập nhật hồ sơ sức khỏe sinh học theo mã ID sinh viên
     * PUT /api/health-profile/{studentId}
     */
    @PutMapping("/{studentId}")
    public ResponseEntity<ApiResponse<HealthProfileResponse>> updateHealthProfileByStudentId(
            @PathVariable Long studentId,
            @Valid @RequestBody HealthProfileRequest request,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : null;
        HealthProfileResponse response = healthProfileService.upsertHealthProfile(currentUsername, studentId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ sức khỏe thành công", response));
    }

    private Long resolveParamId(Long studentId, Long studentIdSnake, Long idParam) {
        if (studentId != null) {
            return studentId;
        }
        if (studentIdSnake != null) {
            return studentIdSnake;
        }
        return idParam;
    }
}
