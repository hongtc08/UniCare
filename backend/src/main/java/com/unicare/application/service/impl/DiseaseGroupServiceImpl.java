package com.unicare.application.service.impl;

import com.unicare.application.dto.request.DiseaseGroupRequest;
import com.unicare.application.dto.response.DiseaseGroupResponse;
import com.unicare.application.service.DiseaseGroupService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
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
        String cleanSearch = search == null ? "" : search.trim();
        List<DiseaseGroup> list = diseaseGroupRepository.searchDiseaseGroups(cleanSearch);
        return list.stream()
                .map(DiseaseGroupResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseGroupResponse getDiseaseGroupById(Integer id) {
        DiseaseGroup group = diseaseGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm bệnh với mã ID: " + id));
        return DiseaseGroupResponse.fromEntity(group);
    }

    @Override
    @Transactional
    public DiseaseGroupResponse createDiseaseGroup(DiseaseGroupRequest request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu nhóm bệnh không được để trống");
        }

        String cleanCode = request.getGroupCode() != null ? request.getGroupCode().trim() : "";
        String cleanName = request.getGroupName() != null ? request.getGroupName().trim() : "";

        if (cleanCode.isEmpty()) {
            throw new BadRequestException("Mã nhóm bệnh không được để trống");
        }
        if (cleanName.isEmpty()) {
            throw new BadRequestException("Tên nhóm bệnh không được để trống");
        }
        if (cleanCode.length() > 10) {
            throw new BadRequestException("Mã nhóm bệnh không được vượt quá 10 ký tự");
        }
        if (cleanName.length() > 60) {
            throw new BadRequestException("Tên nhóm bệnh không được vượt quá 60 ký tự");
        }

        if (diseaseGroupRepository.existsByGroupCodeIgnoreCase(cleanCode)) {
            throw new DuplicateResourceException("Mã nhóm bệnh '" + cleanCode + "' đã tồn tại trên hệ thống.");
        }
        if (diseaseGroupRepository.existsByGroupNameIgnoreCase(cleanName)) {
            throw new DuplicateResourceException("Tên nhóm bệnh '" + cleanName + "' đã tồn tại trên hệ thống.");
        }

        DiseaseGroup diseaseGroup = DiseaseGroup.builder()
                .groupCode(cleanCode)
                .groupName(cleanName)
                .build();

        DiseaseGroup savedGroup = diseaseGroupRepository.save(diseaseGroup);
        return DiseaseGroupResponse.fromEntity(savedGroup);
    }

    @Override
    @Transactional
    public DiseaseGroupResponse updateDiseaseGroup(Integer id, DiseaseGroupRequest request) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu nhóm bệnh không được để trống");
        }

        DiseaseGroup diseaseGroup = diseaseGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm bệnh với mã ID: " + id));

        String cleanCode = request.getGroupCode() != null ? request.getGroupCode().trim() : "";
        String cleanName = request.getGroupName() != null ? request.getGroupName().trim() : "";

        if (cleanCode.isEmpty()) {
            throw new BadRequestException("Mã nhóm bệnh không được để trống");
        }
        if (cleanName.isEmpty()) {
            throw new BadRequestException("Tên nhóm bệnh không được để trống");
        }
        if (cleanCode.length() > 10) {
            throw new BadRequestException("Mã nhóm bệnh không được vượt quá 10 ký tự");
        }
        if (cleanName.length() > 60) {
            throw new BadRequestException("Tên nhóm bệnh không được vượt quá 60 ký tự");
        }

        if (diseaseGroupRepository.existsByGroupCodeIgnoreCaseAndIdNot(cleanCode, id)) {
            throw new DuplicateResourceException("Mã nhóm bệnh '" + cleanCode + "' đã tồn tại trên hệ thống.");
        }
        if (diseaseGroupRepository.existsByGroupNameIgnoreCaseAndIdNot(cleanName, id)) {
            throw new DuplicateResourceException("Tên nhóm bệnh '" + cleanName + "' đã tồn tại trên hệ thống.");
        }

        diseaseGroup.setGroupCode(cleanCode);
        diseaseGroup.setGroupName(cleanName);

        DiseaseGroup updatedGroup = diseaseGroupRepository.save(diseaseGroup);
        return DiseaseGroupResponse.fromEntity(updatedGroup);
    }

    @Override
    @Transactional
    public void deleteDiseaseGroup(Integer id) {
        DiseaseGroup diseaseGroup = diseaseGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm bệnh với mã ID: " + id));

        diseaseGroupRepository.delete(diseaseGroup);
    }
}

