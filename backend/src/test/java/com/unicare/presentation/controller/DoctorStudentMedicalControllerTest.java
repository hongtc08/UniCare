package com.unicare.presentation.controller;

import com.unicare.application.dto.response.HealthInsuranceResponse;
import com.unicare.application.dto.response.HealthProfileResponse;
import com.unicare.application.dto.response.MedicalCheckupSummaryResponse;
import com.unicare.application.dto.response.StudentMedicalOverviewResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.service.HealthProfileService;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.presentation.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DoctorStudentMedicalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HealthProfileService healthProfileService;

    @InjectMocks
    private DoctorStudentMedicalController doctorStudentMedicalController;

    private Principal doctorPrincipal;
    private StudentMedicalOverviewResponse sampleOverview;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(doctorStudentMedicalController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        doctorPrincipal = () -> "bs001";

        StudentPersonalInfoResponse studentInfo = new StudentPersonalInfoResponse();
        studentInfo.setStudentId(2L);
        studentInfo.setStudentCode("24520579");
        studentInfo.setFullName("Tăng Chấn Hồng");
        studentInfo.setClassCode("SE104.O11");
        studentInfo.setFacultyName("Công nghệ Phần mềm");

        HealthProfileResponse healthProfile = new HealthProfileResponse();
        healthProfile.setId(10L);
        healthProfile.setHeight(170.0);
        healthProfile.setWeight(65.0);
        healthProfile.setBmi(22.5);
        healthProfile.setBmiClassification("Bình thường");
        healthProfile.setAllergies("Dị ứng hải sản");

        HealthInsuranceResponse insurance = new HealthInsuranceResponse();
        insurance.setId(20L);
        insurance.setCardNumber("SV4792452057900");
        insurance.setStatus("CON_HAN");

        MedicalCheckupSummaryResponse checkup = new MedicalCheckupSummaryResponse();
        checkup.setId(50L);
        checkup.setCheckupCode("PK2611120001");
        checkup.setCheckupType("KHAM_DA_KHOA");
        checkup.setDiagnosisNotes("Viêm họng cấp");

        sampleOverview = new StudentMedicalOverviewResponse(
                studentInfo,
                healthProfile,
                insurance,
                List.of(checkup)
        );
    }

    @Test
    @DisplayName("GET /api/doctor/students/{studentId}/medical-overview - Bác sĩ tra cứu thành công (200 OK)")
    void getStudentMedicalOverview_Success() throws Exception {
        when(healthProfileService.getStudentMedicalOverview("bs001", 2L))
                .thenReturn(sampleOverview);

        mockMvc.perform(get("/api/doctor/students/2/medical-overview")
                        .principal(doctorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentInfo.studentCode").value("24520579"))
                .andExpect(jsonPath("$.data.studentInfo.classCode").value("SE104.O11"))
                .andExpect(jsonPath("$.data.studentInfo.facultyName").value("Công nghệ Phần mềm"))
                .andExpect(jsonPath("$.data.healthProfile.bmi").value(22.5))
                .andExpect(jsonPath("$.data.healthProfile.allergies").value("Dị ứng hải sản"))
                .andExpect(jsonPath("$.data.healthInsurance.cardNumber").value("SV4792452057900"))
                .andExpect(jsonPath("$.data.previousCheckups[0].checkupCode").value("PK2611120001"));
    }

    @Test
    @DisplayName("GET /api/doctor/students/{studentId}/medical-overview - Chặn Ban quản lý truy cập (403 Forbidden)")
    void getStudentMedicalOverview_AdminForbidden() throws Exception {
        Principal adminPrincipal = () -> "admin";
        when(healthProfileService.getStudentMedicalOverview("admin", 2L))
                .thenThrow(new AccessDeniedException("Ban quản lý không được phép truy cập dữ liệu bệnh án cá nhân của sinh viên"));

        mockMvc.perform(get("/api/doctor/students/2/medical-overview")
                        .principal(adminPrincipal))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/doctor/students/{studentId}/medical-overview - Lỗi 404 khi không tìm thấy sinh viên")
    void getStudentMedicalOverview_NotFound() throws Exception {
        when(healthProfileService.getStudentMedicalOverview("bs001", 999L))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy sinh viên với mã ID/MSSV: 999"));

        mockMvc.perform(get("/api/doctor/students/999/medical-overview")
                        .principal(doctorPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/doctor/students/{studentId}/medical-overview - Principal null")
    void getStudentMedicalOverview_NullPrincipal() throws Exception {
        when(healthProfileService.getStudentMedicalOverview(null, 2L))
                .thenReturn(sampleOverview);

        mockMvc.perform(get("/api/doctor/students/2/medical-overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
