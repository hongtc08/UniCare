package com.unicare.application.dto.response;

import com.unicare.domain.entity.DiseaseGroup;

public class DiseaseGroupResponse {

    private Integer id;
    private String groupCode;
    private String groupName;

    public DiseaseGroupResponse() {
    }

    public DiseaseGroupResponse(Integer id, String groupCode, String groupName) {
        this.id = id;
        this.groupCode = groupCode;
        this.groupName = groupName;
    }

    public static DiseaseGroupResponse fromEntity(DiseaseGroup entity) {
        if (entity == null) {
            return null;
        }
        return new DiseaseGroupResponse(
                entity.getId(),
                entity.getGroupCode(),
                entity.getGroupName()
        );
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getCode() {
        return groupCode;
    }

    public String getName() {
        return groupName;
    }
}

