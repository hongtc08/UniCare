package com.unicare.presentation.controller;

import com.unicare.application.dto.request.CreateUserRequest;
import com.unicare.application.dto.request.UpdateUserRequest;
import com.unicare.application.dto.request.UpdateUserStatusRequest;
import com.unicare.application.dto.response.UserResponse;
import com.unicare.application.service.UserService;
import com.unicare.common.response.ApiResponse;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('BAN_QUAN_LY')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Lấy danh sách tài khoản, hỗ trợ tìm kiếm và lọc
     * GET /api/admin/users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status
    ) {
        List<UserResponse> users = userService.getUsers(search, role, status);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tài khoản thành công", users));
    }

    /**
     * Xem thông tin chi tiết một tài khoản
     * GET /api/admin/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Tạo tài khoản mới (mật khẩu mặc định: UniCare@123)
     * POST /api/admin/users
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse newUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm tài khoản thành công", newUser));
    }

    /**
     * Cập nhật thông tin tài khoản (Họ tên, Vai trò)
     * PUT /api/admin/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : "";
        UserResponse updatedUser = userService.updateUser(id, request, currentUsername);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tài khoản thành công", updatedUser));
    }

    /**
     * Khóa hoặc Mở khóa tài khoản thủ công (ACTIVE / LOCKED)
     * PATCH /api/admin/users/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            Principal principal
    ) {
        String currentUsername = principal != null ? principal.getName() : "";
        UserResponse updatedUser = userService.updateUserStatus(id, request, currentUsername);
        String actionDesc = request.getStatus() == UserStatus.LOCKED ? "Khóa" : "Mở khóa";
        return ResponseEntity.ok(ApiResponse.success(actionDesc + " tài khoản thành công", updatedUser));
    }

    /**
     * Cấp lại mật khẩu mặc định (UniCare@123)
     * POST /api/admin/users/{id}/reset-password
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return ResponseEntity.ok(ApiResponse.success("Đã cấp lại mật khẩu mặc định thành công", null));
    }
}
