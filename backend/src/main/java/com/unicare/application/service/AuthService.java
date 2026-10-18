package com.unicare.application.service;

import com.unicare.application.dto.request.ChangePasswordRequest;
import com.unicare.application.dto.request.LoginRequest;
import com.unicare.application.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void changePassword(String currentUsername, ChangePasswordRequest request);
}
