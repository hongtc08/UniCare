/**
 * REST Controller cung cấp các API CRUD thiết lập lịch trực và mở ca khám tuần (/api/shifts).
 */
package com.unicare.presentation.controller;

import com.unicare.application.dto.request.ShiftRequest;
import com.unicare.application.dto.response.DoctorOptionResponse;
import com.unicare.application.dto.response.ShiftResponse;
import com.unicare.application.service.ShiftService;
import com.unicare.common.response.ApiResponse;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    /**
     * Lấy danh sách ca khám (hỗ trợ lọc theo ngày, khoảng ngày trong tuần, bác sĩ, loại dịch vụ, trạng thái, phòng khám)
     * GET /api/shifts
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<List<ShiftResponse>>> getShifts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) ServiceType serviceType,
            @RequestParam(required = false) ShiftStatus status,
            @RequestParam(required = false) String room
    ) {
        List<ShiftResponse> shifts = shiftService.getShifts(
                date, fromDate, toDate, doctorId, serviceType, status, room
        );
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ca khám thành công", shifts));
    }

    /**
     * Lấy danh sách Bác sĩ / Nhân viên y tế đang hoạt động (ACTIVE) để hiển thị Dropdown chọn bác sĩ
     * GET /api/shifts/doctors
     */
    @GetMapping("/doctors")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<List<DoctorOptionResponse>>> getAvailableDoctors() {
        List<DoctorOptionResponse> doctors = shiftService.getAvailableDoctors();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bác sĩ thành công", doctors));
    }

    /**
     * Xem thông tin chi tiết một ca khám
     * GET /api/shifts/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<ShiftResponse>> getShiftById(@PathVariable Long id) {
        ShiftResponse shift = shiftService.getShiftById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin ca khám thành công", shift));
    }

    /**
     * Mở ca khám mới (Dành cho Ban quản lý và Bác sĩ)
     * POST /api/shifts
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<ShiftResponse>> createShift(
            @Valid @RequestBody ShiftRequest request,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : null;
        ShiftResponse created = shiftService.createShift(request, currentUsername);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Mở ca khám thành công", created));
    }

    /**
     * Cập nhật thông tin ca khám (Dành cho Ban quản lý và Bác sĩ)
     * PUT /api/shifts/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<ShiftResponse>> updateShift(
            @PathVariable Long id,
            @Valid @RequestBody ShiftRequest request,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : null;
        ShiftResponse updated = shiftService.updateShift(id, request, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ca khám thành công", updated));
    }

    /**
     * Xóa ca khám (Chỉ xóa được khi chưa có lịch hẹn nào được đặt)
     * DELETE /api/shifts/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<Void>> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa ca khám thành công", null));
    }
}
