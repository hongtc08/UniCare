package com.unicare.presentation.controller;

import com.unicare.application.dto.request.AcademicClassRequest;
import com.unicare.application.dto.response.AcademicClassResponse;
import com.unicare.application.service.AcademicClassService;
import com.unicare.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/classes", "/api/catalogs/classes", "/api/academic-classes"})
public class AcademicClassController {

    private final AcademicClassService academicClassService;

    public AcademicClassController(AcademicClassService academicClassService) {
        this.academicClassService = academicClassService;
    }

    /**
     * Lấy danh sách lớp học (hỗ trợ lọc theo khoa và tìm kiếm từ khóa)
     * GET /api/classes?search=...&facultyId=...
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<List<AcademicClassResponse>>> getClasses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer facultyId
    ) {
        List<AcademicClassResponse> classes = academicClassService.getClasses(search, facultyId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lớp học thành công", classes));
    }

    /**
     * Lấy thông tin chi tiết một lớp học theo ID
     * GET /api/classes/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<AcademicClassResponse>> getClassById(@PathVariable Integer id) {
        AcademicClassResponse academicClass = academicClassService.getClassById(id);
        return ResponseEntity.ok(ApiResponse.success(academicClass));
    }

    /**
     * Tạo mới một lớp học
     * POST /api/classes
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<AcademicClassResponse>> createClass(
            @Valid @RequestBody AcademicClassRequest request
    ) {
        AcademicClassResponse newClass = academicClassService.createClass(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm lớp học thành công", newClass));
    }

    /**
     * Cập nhật thông tin lớp học
     * PUT /api/classes/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<AcademicClassResponse>> updateClass(
            @PathVariable Integer id,
            @Valid @RequestBody AcademicClassRequest request
    ) {
        AcademicClassResponse updatedClass = academicClassService.updateClass(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lớp học thành công", updatedClass));
    }

    /**
     * Xóa lớp học theo ID
     * DELETE /api/classes/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<Void>> deleteClass(@PathVariable Integer id) {
        academicClassService.deleteClass(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa lớp học thành công", null));
    }
}

