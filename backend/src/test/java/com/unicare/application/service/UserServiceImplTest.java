package com.unicare.application.service;

import com.unicare.application.dto.request.CreateUserRequest;
import com.unicare.application.dto.request.UpdateUserRequest;
import com.unicare.application.dto.request.UpdateUserStatusRequest;
import com.unicare.application.dto.response.UserResponse;
import com.unicare.application.service.impl.UserServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;
import com.unicare.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("admin01", "hashed_pwd", "Quản Trị Viên", Role.BAN_QUAN_LY, UserStatus.ACTIVE);
        sampleUser.setId(1L);
    }

    @Test
    @DisplayName("Lấy danh sách tài khoản thành công")
    void getUsers_Success() {
        when(userRepository.searchUsers(null, null, null)).thenReturn(List.of(sampleUser));

        List<UserResponse> responses = userService.getUsers(null, null, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUsername()).isEqualTo("admin01");
        assertThat(responses.get(0).getRole()).isEqualTo(Role.BAN_QUAN_LY);
    }

    @Test
    @DisplayName("Tạo tài khoản mới thành công")
    void createUser_Success() {
        CreateUserRequest request = new CreateUserRequest("sv2026", "Nguyễn Văn A", Role.SINH_VIEN, null);
        when(userRepository.existsByUsernameIgnoreCase("sv2026")).thenReturn(false);
        when(passwordEncoder.encode(UserServiceImpl.DEFAULT_PASSWORD)).thenReturn("encoded_default_pwd");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserResponse response = userService.createUser(request);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getUsername()).isEqualTo("sv2026");
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(passwordEncoder).encode(UserServiceImpl.DEFAULT_PASSWORD);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Tạo tài khoản thất bại do trùng username (409)")
    void createUser_DuplicateUsername_ThrowsException() {
        CreateUserRequest request = new CreateUserRequest("admin01", "Nguyễn Văn A", Role.SINH_VIEN, null);
        when(userRepository.existsByUsernameIgnoreCase("admin01")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã tồn tại");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Cập nhật thông tin tài khoản thành công")
    void updateUser_Success() {
        UpdateUserRequest request = new UpdateUserRequest("Quản Trị Viên Cập Nhật", Role.BAN_QUAN_LY);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserResponse response = userService.updateUser(1L, request, "admin01");

        assertThat(response.getFullName()).isEqualTo("Quản Trị Viên Cập Nhật");
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Chặn tự hạ vai trò của chính tài khoản đang đăng nhập")
    void updateUser_SelfDemote_ThrowsException() {
        UpdateUserRequest request = new UpdateUserRequest("Quản Trị Viên", Role.SINH_VIEN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> userService.updateUser(1L, request, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không thể tự thay đổi vai trò");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Khóa tài khoản người dùng khác thành công")
    void updateUserStatus_LockOtherUser_Success() {
        User otherUser = new User("bs01", "hash", "Bác sĩ B", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        otherUser.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(userRepository.save(any(User.class))).thenReturn(otherUser);

        UpdateUserStatusRequest request = new UpdateUserStatusRequest(UserStatus.LOCKED);
        UserResponse response = userService.updateUserStatus(2L, request, "admin01");

        assertThat(response.getStatus()).isEqualTo(UserStatus.LOCKED);
        verify(userRepository).save(otherUser);
    }

    @Test
    @DisplayName("Chặn tự khóa tài khoản của chính mình (Self-lock prevention)")
    void updateUserStatus_SelfLock_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UpdateUserStatusRequest request = new UpdateUserStatusRequest(UserStatus.LOCKED);
        assertThatThrownBy(() -> userService.updateUserStatus(1L, request, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không thể tự khóa tài khoản của chính mình");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Cấp lại mật khẩu mặc định thành công")
    void resetPassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode(UserServiceImpl.DEFAULT_PASSWORD)).thenReturn("new_hash");

        userService.resetPassword(1L);

        assertThat(sampleUser.getPassword()).isEqualTo("new_hash");
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Thao tác trên ID không tồn tại ném ResourceNotFoundException (404)")
    void notFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy người dùng");
    }
}
