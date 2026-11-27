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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(adminUserController)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("GET /api/admin/users - Trả về danh sách tài khoản bọc trong ApiResponse")
    void getUsers_ReturnsList() throws Exception {
        UserResponse user = new UserResponse(1L, "admin01", "Quản Trị Viên", Role.BAN_QUAN_LY, UserStatus.ACTIVE);
        user.setStaffCode("QL0001");
        when(userService.getUsers(null, null, null)).thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].username").value("admin01"))
                .andExpect(jsonPath("$.data[0].role").value("BAN_QUAN_LY"))
                .andExpect(jsonPath("$.data[0].staffCode").value("QL0001"));
    }

    @Test
    @DisplayName("POST /api/admin/users - Tạo tài khoản thành công trả về 201 Created kèm thông tin mở rộng")
    void createUser_Success() throws Exception {
        CreateUserRequest request = new CreateUserRequest("22120001", "Nguyễn Văn Sinh", Role.SINH_VIEN, null);
        request.setEmail("22120001@gm.uit.edu.vn");
        request.setPhoneNumber("0909123456");
        request.setClassId(5);

        UserResponse response = new UserResponse(2L, "22120001", "Nguyễn Văn Sinh", Role.SINH_VIEN, UserStatus.ACTIVE);
        response.setEmail("22120001@gm.uit.edu.vn");
        response.setPhoneNumber("0909123456");
        response.setStudentCode("22120001");
        response.setClassId(5);
        response.setClassCode("KTPM2024");

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.username").value("22120001"))
                .andExpect(jsonPath("$.data.email").value("22120001@gm.uit.edu.vn"))
                .andExpect(jsonPath("$.data.classCode").value("KTPM2024"));
    }

    @Test
    @DisplayName("PUT /api/admin/users/{id} - Cập nhật thông tin nhân viên y tế kèm chuyên môn thành công")
    void updateUser_MedicalStaff_Success() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("ThS. Tâm Lý", Role.NHAN_VIEN_Y_TE);
        request.setEmail("tamly@uit.edu.vn");
        request.setTitle("CHUYEN_GIA_TAM_LY");
        request.setSpecialty("TAM_LY");

        UserResponse response = new UserResponse(3L, "cgtl01", "ThS. Tâm Lý", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        response.setEmail("tamly@uit.edu.vn");
        response.setTitle("CHUYEN_GIA_TAM_LY");
        response.setSpecialty("TAM_LY");

        when(userService.updateUser(eq(3L), any(UpdateUserRequest.class), eq("admin01"))).thenReturn(response);

        mockMvc.perform(put("/api/admin/users/3")
                        .principal(() -> "admin01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("CHUYEN_GIA_TAM_LY"))
                .andExpect(jsonPath("$.data.specialty").value("TAM_LY"));
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

    @Test
    @DisplayName("GlobalExceptionHandler giữ nguyên thông báo cụ thể và fallback thông báo mặc định cho AccessDeniedException")
    void globalExceptionHandler_PreservesCustomAccessDeniedMessage() {
        var customResp = exceptionHandler.handleAccessDeniedException(
                new AccessDeniedException("Bác sĩ chỉ được phép xóa ca khám do chính mình phụ trách")
        );
        assertThat(customResp.getStatusCode().value()).isEqualTo(403);
        assertThat(customResp.getBody()).isNotNull();
        assertThat(customResp.getBody().getMessage()).isEqualTo("Bác sĩ chỉ được phép xóa ca khám do chính mình phụ trách");

        var genericResp1 = exceptionHandler.handleAccessDeniedException(new AccessDeniedException("Access Denied"));
        assertThat(genericResp1.getBody().getMessage()).isEqualTo("Bạn không có quyền thực hiện thao tác này");

        var genericResp2 = exceptionHandler.handleAccessDeniedException(new AccessDeniedException("Access is denied"));
        assertThat(genericResp2.getBody().getMessage()).isEqualTo("Bạn không có quyền thực hiện thao tác này");

        var blankResp = exceptionHandler.handleAccessDeniedException(new AccessDeniedException("   "));
        assertThat(blankResp.getBody().getMessage()).isEqualTo("Bạn không có quyền thực hiện thao tác này");

        var nullMsgResp = exceptionHandler.handleAccessDeniedException(new AccessDeniedException(null));
        assertThat(nullMsgResp.getBody().getMessage()).isEqualTo("Bạn không có quyền thực hiện thao tác này");
    }
}
