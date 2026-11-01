package com.unicare.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicare.application.dto.request.AcademicClassRequest;
import com.unicare.application.dto.response.AcademicClassResponse;
import com.unicare.application.service.AcademicClassService;
import com.unicare.common.exception.BadRequestException;
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
class AcademicClassControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AcademicClassService academicClassService;

    @InjectMocks
    private AcademicClassController academicClassController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(academicClassController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/classes - Trả về danh sách lớp học bọc trong ApiResponse")
    void getClasses_ReturnsList() throws Exception {
        AcademicClassResponse response = new AcademicClassResponse(
                1, "SE104.O11", "Kỹ thuật phần mềm 1", "K19", 1, "CNPM", "Công nghệ phần mềm"
        );
        when(academicClassService.getClasses(null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].classCode").value("SE104.O11"))
                .andExpect(jsonPath("$.data[0].className").value("Kỹ thuật phần mềm 1"))
                .andExpect(jsonPath("$.data[0].code").value("SE104.O11"))
                .andExpect(jsonPath("$.data[0].name").value("Kỹ thuật phần mềm 1"))
                .andExpect(jsonPath("$.data[0].cohort").value("K19"))
                .andExpect(jsonPath("$.data[0].facultyId").value(1));
    }

    @Test
    @DisplayName("GET /api/catalogs/classes - Endpoint alias trả về danh sách lớp")
    void getClasses_CatalogAlias_ReturnsList() throws Exception {
        AcademicClassResponse response = new AcademicClassResponse(
                1, "SE104.O11", "Kỹ thuật phần mềm 1", "K19", 1, "CNPM", "Công nghệ phần mềm"
        );
        when(academicClassService.getClasses(null, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/catalogs/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/classes/{id} - Lấy chi tiết lớp học thành công")
    void getClassById_Success() throws Exception {
        AcademicClassResponse response = new AcademicClassResponse(
                1, "SE104.O11", "Kỹ thuật phần mềm 1", "K19", 1, "CNPM", "Công nghệ phần mềm"
        );
        when(academicClassService.getClassById(1)).thenReturn(response);

        mockMvc.perform(get("/api/classes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.classCode").value("SE104.O11"));
    }

    @Test
    @DisplayName("GET /api/classes/{id} - Không tìm thấy trả về 404")
    void getClassById_NotFound() throws Exception {
        when(academicClassService.getClassById(99))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy lớp học với mã ID: 99"));

        mockMvc.perform(get("/api/classes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/classes - Tạo mới lớp học thành công trả về 201 Created")
    void createClass_Success() throws Exception {
        AcademicClassRequest request = new AcademicClassRequest("SE104.O12", "Kỹ thuật phần mềm 2", "K19", 1);
        AcademicClassResponse response = new AcademicClassResponse(
                2, "SE104.O12", "Kỹ thuật phần mềm 2", "K19", 1, "CNPM", "Công nghệ phần mềm"
        );

        when(academicClassService.createClass(any(AcademicClassRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.classCode").value("SE104.O12"));
    }

    @Test
    @DisplayName("POST /api/classes - Dữ liệu không hợp lệ trả về 400 Bad Request")
    void createClass_InvalidInput_Returns400() throws Exception {
        AcademicClassRequest request = new AcademicClassRequest("", "", "", null);

        mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/classes - Trùng mã trả về 409 Conflict")
    void createClass_DuplicateCode_Returns409() throws Exception {
        AcademicClassRequest request = new AcademicClassRequest("SE104.O11", "Kỹ thuật phần mềm 1", "K19", 1);

        when(academicClassService.createClass(any(AcademicClassRequest.class)))
                .thenThrow(new DuplicateResourceException("Mã lớp 'SE104.O11' đã tồn tại trên hệ thống."));

        mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/classes/{id} - Cập nhật thành công trả về 200 OK")
    void updateClass_Success() throws Exception {
        AcademicClassRequest request = new AcademicClassRequest("SE104.O11", "Kỹ thuật phần mềm Đã Sửa", "K20", 1);
        AcademicClassResponse response = new AcademicClassResponse(
                1, "SE104.O11", "Kỹ thuật phần mềm Đã Sửa", "K20", 1, "CNPM", "Công nghệ phần mềm"
        );

        when(academicClassService.updateClass(eq(1), any(AcademicClassRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/classes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.className").value("Kỹ thuật phần mềm Đã Sửa"));
    }

    @Test
    @DisplayName("DELETE /api/classes/{id} - Xóa thành công trả về 200 OK")
    void deleteClass_Success() throws Exception {
        mockMvc.perform(delete("/api/classes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa lớp học thành công"));
    }

    @Test
    @DisplayName("DELETE /api/classes/{id} - Có sinh viên thuộc lớp trả về 400 Bad Request")
    void deleteClass_HasStudents_Returns400() throws Exception {
        doThrow(new BadRequestException("Không thể xóa lớp học vì đang có sinh viên thuộc lớp này."))
                .when(academicClassService).deleteClass(1);

        mockMvc.perform(delete("/api/classes/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Không thể xóa lớp học vì đang có sinh viên thuộc lớp này."));
    }
}

