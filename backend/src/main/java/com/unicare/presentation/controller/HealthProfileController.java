/**
 * REST Controller cung cấp các API xem và cập nhật hồ sơ sức khỏe sinh học (/api/health-profile).
 */
package com.unicare.presentation.controller;

import com.unicare.application.dto.request.HealthProfileRequest;
import com.unicare.application.dto.response.HealthProfileResponse;
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
