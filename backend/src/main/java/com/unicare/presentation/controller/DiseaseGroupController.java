package com.unicare.presentation.controller;

import com.unicare.application.dto.request.DiseaseGroupRequest;
import com.unicare.application.dto.response.DiseaseGroupResponse;
import com.unicare.application.service.DiseaseGroupService;
import com.unicare.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/disease-groups", "/api/catalogs/disease-groups"})
public class DiseaseGroupController {

    private final DiseaseGroupService diseaseGroupService;

    public DiseaseGroupController(DiseaseGroupService diseaseGroupService) {
        this.diseaseGroupService = diseaseGroupService;
    }

    /**
     * Lấy danh sách nhóm bệnh dịch tễ (hỗ trợ tìm kiếm theo từ khóa)
     * GET /api/disease-groups?search=...
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<List<DiseaseGroupResponse>>> getDiseaseGroups(
            @RequestParam(required = false) String search
    ) {
        List<DiseaseGroupResponse> groups = diseaseGroupService.getDiseaseGroups(search);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhóm bệnh thành công", groups));
    }

    /**
     * Lấy thông tin chi tiết một nhóm bệnh theo ID
     * GET /api/disease-groups/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE', 'SINH_VIEN')")
    public ResponseEntity<ApiResponse<DiseaseGroupResponse>> getDiseaseGroupById(@PathVariable Integer id) {
        DiseaseGroupResponse group = diseaseGroupService.getDiseaseGroupById(id);
        return ResponseEntity.ok(ApiResponse.success(group));
    }

    /**
     * Tạo mới một nhóm bệnh dịch tễ
     * POST /api/disease-groups
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<DiseaseGroupResponse>> createDiseaseGroup(
            @Valid @RequestBody DiseaseGroupRequest request
    ) {
        DiseaseGroupResponse newGroup = diseaseGroupService.createDiseaseGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm nhóm bệnh thành công", newGroup));
    }

    /**
     * Cập nhật thông tin nhóm bệnh dịch tễ
     * PUT /api/disease-groups/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<DiseaseGroupResponse>> updateDiseaseGroup(
            @PathVariable Integer id,
            @Valid @RequestBody DiseaseGroupRequest request
    ) {
        DiseaseGroupResponse updatedGroup = diseaseGroupService.updateDiseaseGroup(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhóm bệnh thành công", updatedGroup));
    }

    /**
     * Xóa nhóm bệnh dịch tễ theo ID
     * DELETE /api/disease-groups/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAN_QUAN_LY', 'NHAN_VIEN_Y_TE')")
    public ResponseEntity<ApiResponse<Void>> deleteDiseaseGroup(@PathVariable Integer id) {
        diseaseGroupService.deleteDiseaseGroup(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa nhóm bệnh thành công", null));
    }
}

