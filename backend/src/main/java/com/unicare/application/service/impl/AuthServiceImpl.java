package com.unicare.application.service.impl;

import com.unicare.application.dto.request.ChangePasswordRequest;
import com.unicare.application.dto.request.LoginRequest;
import com.unicare.application.dto.response.LoginResponse;
import com.unicare.application.service.AuthService;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.UserStatus;
import com.unicare.domain.repository.UserRepository;
import com.unicare.infrastructure.security.JwtTokenProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Tên đăng nhập hoặc mật khẩu không chính xác"));

        // Kiểm tra tài khoản có bị khóa thủ công bởi Ban quản lý không
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new LockedException("Tài khoản đã bị khóa, vui lòng liên hệ Ban quản lý");
        }

        // Kiểm tra mật khẩu băm BCrypt (Không tự động khóa khi nhập sai nhiều lần)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        String token = jwtTokenProvider.generateTokenFromUser(user);

        return new LoginResponse(
                token,
                user.getUsername(),
                user.getFullName(),
                user.getRole()
        );
    }

    @Override
    @Transactional
    public void changePassword(String currentUsername, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
