/**
 * REST Controller cung cấp các API quản lý lịch hẹn dành cho Bác sĩ / Chuyên gia (/api/doctor/appointments).
 */
package com.unicare.presentation.controller;

import com.unicare.application.dto.request.CancelAppointmentRequest;
import com.unicare.application.dto.request.RescheduleAppointmentRequest;
import com.unicare.application.dto.response.AppointmentResponse;
import com.unicare.application.service.AppointmentService;
import com.unicare.common.response.ApiResponse;
import com.unicare.domain.enums.ServiceType;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctor/appointments")
public class DoctorAppointmentController {

    private final AppointmentService appointmentService;

    public DoctorAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Lấy danh sách lịch hẹn thuộc phạm vi phụ trách của Bác sĩ / Chuyên gia đang đăng nhập.
     * Hỗ trợ lọc theo ngày khám, loại dịch vụ (serviceType / shiftType) và trạng thái (status).
     * GET /api/doctor/appointments
     */
    @GetMapping
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getDoctorAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) ServiceType serviceType,
            @RequestParam(required = false) ServiceType shiftType,
            @RequestParam(required = false) String status,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        ServiceType effectiveServiceType = serviceType != null ? serviceType : shiftType;
        List<AppointmentResponse> appointments = appointmentService.getDoctorAppointments(
                currentUsername,
                date,
                effectiveServiceType,
                status
        );
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lịch hẹn của bác sĩ thành công", appointments));
    }

    /**
     * Bác sĩ chủ động hủy lịch hẹn thuộc ca khám mình phụ trách (bắt buộc nhập lý do).
     * PUT /api/doctor/appointments/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointmentByDoctor(
            @PathVariable Long id,
            @Valid @RequestBody CancelAppointmentRequest request,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        AppointmentResponse response = appointmentService.cancelAppointment(id, request, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Hủy lịch hẹn thành công", response));
    }

    /**
     * Bác sĩ chủ động dời lịch hẹn thuộc ca khám mình phụ trách sang ca khám mới (bắt buộc nhập lý do).
     * PUT /api/doctor/appointments/{id}/reschedule
     */
    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rescheduleAppointmentByDoctor(
            @PathVariable Long id,
            @Valid @RequestBody RescheduleAppointmentRequest request,
            Principal principal) {
        String currentUsername = principal != null ? principal.getName() : null;
        AppointmentResponse response = appointmentService.rescheduleAppointment(id, request, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Dời lịch hẹn thành công", response));
    }
}
