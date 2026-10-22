package com.unicare.application.service;

import com.unicare.application.dto.request.CreateUserRequest;
import com.unicare.application.dto.request.UpdateUserRequest;
import com.unicare.application.dto.request.UpdateUserStatusRequest;
import com.unicare.application.dto.response.UserResponse;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;

import java.util.List;

public interface UserService {

    List<UserResponse> getUsers(String search, Role role, UserStatus status);

    UserResponse getUserById(Long id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request, String currentUsername);

    UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request, String currentUsername);

    void resetPassword(Long id);
}
