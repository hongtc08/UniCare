package com.unicare.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicare.application.dto.request.FacultyRequest;
import com.unicare.application.dto.response.FacultyResponse;
import com.unicare.application.service.FacultyService;
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
class FacultyControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FacultyService facultyService;

    @InjectMocks
    private FacultyController facultyController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(facultyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/faculties - Trả về danh sách khoa bọc trong ApiResponse")
    void getFaculties_ReturnsList() throws Exception {
        FacultyResponse response = new FacultyResponse(1, "CNPM", "Công nghệ phần mềm");
        when(facultyService.getFaculties(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/faculties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].facultyCode").value("CNPM"))
                .andExpect(jsonPath("$.data[0].facultyName").value("Công nghệ phần mềm"))
                .andExpect(jsonPath("$.data[0].code").value("CNPM"))
                .andExpect(jsonPath("$.data[0].name").value("Công nghệ phần mềm"));
    }

    @Test
    @DisplayName("GET /api/catalogs/faculties - Endpoint alias trả về danh sách khoa")
    void getFaculties_CatalogAlias_ReturnsList() throws Exception {
        FacultyResponse response = new FacultyResponse(1, "CNPM", "Công nghệ phần mềm");
        when(facultyService.getFaculties(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/catalogs/faculties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/faculties/{id} - Lấy chi tiết khoa thành công")
    void getFacultyById_Success() throws Exception {
        FacultyResponse response = new FacultyResponse(1, "CNPM", "Công nghệ phần mềm");
        when(facultyService.getFacultyById(1)).thenReturn(response);

        mockMvc.perform(get("/api/faculties/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.facultyCode").value("CNPM"));
    }

    @Test
    @DisplayName("GET /api/faculties/{id} - Không tìm thấy trả về 404")
    void getFacultyById_NotFound() throws Exception {
        when(facultyService.getFacultyById(99))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy khoa với mã ID: 99"));

        mockMvc.perform(get("/api/faculties/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/faculties - Tạo mới khoa thành công trả về 201 Created")
    void createFaculty_Success() throws Exception {
        FacultyRequest request = new FacultyRequest("KHMT", "Khoa học máy tính");
        FacultyResponse response = new FacultyResponse(2, "KHMT", "Khoa học máy tính");

        when(facultyService.createFaculty(any(FacultyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/faculties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.facultyCode").value("KHMT"));
    }

    @Test
    @DisplayName("POST /api/faculties - Dữ liệu rỗng trả về 400 Bad Request")
    void createFaculty_InvalidInput_Returns400() throws Exception {
        FacultyRequest request = new FacultyRequest("", "");

        mockMvc.perform(post("/api/faculties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/faculties - Trùng mã trả về 409 Conflict")
    void createFaculty_DuplicateCode_Returns409() throws Exception {
        FacultyRequest request = new FacultyRequest("CNPM", "Công nghệ phần mềm");

        when(facultyService.createFaculty(any(FacultyRequest.class)))
                .thenThrow(new DuplicateResourceException("Mã khoa 'CNPM' đã tồn tại trên hệ thống."));

        mockMvc.perform(post("/api/faculties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/faculties/{id} - Cập nhật thành công trả về 200 OK")
    void updateFaculty_Success() throws Exception {
        FacultyRequest request = new FacultyRequest("CNPM", "Kỹ thuật phần mềm");
        FacultyResponse response = new FacultyResponse(1, "CNPM", "Kỹ thuật phần mềm");

        when(facultyService.updateFaculty(eq(1), any(FacultyRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/faculties/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.facultyName").value("Kỹ thuật phần mềm"));
    }

    @Test
    @DisplayName("DELETE /api/faculties/{id} - Xóa thành công trả về 200 OK")
    void deleteFaculty_Success() throws Exception {
        mockMvc.perform(delete("/api/faculties/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa khoa thành công"));
    }

    @Test
    @DisplayName("DELETE /api/faculties/{id} - Đang có lớp học trực thuộc trả về 400 Bad Request")
    void deleteFaculty_HasClasses_Returns400() throws Exception {
        doThrow(new BadRequestException("Không thể xóa khoa vì đang có lớp học trực thuộc."))
                .when(facultyService).deleteFaculty(1);

        mockMvc.perform(delete("/api/faculties/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Không thể xóa khoa vì đang có lớp học trực thuộc."));
    }
}

