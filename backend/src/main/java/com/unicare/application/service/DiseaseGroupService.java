package com.unicare.application.service;

import com.unicare.application.dto.request.DiseaseGroupRequest;
import com.unicare.application.dto.response.DiseaseGroupResponse;

import java.util.List;

public interface DiseaseGroupService {

    List<DiseaseGroupResponse> getDiseaseGroups(String search);

    DiseaseGroupResponse getDiseaseGroupById(Integer id);

    DiseaseGroupResponse createDiseaseGroup(DiseaseGroupRequest request);

    DiseaseGroupResponse updateDiseaseGroup(Integer id, DiseaseGroupRequest request);

    void deleteDiseaseGroup(Integer id);
}

