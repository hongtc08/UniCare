/**
 * DTO rút gọn chứa thông tin Bác sĩ / Nhân viên y tế phục vụ hiển thị Dropdown trên Frontend.
 */
package com.unicare.application.dto.response;

import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;

public class DoctorOptionResponse {

    private Long id;
    private String username;
    private String name;
    private String fullName;
    private Role role;
    private UserStatus status;

    public DoctorOptionResponse() {
    }

    public DoctorOptionResponse(
            Long id,
            String username,
            String name,
            String fullName,
            Role role,
            UserStatus status
    ) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
    }

    public static DoctorOptionResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new DoctorOptionResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getFullName(),
                user.getRole(),
                user.getStatus()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
