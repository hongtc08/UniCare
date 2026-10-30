package com.unicare.application.service;

import com.unicare.application.dto.request.DiseaseGroupRequest;
import com.unicare.application.dto.response.DiseaseGroupResponse;
import com.unicare.application.service.impl.DiseaseGroupServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.DiseaseGroup;
import com.unicare.domain.repository.DiseaseGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiseaseGroupServiceImplTest {

    @Mock
    private DiseaseGroupRepository diseaseGroupRepository;

    @InjectMocks
    private DiseaseGroupServiceImpl diseaseGroupService;

    private DiseaseGroup sampleGroup;

    @BeforeEach
    void setUp() {
        sampleGroup = new DiseaseGroup(1, "CUM", "Bệnh cảm cúm");
    }

    @Test
    @DisplayName("Lấy danh sách nhóm bệnh thành công")
    void getDiseaseGroups_Success() {
        when(diseaseGroupRepository.searchDiseaseGroups("")).thenReturn(List.of(sampleGroup));

        List<DiseaseGroupResponse> result = diseaseGroupService.getDiseaseGroups(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(0).getGroupCode()).isEqualTo("CUM");
        assertThat(result.get(0).getGroupName()).isEqualTo("Bệnh cảm cúm");
    }

    @Test
    @DisplayName("Lấy thông tin chi tiết nhóm bệnh theo ID thành công")
    void getDiseaseGroupById_Success() {
        when(diseaseGroupRepository.findById(1)).thenReturn(Optional.of(sampleGroup));

        DiseaseGroupResponse result = diseaseGroupService.getDiseaseGroupById(1);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getGroupCode()).isEqualTo("CUM");
    }

    @Test
    @DisplayName("Lấy chi tiết nhóm bệnh thất bại khi không tìm thấy ID (404)")
    void getDiseaseGroupById_NotFound() {
        when(diseaseGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diseaseGroupService.getDiseaseGroupById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy");
    }

    @Test
    @DisplayName("Tạo mới nhóm bệnh thành công")
    void createDiseaseGroup_Success() {
        DiseaseGroupRequest request = new DiseaseGroupRequest("SXH", "Sốt xuất huyết");
        when(diseaseGroupRepository.existsByGroupCodeIgnoreCase("SXH")).thenReturn(false);
        when(diseaseGroupRepository.existsByGroupNameIgnoreCase("Sốt xuất huyết")).thenReturn(false);
        when(diseaseGroupRepository.save(any(DiseaseGroup.class))).thenAnswer(invocation -> {
            DiseaseGroup dg = invocation.getArgument(0);
            dg.setId(2);
            return dg;
        });

        DiseaseGroupResponse result = diseaseGroupService.createDiseaseGroup(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getGroupCode()).isEqualTo("SXH");
        assertThat(result.getGroupName()).isEqualTo("Sốt xuất huyết");
        verify(diseaseGroupRepository).save(any(DiseaseGroup.class));
    }

    @Test
    @DisplayName("Tạo nhóm bệnh thất bại do trùng mã group_code (409)")
    void createDiseaseGroup_DuplicateCode_ThrowsException() {
        DiseaseGroupRequest request = new DiseaseGroupRequest("CUM", "Cúm mùa");
        when(diseaseGroupRepository.existsByGroupCodeIgnoreCase("CUM")).thenReturn(true);

        assertThatThrownBy(() -> diseaseGroupService.createDiseaseGroup(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Mã nhóm bệnh 'CUM' đã tồn tại");

        verify(diseaseGroupRepository, never()).save(any(DiseaseGroup.class));
    }

    @Test
    @DisplayName("Tạo nhóm bệnh thất bại do trùng tên group_name (409)")
    void createDiseaseGroup_DuplicateName_ThrowsException() {
        DiseaseGroupRequest request = new DiseaseGroupRequest("CUM02", "Bệnh cảm cúm");
        when(diseaseGroupRepository.existsByGroupCodeIgnoreCase("CUM02")).thenReturn(false);
        when(diseaseGroupRepository.existsByGroupNameIgnoreCase("Bệnh cảm cúm")).thenReturn(true);

        assertThatThrownBy(() -> diseaseGroupService.createDiseaseGroup(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Tên nhóm bệnh 'Bệnh cảm cúm' đã tồn tại");

        verify(diseaseGroupRepository, never()).save(any(DiseaseGroup.class));
    }

    @Test
    @DisplayName("Tạo nhóm bệnh thất bại khi request null hoặc rỗng (400)")
    void createDiseaseGroup_EmptyFields_ThrowsException() {
        assertThatThrownBy(() -> diseaseGroupService.createDiseaseGroup(null))
                .isInstanceOf(BadRequestException.class);

        DiseaseGroupRequest emptyCode = new DiseaseGroupRequest("", "Tên bệnh");
        assertThatThrownBy(() -> diseaseGroupService.createDiseaseGroup(emptyCode))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Mã nhóm bệnh không được để trống");
    }

    @Test
    @DisplayName("Cập nhật nhóm bệnh thành công")
    void updateDiseaseGroup_Success() {
        DiseaseGroupRequest request = new DiseaseGroupRequest("CUM_A", "Cúm chủng A");
        when(diseaseGroupRepository.findById(1)).thenReturn(Optional.of(sampleGroup));
        when(diseaseGroupRepository.existsByGroupCodeIgnoreCaseAndIdNot("CUM_A", 1)).thenReturn(false);
        when(diseaseGroupRepository.existsByGroupNameIgnoreCaseAndIdNot("Cúm chủng A", 1)).thenReturn(false);
        when(diseaseGroupRepository.save(any(DiseaseGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiseaseGroupResponse result = diseaseGroupService.updateDiseaseGroup(1, request);

        assertThat(result.getGroupCode()).isEqualTo("CUM_A");
        assertThat(result.getGroupName()).isEqualTo("Cúm chủng A");
        verify(diseaseGroupRepository).save(sampleGroup);
    }

    @Test
    @DisplayName("Cập nhật nhóm bệnh thất bại do không tìm thấy ID (404)")
    void updateDiseaseGroup_NotFound() {
        DiseaseGroupRequest request = new DiseaseGroupRequest("CUM", "Cúm");
        when(diseaseGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diseaseGroupService.updateDiseaseGroup(99, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Xóa nhóm bệnh thành công")
    void deleteDiseaseGroup_Success() {
        when(diseaseGroupRepository.findById(1)).thenReturn(Optional.of(sampleGroup));

        diseaseGroupService.deleteDiseaseGroup(1);

        verify(diseaseGroupRepository).delete(sampleGroup);
    }

    @Test
    @DisplayName("Xóa nhóm bệnh thất bại khi không tìm thấy ID (404)")
    void deleteDiseaseGroup_NotFound() {
        when(diseaseGroupRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diseaseGroupService.deleteDiseaseGroup(99))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(diseaseGroupRepository, never()).delete(any());
    }
}

