package com.unicare.application.dto.response;

import com.unicare.domain.enums.Role;

public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private String username;
    private String fullName;
    private Role role;

    public LoginResponse() {
    }

    public LoginResponse(String token, String username, String fullName, Role role) {
        this.token = token;
        this.tokenType = "Bearer";
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
