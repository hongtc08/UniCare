package com.unicare.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiseaseGroupRequest {

    @NotBlank(message = "Mã nhóm bệnh không được để trống")
    @Size(max = 10, message = "Mã nhóm bệnh không được vượt quá 10 ký tự")
    @JsonAlias({"code", "group_code"})
    private String groupCode;

    @NotBlank(message = "Tên nhóm bệnh không được để trống")
    @Size(max = 60, message = "Tên nhóm bệnh không được vượt quá 60 ký tự")
    @JsonAlias({"name", "group_name"})
    private String groupName;

    public DiseaseGroupRequest() {
    }

    public DiseaseGroupRequest(String groupCode, String groupName) {
        this.groupCode = groupCode;
        this.groupName = groupName;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}

