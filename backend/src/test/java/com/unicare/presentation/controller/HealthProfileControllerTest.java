/**
 * Unit test kiểm thử các endpoint GET/PUT của HealthProfileController.
 */
package com.unicare.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicare.application.dto.request.HealthProfileRequest;
import com.unicare.application.dto.response.HealthProfileResponse;
import com.unicare.application.service.HealthProfileService;
import com.unicare.presentation.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HealthProfileControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HealthProfileService healthProfileService;

    @InjectMocks
    private HealthProfileController healthProfileController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(healthProfileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/health-profile - Sinh viên lấy hồ sơ sức khỏe thành công")
    void getHealthProfile_Success() throws Exception {
        HealthProfileResponse response = new HealthProfileResponse();
        response.setId(1L);
        response.setStudentId(2L);
        response.setUsername("24520579");
        response.setFullName("Tăng Chấn Hồng");
        response.setHeight(170.0);
        response.setWeight(65.0);
        response.setBloodType("O");
        response.setBmi(22.5);
        response.setBmiClassification("Bình thường");
        response.setAllergies("Không");

        when(healthProfileService.getHealthProfile("24520579", null)).thenReturn(response);

        mockMvc.perform(get("/api/health-profile")
                        .principal(() -> "24520579"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(2))
                .andExpect(jsonPath("$.data.height").value(170.0))
                .andExpect(jsonPath("$.data.weight").value(65.0))
                .andExpect(jsonPath("$.data.bloodType").value("O"))
                .andExpect(jsonPath("$.data.bmi").value(22.5))
                .andExpect(jsonPath("$.data.bmiClassification").value("Bình thường"));
    }

    @Test
    @DisplayName("PUT /api/health-profile - Cập nhật hồ sơ sức khỏe và tự tính BMI thành công")
    void updateHealthProfile_Success() throws Exception {
        HealthProfileRequest request = new HealthProfileRequest(
                170.0,
                65.0,
                "AB",
                "Dị ứng phấn hoa",
                "Không có",
                "Đã tiêm đủ"
        );

        HealthProfileResponse response = new HealthProfileResponse();
        response.setId(1L);
        response.setStudentId(2L);
        response.setHeight(170.0);
        response.setWeight(65.0);
        response.setBloodType("AB");
        response.setBmi(22.5);
        response.setBmiClassification("Bình thường");
        response.setAllergies("Dị ứng phấn hoa");
        response.setUnderlyingDiseases("Không có");
        response.setVaccinationHistory("Đã tiêm đủ");

        when(healthProfileService.upsertHealthProfile(eq("24520579"), isNull(), any(HealthProfileRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/health-profile")
                        .principal(() -> "24520579")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật hồ sơ sức khỏe thành công"))
                .andExpect(jsonPath("$.data.bmi").value(22.5))
                .andExpect(jsonPath("$.data.bmiClassification").value("Bình thường"))
                .andExpect(jsonPath("$.data.bloodType").value("AB"));
    }

    @Test
    @DisplayName("PUT /api/health-profile/{studentId} - Bác sĩ cập nhật hồ sơ sức khỏe cho sinh viên")
    void updateHealthProfileByStudentId_Doctor_Success() throws Exception {
        HealthProfileRequest request = new HealthProfileRequest(165.0, 48.0, "A", null, null, null);
        HealthProfileResponse response = new HealthProfileResponse();
        response.setStudentId(2L);
        response.setHeight(165.0);
        response.setWeight(48.0);
        response.setBloodType("A");
        response.setBmi(17.6);
        response.setBmiClassification("Gầy");

        when(healthProfileService.upsertHealthProfile(eq("NV001"), eq(2L), any(HealthProfileRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/health-profile/2")
                        .principal(() -> "NV001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bmi").value(17.6))
                .andExpect(jsonPath("$.data.bmiClassification").value("Gầy"));
    }

    @Test
    @DisplayName("PUT /api/health-profile - Chiều cao và cân nặng <= 0 hoặc nhóm máu sai trả về 400 Bad Request")
    void updateHealthProfile_InvalidInput_Returns400() throws Exception {
        HealthProfileRequest invalidRequest = new HealthProfileRequest(
                -10.0,
                0.0,
                "INVALID_BLOOD",
                null,
                null,
                null
        );

        mockMvc.perform(put("/api/health-profile")
                        .principal(() -> "24520579")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.height").exists())
                .andExpect(jsonPath("$.data.weight").exists())
                .andExpect(jsonPath("$.data.bloodType").exists());
    }

    @Test
    @DisplayName("GET /api/health-profile/{studentId} - Sinh viên xem hồ sơ của người khác trả về 403 Forbidden")
    void getHealthProfile_UnauthorizedStudent_Returns403() throws Exception {
        when(healthProfileService.getHealthProfile("24520579", 99L))
                .thenThrow(new AccessDeniedException("Sinh viên chỉ được phép xem và cập nhật hồ sơ sức khỏe của chính mình"));

        mockMvc.perform(get("/api/health-profile/99")
                        .principal(() -> "24520579"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
