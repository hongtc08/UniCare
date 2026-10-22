package com.unicare.application.service.impl;

import com.unicare.application.dto.request.CreateUserRequest;
import com.unicare.application.dto.request.UpdateUserRequest;
import com.unicare.application.dto.request.UpdateUserStatusRequest;
import com.unicare.application.dto.response.UserResponse;
import com.unicare.application.service.UserService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;
import com.unicare.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    public static final String DEFAULT_PASSWORD = "UniCare@123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(String search, Role role, UserStatus status) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        List<User> users = userRepository.searchUsers(cleanSearch, role, status);
        return users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với mã ID: " + id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String cleanUsername = request.getUsername().trim();
        if (userRepository.existsByUsernameIgnoreCase(cleanUsername)) {
            throw new DuplicateResourceException("Tên đăng nhập '" + cleanUsername + "' đã tồn tại trên hệ thống.");
        }

        String rawPassword = (request.getPassword() != null && !request.getPassword().trim().isEmpty())
                ? request.getPassword().trim()
                : DEFAULT_PASSWORD;

        User user = new User(
                cleanUsername,
                passwordEncoder.encode(rawPassword),
                request.getFullName().trim(),
                request.getRole(),
                UserStatus.ACTIVE
        );

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với mã ID: " + id));

        // Ràng buộc bảo mật: Chặn tự thay đổi vai trò của chính tài khoản đang đăng nhập
        if (currentUsername != null && !currentUsername.isBlank()
                && user.getUsername().equalsIgnoreCase(currentUsername)
                && user.getRole() != request.getRole()) {
            throw new BadRequestException("Bạn không thể tự thay đổi vai trò của chính mình.");
        }

        user.setFullName(request.getFullName().trim());
        user.setRole(request.getRole());

        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với mã ID: " + id));

        // Ràng buộc bảo mật: Chặn tự khóa tài khoản của chính mình (Self-lock prevention)
        if (currentUsername != null && !currentUsername.isBlank()
                && user.getUsername().equalsIgnoreCase(currentUsername)
                && request.getStatus() == UserStatus.LOCKED) {
            throw new BadRequestException("Bạn không thể tự khóa tài khoản của chính mình.");
        }

        user.setStatus(request.getStatus());
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với mã ID: " + id));

        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        userRepository.save(user);
    }
}
