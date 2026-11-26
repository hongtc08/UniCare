package com.unicare.presentation.controller;

import com.unicare.application.dto.response.Dass21ResultResponse;
import com.unicare.application.dto.response.Dass21ScorePointResponse;
import com.unicare.application.dto.response.HealthInsuranceResponse;
import com.unicare.application.dto.response.HealthProfileResponse;
import com.unicare.application.dto.response.MedicalCheckupSummaryResponse;
import com.unicare.application.dto.response.MoodLogHistoryResponse;
import com.unicare.application.dto.response.StudentMedicalOverviewResponse;
import com.unicare.application.dto.response.StudentMentalHealthHistoryResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.service.DoctorMentalHealthService;
import com.unicare.application.service.HealthProfileService;
import com.unicare.common.exception.BadRequestException;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DoctorStudentMedicalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HealthProfileService healthProfileService;

    @Mock
    private DoctorMentalHealthService doctorMentalHealthService;

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

    @Test
    @DisplayName("Kiểm tra annotation @PreAuthorize chỉ cho phép NHAN_VIEN_Y_TE truy cập Controller")
    void controllerRequiresDoctorRole() {
        PreAuthorize annotation = DoctorStudentMedicalController.class.getAnnotation(PreAuthorize.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("hasRole('NHAN_VIEN_Y_TE')");
    }

    @Test
    @DisplayName("GET /api/doctor/students/search - Tra cứu nhanh sinh viên theo keyword hoặc q thành công (200 OK)")
    void searchStudentsForEmergency_Success() throws Exception {
        StudentPersonalInfoResponse studentInfo = sampleOverview.getStudentInfo();
        when(healthProfileService.searchStudentsForDoctor("bs001", "24520579"))
                .thenReturn(List.of(studentInfo));

        mockMvc.perform(get("/api/doctor/students/search")
                        .param("keyword", "24520579")
                        .principal(doctorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].studentCode").value("24520579"))
                .andExpect(jsonPath("$.data[0].fullName").value("Tăng Chấn Hồng"));

        when(healthProfileService.searchStudentsForDoctor("bs001", "SE104"))
                .thenReturn(List.of(studentInfo));

        mockMvc.perform(get("/api/doctor/students/search")
                        .param("q", "SE104")
                        .principal(doctorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].classCode").value("SE104.O11"));
    }

    @Test
    @DisplayName("GET /api/doctor/students/{studentId}/mental-health-history, /dass21-history & /mood-logs - Thành công & Chặn phân quyền")
    void doctorMentalHealthEndpoints_Success_And_Forbidden() throws Exception {
        assertThat(new DoctorStudentMedicalController(healthProfileService)).isNotNull();

        Dass21ResultResponse dassRes = new Dass21ResultResponse();
        dassRes.setId(102L);
        dassRes.setStudentId(2L);
        dassRes.setStudentCode("24520579");
        dassRes.setDepressionScore(10);
        dassRes.setAnxietyScore(8);
        dassRes.setStressScore(12);
        dassRes.setTotalScore(30);
        dassRes.setDepressionLevel("Nhẹ");

        Dass21ScorePointResponse scorePoint = new Dass21ScorePointResponse();
        scorePoint.setId(102L);
        scorePoint.setDepressionScore(10);
        scorePoint.setAnxietyScore(8);
        scorePoint.setStressScore(12);
        scorePoint.setTotalScore(30);

        MoodLogHistoryResponse moodHistory = MoodLogHistoryResponse.builder()
                .period("MONTH")
                .totalLogs(2)
                .averageMoodLevel(3.5)
                .build();

        StudentMentalHealthHistoryResponse combined = new StudentMentalHealthHistoryResponse(
                sampleOverview.getStudentInfo(),
                1,
                dassRes,
                List.of(scorePoint),
                List.of(dassRes),
                moodHistory
        );

        when(doctorMentalHealthService.getStudentMentalHealthHistory("bs001", 2L, "MONTH", null, null))
                .thenReturn(combined);

        mockMvc.perform(get("/api/doctor/students/2/mental-health-history")
                        .param("period", "MONTH")
                        .principal(doctorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDass21Tests").value(1))
                .andExpect(jsonPath("$.data.latestDass21.depressionScore").value(10))
                .andExpect(jsonPath("$.data.dass21ScoreTrend[0].totalScore").value(30))
                .andExpect(jsonPath("$.data.moodHistory.averageMoodLevel").value(3.5));

        // Alias /{studentId}/mental-health và nhánh principal == null
        when(doctorMentalHealthService.getStudentMentalHealthHistory(null, 2L, null, null, null))
                .thenReturn(combined);
        mockMvc.perform(get("/api/doctor/students/2/mental-health"))
                .andExpect(status().isOk());

        // Chặn Ban quản lý (403) và Sinh viên khác (403)
        when(doctorMentalHealthService.getStudentMentalHealthHistory("admin", 2L, null, null, null))
                .thenThrow(new AccessDeniedException("Ban quản lý không được phép truy cập"));
        mockMvc.perform(get("/api/doctor/students/2/mental-health-history")
                        .principal(() -> "admin"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        when(doctorMentalHealthService.getStudentMentalHealthHistory("sv_other", 2L, null, null, null))
                .thenThrow(new AccessDeniedException("Chỉ Bác sĩ / Chuyên gia tâm lý"));
        mockMvc.perform(get("/api/doctor/students/2/mental-health-history")
                        .principal(() -> "sv_other"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        // GET /{studentId}/dass21-history (kèm lọc ngày và nhánh principal == null)
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate to = LocalDate.of(2026, 11, 26);
        when(doctorMentalHealthService.getStudentDass21History("bs001", 2L, from, to))
                .thenReturn(List.of(dassRes));
        mockMvc.perform(get("/api/doctor/students/2/dass21-history")
                        .param("fromDate", "2026-11-01")
                        .param("toDate", "2026-11-26")
                        .principal(doctorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(102));

        when(doctorMentalHealthService.getStudentDass21History(null, 2L, null, null))
                .thenReturn(List.of(dassRes));
        mockMvc.perform(get("/api/doctor/students/2/dass21"))
                .andExpect(status().isOk());

        // GET /{studentId}/mood-logs (200 OK, nhánh principal == null và 400 Bad Request)
        when(doctorMentalHealthService.getStudentMoodLogsForDoctor("bs001", 2L, "WEEK", null, null))
                .thenReturn(moodHistory);
        mockMvc.perform(get("/api/doctor/students/2/mood-logs")
                        .param("period", "WEEK")
                        .principal(doctorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averageMoodLevel").value(3.5));

        when(doctorMentalHealthService.getStudentMoodLogsForDoctor(null, 2L, null, null, null))
                .thenReturn(moodHistory);
        mockMvc.perform(get("/api/doctor/students/2/mood-logs"))
                .andExpect(status().isOk());

        when(doctorMentalHealthService.getStudentMoodLogsForDoctor("bs001", 2L, "INVALID", null, null))
                .thenThrow(new BadRequestException("Giá trị period không hợp lệ"));
        mockMvc.perform(get("/api/doctor/students/2/mood-logs")
                        .param("period", "INVALID")
                        .principal(doctorPrincipal))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
