package com.unicare.presentation.controller;

import com.unicare.application.dto.request.FacultyRequest;
import com.unicare.application.dto.response.FacultyResponse;
import com.unicare.application.service.FacultyService;
import com.unicare.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/faculties", "/api/catalogs/faculties"})
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    /**
     * Lấy danh sách khoa đào tạo (hỗ trợ tìm kiếm theo từ khóa)
     * GET /api/faculties?search=...
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<List<FacultyResponse>>> getFaculties(
            @RequestParam(required = false) String search
    ) {
        List<FacultyResponse> faculties = facultyService.getFaculties(search);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khoa thành công", faculties));
    }

    /**
     * Lấy thông tin chi tiết một khoa theo ID
     * GET /api/faculties/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<FacultyResponse>> getFacultyById(@PathVariable Integer id) {
        FacultyResponse faculty = facultyService.getFacultyById(id);
        return ResponseEntity.ok(ApiResponse.success(faculty));
    }

    /**
     * Tạo mới một khoa đào tạo
     * POST /api/faculties
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<FacultyResponse>> createFaculty(
            @Valid @RequestBody FacultyRequest request
    ) {
        FacultyResponse newFaculty = facultyService.createFaculty(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm khoa thành công", newFaculty));
    }

    /**
     * Cập nhật thông tin khoa đào tạo
     * PUT /api/faculties/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<FacultyResponse>> updateFaculty(
            @PathVariable Integer id,
            @Valid @RequestBody FacultyRequest request
    ) {
        FacultyResponse updatedFaculty = facultyService.updateFaculty(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật khoa thành công", updatedFaculty));
    }

    /**
     * Xóa khoa đào tạo theo ID
     * DELETE /api/faculties/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<Void>> deleteFaculty(@PathVariable Integer id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa khoa thành công", null));
    }
}

