package com.unicare.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicare.application.dto.request.DiseaseGroupRequest;
import com.unicare.application.dto.response.DiseaseGroupResponse;
import com.unicare.application.service.DiseaseGroupService;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.presentation.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DiseaseGroupControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DiseaseGroupService diseaseGroupService;

    @InjectMocks
    private DiseaseGroupController diseaseGroupController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(diseaseGroupController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/disease-groups - Trả về danh sách nhóm bệnh bọc trong ApiResponse")
    void getDiseaseGroups_ReturnsList() throws Exception {
        DiseaseGroupResponse response = new DiseaseGroupResponse(1, "CUM", "Bệnh cảm cúm");
        when(diseaseGroupService.getDiseaseGroups(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/disease-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].groupCode").value("CUM"))
                .andExpect(jsonPath("$.data[0].groupName").value("Bệnh cảm cúm"))
                .andExpect(jsonPath("$.data[0].code").value("CUM"))
                .andExpect(jsonPath("$.data[0].name").value("Bệnh cảm cúm"));
    }

    @Test
    @DisplayName("GET /api/catalogs/disease-groups - Endpoint alias trả về danh sách nhóm bệnh")
    void getDiseaseGroups_CatalogAlias_ReturnsList() throws Exception {
        DiseaseGroupResponse response = new DiseaseGroupResponse(1, "CUM", "Bệnh cảm cúm");
        when(diseaseGroupService.getDiseaseGroups(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/catalogs/disease-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/disease-groups/{id} - Lấy chi tiết nhóm bệnh thành công")
    void getDiseaseGroupById_Success() throws Exception {
        DiseaseGroupResponse response = new DiseaseGroupResponse(1, "CUM", "Bệnh cảm cúm");
        when(diseaseGroupService.getDiseaseGroupById(1)).thenReturn(response);

        mockMvc.perform(get("/api/disease-groups/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.groupCode").value("CUM"));
    }

    @Test
    @DisplayName("GET /api/disease-groups/{id} - Không tìm thấy trả về 404")
    void getDiseaseGroupById_NotFound() throws Exception {
        when(diseaseGroupService.getDiseaseGroupById(99))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy nhóm bệnh với mã ID: 99"));

        mockMvc.perform(get("/api/disease-groups/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Không tìm thấy nhóm bệnh với mã ID: 99"));
    }

    @Test
    @DisplayName("POST /api/disease-groups - Thêm nhóm bệnh thành công trả về 201 Created")
    void createDiseaseGroup_Success() throws Exception {
        DiseaseGroupRequest request = new DiseaseGroupRequest("SXH", "Sốt xuất huyết");
        DiseaseGroupResponse response = new DiseaseGroupResponse(2, "SXH", "Sốt xuất huyết");

        when(diseaseGroupService.createDiseaseGroup(any(DiseaseGroupRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/disease-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.groupCode").value("SXH"))
                .andExpect(jsonPath("$.data.groupName").value("Sốt xuất huyết"));
    }

    @Test
    @DisplayName("POST /api/disease-groups - Dữ liệu không hợp lệ trả về 400 Bad Request")
    void createDiseaseGroup_InvalidInput_Returns400() throws Exception {
        DiseaseGroupRequest request = new DiseaseGroupRequest("", "");

        mockMvc.perform(post("/api/disease-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.groupCode").exists())
                .andExpect(jsonPath("$.data.groupName").exists());
    }

    @Test
    @DisplayName("POST /api/disease-groups - Trùng mã nhóm bệnh trả về 409 Conflict")
    void createDiseaseGroup_Duplicate_Returns409() throws Exception {
        DiseaseGroupRequest request = new DiseaseGroupRequest("CUM", "Cúm mùa");

        when(diseaseGroupService.createDiseaseGroup(any(DiseaseGroupRequest.class)))
                .thenThrow(new DuplicateResourceException("Mã nhóm bệnh 'CUM' đã tồn tại trên hệ thống."));

        mockMvc.perform(post("/api/disease-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Mã nhóm bệnh 'CUM' đã tồn tại trên hệ thống."));
    }

    @Test
    @DisplayName("PUT /api/disease-groups/{id} - Cập nhật thành công trả về 200 OK")
    void updateDiseaseGroup_Success() throws Exception {
        DiseaseGroupRequest request = new DiseaseGroupRequest("CUM_A", "Cúm A");
        DiseaseGroupResponse response = new DiseaseGroupResponse(1, "CUM_A", "Cúm A");

        when(diseaseGroupService.updateDiseaseGroup(eq(1), any(DiseaseGroupRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/disease-groups/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.groupCode").value("CUM_A"))
                .andExpect(jsonPath("$.data.groupName").value("Cúm A"));
    }

    @Test
    @DisplayName("DELETE /api/disease-groups/{id} - Xóa thành công trả về 200 OK")
    void deleteDiseaseGroup_Success() throws Exception {
        mockMvc.perform(delete("/api/disease-groups/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa nhóm bệnh thành công"));
    }

    @Test
    @DisplayName("DELETE /api/disease-groups/{id} - Không tìm thấy trả về 404 Not Found")
    void deleteDiseaseGroup_NotFound_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Không tìm thấy nhóm bệnh với mã ID: 99"))
                .when(diseaseGroupService).deleteDiseaseGroup(99);

        mockMvc.perform(delete("/api/disease-groups/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}

