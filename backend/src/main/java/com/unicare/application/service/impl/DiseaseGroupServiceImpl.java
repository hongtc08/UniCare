package com.unicare.application.service.impl;

import com.unicare.application.dto.request.DiseaseGroupRequest;
import com.unicare.application.dto.response.DiseaseGroupResponse;
import com.unicare.application.service.DiseaseGroupService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.common.util.ValidationUtils;
import com.unicare.domain.entity.DiseaseGroup;
import com.unicare.domain.repository.DiseaseGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiseaseGroupServiceImpl implements DiseaseGroupService {

    private final DiseaseGroupRepository diseaseGroupRepository;

    public DiseaseGroupServiceImpl(DiseaseGroupRepository diseaseGroupRepository) {
        this.diseaseGroupRepository = diseaseGroupRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiseaseGroupResponse> getDiseaseGroups(String search) {
        return diseaseGroupRepository.searchDiseaseGroups(ValidationUtils.cleanSearch(search)).stream()
                .map(DiseaseGroupResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseGroupResponse getDiseaseGroupById(Integer id) {
        return DiseaseGroupResponse.fromEntity(findGroupById(id));
    }

    @Override
    @Transactional
    public DiseaseGroupResponse createDiseaseGroup(DiseaseGroupRequest request) {
        validateRequestNotNull(request);
        String cleanCode = ValidationUtils.requireBoundedText(request.getGroupCode(), 10, "Mã nhóm bệnh");
        String cleanName = ValidationUtils.requireBoundedText(request.getGroupName(), 60, "Tên nhóm bệnh");

        if (diseaseGroupRepository.existsByGroupCodeIgnoreCase(cleanCode)) {
            throw new DuplicateResourceException("Mã nhóm bệnh '" + cleanCode + "' đã tồn tại trên hệ thống.");
        }
        if (diseaseGroupRepository.existsByGroupNameIgnoreCase(cleanName)) {
            throw new DuplicateResourceException("Tên nhóm bệnh '" + cleanName + "' đã tồn tại trên hệ thống.");
        }

        DiseaseGroup savedGroup = diseaseGroupRepository.save(new DiseaseGroup(cleanCode, cleanName));
        return DiseaseGroupResponse.fromEntity(savedGroup);
    }

    @Override
    @Transactional
    public DiseaseGroupResponse updateDiseaseGroup(Integer id, DiseaseGroupRequest request) {
        validateRequestNotNull(request);
        DiseaseGroup diseaseGroup = findGroupById(id);

        String cleanCode = ValidationUtils.requireBoundedText(request.getGroupCode(), 10, "Mã nhóm bệnh");
        String cleanName = ValidationUtils.requireBoundedText(request.getGroupName(), 60, "Tên nhóm bệnh");

        if (diseaseGroupRepository.existsByGroupCodeIgnoreCaseAndIdNot(cleanCode, id)) {
            throw new DuplicateResourceException("Mã nhóm bệnh '" + cleanCode + "' đã tồn tại trên hệ thống.");
        }
        if (diseaseGroupRepository.existsByGroupNameIgnoreCaseAndIdNot(cleanName, id)) {
            throw new DuplicateResourceException("Tên nhóm bệnh '" + cleanName + "' đã tồn tại trên hệ thống.");
        }

        diseaseGroup.setGroupCode(cleanCode);
        diseaseGroup.setGroupName(cleanName);

        return DiseaseGroupResponse.fromEntity(diseaseGroupRepository.save(diseaseGroup));
    }

    @Override
    @Transactional
    public void deleteDiseaseGroup(Integer id) {
        diseaseGroupRepository.delete(findGroupById(id));
    }

    private DiseaseGroup findGroupById(Integer id) {
        return diseaseGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm bệnh với mã ID: " + id));
    }

    private void validateRequestNotNull(DiseaseGroupRequest request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu nhóm bệnh không được để trống");
        }
    }
}
