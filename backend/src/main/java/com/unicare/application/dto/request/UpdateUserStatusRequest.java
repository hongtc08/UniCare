package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.unicare.domain.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserStatusRequest {

    @NotNull(message = "Trạng thái (status) không được để trống")
    private UserStatus status;

    public UpdateUserStatusRequest() {
    }

    public UpdateUserStatusRequest(UserStatus status) {
        this.status = status;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }
}
