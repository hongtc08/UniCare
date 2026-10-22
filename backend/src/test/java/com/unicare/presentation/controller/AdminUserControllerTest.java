package com.unicare.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicare.application.dto.request.CreateUserRequest;
import com.unicare.application.dto.request.UpdateUserRequest;
import com.unicare.application.dto.request.UpdateUserStatusRequest;
import com.unicare.application.dto.response.UserResponse;
import com.unicare.application.service.UserService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;
import com.unicare.presentation.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminUserController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/admin/users - Trả về danh sách tài khoản bọc trong ApiResponse")
    void getUsers_ReturnsList() throws Exception {
        UserResponse user = new UserResponse(1L, "admin01", "Quản Trị Viên", Role.BAN_QUAN_LY, UserStatus.ACTIVE);
        when(userService.getUsers(null, null, null)).thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].username").value("admin01"))
                .andExpect(jsonPath("$.data[0].role").value("BAN_QUAN_LY"));
    }

    @Test
    @DisplayName("POST /api/admin/users - Tạo tài khoản thành công trả về 201 Created")
    void createUser_Success() throws Exception {
        CreateUserRequest request = new CreateUserRequest("sv22120001", "Nguyễn Văn Sinh", Role.SINH_VIEN, null);
        UserResponse response = new UserResponse(2L, "sv22120001", "Nguyễn Văn Sinh", Role.SINH_VIEN, UserStatus.ACTIVE);

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.username").value("sv22120001"));
    }

    @Test
    @DisplayName("POST /api/admin/users - Dữ liệu không hợp lệ trả về 400 Bad Request")
    void createUser_InvalidInput_Returns400() throws Exception {
        CreateUserRequest request = new CreateUserRequest("", "", null, null);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.username").exists())
                .andExpect(jsonPath("$.data.fullName").exists());
    }

    @Test
    @DisplayName("POST /api/admin/users - Trùng username trả về 409 Conflict")
    void createUser_DuplicateUsername_Returns409() throws Exception {
        CreateUserRequest request = new CreateUserRequest("admin01", "Quản Trị Viên", Role.BAN_QUAN_LY, null);

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new DuplicateResourceException("Tên đăng nhập 'admin01' đã tồn tại trên hệ thống."));

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tên đăng nhập 'admin01' đã tồn tại trên hệ thống."));
    }

    @Test
    @DisplayName("PATCH /api/admin/users/{id}/status - Tự khóa bản thân trả về 400 Bad Request")
    void updateUserStatus_SelfLock_Returns400() throws Exception {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(UserStatus.LOCKED);

        when(userService.updateUserStatus(eq(1L), any(UpdateUserStatusRequest.class), eq("admin01")))
                .thenThrow(new BadRequestException("Bạn không thể tự khóa tài khoản của chính mình."));

        mockMvc.perform(patch("/api/admin/users/1/status")
                        .principal(() -> "admin01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bạn không thể tự khóa tài khoản của chính mình."));
    }

    @Test
    @DisplayName("POST /api/admin/users/{id}/reset-password - Cấp lại mật khẩu thành công")
    void resetPassword_Success() throws Exception {
        mockMvc.perform(post("/api/admin/users/1/reset-password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đã cấp lại mật khẩu mặc định thành công"));
    }

    @Test
    @DisplayName("GET /api/admin/users/{id} - Không tìm thấy trả về 404 Not Found")
    void getUserById_NotFound_Returns404() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy người dùng với mã ID: 999"));

        mockMvc.perform(get("/api/admin/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Không tìm thấy người dùng với mã ID: 999"));
    }
}
