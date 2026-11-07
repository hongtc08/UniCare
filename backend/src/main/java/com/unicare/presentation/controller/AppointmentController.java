package com.unicare.presentation.controller;

import com.unicare.application.dto.request.CancelAppointmentRequest;
import com.unicare.application.dto.request.CreateAppointmentRequest;
import com.unicare.application.dto.request.RescheduleAppointmentRequest;
import com.unicare.application.dto.response.AppointmentResponse;
import com.unicare.application.service.AppointmentService;
import com.unicare.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Sinh viên đặt lịch hẹn khám bệnh mới (Tự động xác nhận CONFIRMED & khóa đồng thời ca khám).
     * POST /api/appointments
     */
    @PostMapping
    @PreAuthorize("hasRole('SINH_VIEN')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        AppointmentResponse response = appointmentService.createAppointment(request, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt lịch hẹn khám thành công", response));
    }

    /**
     * Hủy lịch hẹn khám bệnh.
     * PUT /api/appointments/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SINH_VIEN', 'NHAN_VIEN_Y_TE', 'BAN_QUAN_LY')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @PathVariable Long id,
            @Valid @RequestBody CancelAppointmentRequest request,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        AppointmentResponse response = appointmentService.cancelAppointment(id, request, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Hủy lịch hẹn thành công", response));
    }

    /**
     * Dời lịch hẹn khám bệnh sang ca khám mới.
     * PUT /api/appointments/{id}/reschedule
     */
    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('SINH_VIEN', 'NHAN_VIEN_Y_TE', 'BAN_QUAN_LY')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rescheduleAppointment(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleAppointmentRequest request,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        AppointmentResponse response = appointmentService.rescheduleAppointment(id, request, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Dời lịch hẹn thành công", response));
    }

    /**
     * Lấy danh sách lịch hẹn của sinh viên đang đăng nhập.
     * GET /api/appointments/my-appointments?status=CONFIRMED
     */
    @GetMapping("/my-appointments")
    @PreAuthorize("hasRole('SINH_VIEN')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getMyAppointments(
            @RequestParam(required = false) String status,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        List<AppointmentResponse> list = appointmentService.getStudentAppointments(currentUsername, status);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lịch hẹn thành công", list));
    }

    /**
     * Lấy thông tin chi tiết một lịch hẹn theo ID.
     * GET /api/appointments/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SINH_VIEN', 'NHAN_VIEN_Y_TE', 'BAN_QUAN_LY')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(
            @PathVariable Long id,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        AppointmentResponse response = appointmentService.getAppointmentById(id, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết lịch hẹn thành công", response));
    }
}
