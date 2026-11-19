/**
 * REST Controller cung cấp danh sách Cơ sở y tế (/api/facilities) phục vụ luồng Đặt lịch khám.
 */
package com.unicare.presentation.controller;

import com.unicare.application.dto.response.FacilityResponse;
import com.unicare.application.service.ShiftService;
import com.unicare.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/facilities")
public class FacilityController {

    private final ShiftService shiftService;

    public FacilityController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    /**
     * Lấy danh sách cơ sở y tế gồm ID và tên.
     * GET /api/facilities
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<List<FacilityResponse>>> getFacilities() {
        List<FacilityResponse> facilities = shiftService.getFacilities();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cơ sở y tế thành công", facilities));
    }
}
