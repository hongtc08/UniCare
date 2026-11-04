/**
 * Unit test kiểm thử các endpoint GET/PUT của HealthProfileController (bao gồm hồ sơ sinh học và thông tin cá nhân).
 */
package com.unicare.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicare.application.dto.request.HealthProfileRequest;
import com.unicare.application.dto.request.StudentPersonalInfoRequest;
import com.unicare.application.dto.response.HealthProfileResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
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
    @DisplayName("GET /api/health-profile/personal-info & /{studentId} - Lấy thông tin cá nhân sinh viên thành công")
    void getStudentPersonalInfo_Success() throws Exception {
        StudentPersonalInfoResponse response = new StudentPersonalInfoResponse();
        response.setStudentId(2L);
        response.setUsername("24520579");
        response.setStudentCode("24520579");
        response.setFullName("Tăng Chấn Hồng");
        response.setDateOfBirth(LocalDate.of(2006, 5, 15));
        response.setGender("Nam");
        response.setPhoneNumber("0901234567");
        response.setEmail("24520579@gm.uit.edu.vn");
        response.setClassCode("SE104.O11");
        response.setClassName("Kỹ thuật phần mềm K19");
        response.setFacultyCode("CNPM");
        response.setFacultyName("Công nghệ Phần mềm");

        when(healthProfileService.getStudentPersonalInfo("24520579", null)).thenReturn(response);
        when(healthProfileService.getStudentPersonalInfo("NV001", 2L)).thenReturn(response);
        when(healthProfileService.getStudentPersonalInfo(null, 2L)).thenReturn(response);

        mockMvc.perform(get("/api/health-profile/personal-info")
                        .principal(() -> "24520579"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(2))
                .andExpect(jsonPath("$.data.fullName").value("Tăng Chấn Hồng"))
                .andExpect(jsonPath("$.data.gender").value("Nam"))
                .andExpect(jsonPath("$.data.phoneNumber").value("0901234567"))
                .andExpect(jsonPath("$.data.email").value("24520579@gm.uit.edu.vn"))
                .andExpect(jsonPath("$.data.className").value("Kỹ thuật phần mềm K19"))
                .andExpect(jsonPath("$.data.facultyName").value("Công nghệ Phần mềm"));

        mockMvc.perform(get("/api/health-profile/personal-info/2")
                        .principal(() -> "NV001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(2));

        assertThat(healthProfileController.getStudentPersonalInfo(2L, null, null, null).getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(healthProfileController.getStudentPersonalInfo(null, 2L, null, null).getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(healthProfileController.getStudentPersonalInfoByStudentId(2L, null).getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    @DisplayName("PUT /api/health-profile/personal-info & /{studentId} - Cập nhật thông tin cá nhân sinh viên thành công")
    void updateStudentPersonalInfo_Success() throws Exception {
        String requestJson = """
                {
                  "fullName": "Tăng Chấn Hồng",
                  "dateOfBirth": "2006-05-15",
                  "gender": "Nam",
                  "phoneNumber": "0909888777",
                  "email": "hongtc@gm.uit.edu.vn"
                }
                """;

        StudentPersonalInfoResponse response = new StudentPersonalInfoResponse();
        response.setStudentId(2L);
        response.setFullName("Tăng Chấn Hồng");
        response.setDateOfBirth(LocalDate.of(2006, 5, 15));
        response.setGender("Nam");
        response.setPhoneNumber("0909888777");
        response.setEmail("hongtc@gm.uit.edu.vn");

        when(healthProfileService.updateStudentPersonalInfo(eq("24520579"), isNull(), any(StudentPersonalInfoRequest.class)))
                .thenReturn(response);
        when(healthProfileService.updateStudentPersonalInfo(eq("NV001"), eq(2L), any(StudentPersonalInfoRequest.class)))
                .thenReturn(response);
        when(healthProfileService.updateStudentPersonalInfo(isNull(), eq(2L), any(StudentPersonalInfoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/health-profile/personal-info")
                        .principal(() -> "24520579")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật thông tin cá nhân sinh viên thành công"))
                .andExpect(jsonPath("$.data.gender").value("Nam"))
                .andExpect(jsonPath("$.data.phoneNumber").value("0909888777"))
                .andExpect(jsonPath("$.data.email").value("hongtc@gm.uit.edu.vn"));

        mockMvc.perform(put("/api/health-profile/personal-info/2")
                        .principal(() -> "NV001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(2));

        StudentPersonalInfoRequest dto = new StudentPersonalInfoRequest();
        assertThat(healthProfileController.updateStudentPersonalInfo(2L, null, null, dto, null).getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(healthProfileController.updateStudentPersonalInfoByStudentId(2L, dto, null).getStatusCode().is2xxSuccessful()).isTrue();
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
