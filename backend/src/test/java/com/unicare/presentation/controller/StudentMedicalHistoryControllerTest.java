package com.unicare.presentation.controller;

import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.dto.response.PrescribedMedicineResponse;
import com.unicare.application.dto.response.TransferRecordResponse;
import com.unicare.application.service.MedicalCheckupService;
import com.unicare.domain.enums.ServiceType;
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
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StudentMedicalHistoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MedicalCheckupService medicalCheckupService;

    @InjectMocks
    private StudentMedicalHistoryController studentMedicalHistoryController;

    private Principal studentPrincipal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(studentMedicalHistoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        studentPrincipal = () -> "24521234";
    }

    @Test
    @DisplayName("GET /api/student/medical-history - Trả về dòng thời gian đầy đủ phiếu khám, đơn thuốc và chuyển viện (200 OK)")
    void getMyMedicalHistory_Success() throws Exception {
        MedicalCheckupDetailResponse emergencyWithTransfer = MedicalCheckupDetailResponse.builder()
                .id(2L)
                .checkupCode("PK2611040002")
                .checkupType("GENERAL")
                .timelineCategory("GENERAL")
                .isPeriodic(false)
                .isEmergency(true)
                .symptoms("Ngất xỉu khi vận động")
                .diagnosisNotes("Cơn tăng huyết áp kịch phát")
                .status("COMPLETED")
                .completedAt(OffsetDateTime.now())
                .doctorName("Bác sĩ A")
                .prescribedMedicines(Collections.emptyList())
                .transferRecord(TransferRecordResponse.builder()
                        .id(1L)
                        .recordCode("CV2611040001")
                        .checkupId(2L)
                        .targetHospital("Bệnh viện Đa khoa Khu vực Thủ Đức")
                        .reasonCategory("CAP_CUU_NANG")
                        .transferReason("Cần theo dõi tim mạch chuyên sâu")
                        .currentCondition("Đã tỉnh")
                        .firstAidNotes("Thở oxy")
                        .build())
                .build();

        MedicalCheckupDetailResponse generalWithPrescription = MedicalCheckupDetailResponse.builder()
                .id(1L)
                .checkupCode("PK2611040001")
                .checkupType("GENERAL")
                .timelineCategory("GENERAL")
                .isPeriodic(false)
                .isEmergency(false)
                .appointmentId(2L)
                .appointmentCode("LH2611040002")
                .shiftDate(LocalDate.of(2026, 11, 4))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .room("Phòng 101")
                .shiftType(ServiceType.KHAM_DA_KHOA)
                .symptoms("Ho khan, sốt nhẹ")
                .diagnosisNotes("Cảm cúm thông thường")
                .status("COMPLETED")
                .completedAt(OffsetDateTime.now().minusDays(2))
                .doctorName("Bác sĩ A")
                .prescribedMedicines(List.of(
                        PrescribedMedicineResponse.builder()
                                .id(10L)
                                .medicineName("Paracetamol 500mg")
                                .quantity(10)
                                .dosageInstruction("Uống 1 viên khi sốt")
                                .build()
                ))
                .build();

        when(medicalCheckupService.getStudentMedicalHistory(eq("24521234"), isNull()))
                .thenReturn(List.of(emergencyWithTransfer, generalWithPrescription));

        mockMvc.perform(get("/api/student/medical-history")
                        .principal(studentPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy lịch sử khám bệnh thành công"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].checkupCode").value("PK2611040002"))
                .andExpect(jsonPath("$.data[0].transferRecord.recordCode").value("CV2611040001"))
                .andExpect(jsonPath("$.data[0].transferRecord.targetHospital").value("Bệnh viện Đa khoa Khu vực Thủ Đức"))
                .andExpect(jsonPath("$.data[1].checkupCode").value("PK2611040001"))
                .andExpect(jsonPath("$.data[1].prescribedMedicines[0].medicineName").value("Paracetamol 500mg"))
                .andExpect(jsonPath("$.data[1].transferRecord").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/student/medical-history - Sinh viên chưa có lịch sử khám trả về mảng rỗng (200 OK)")
    void getMyMedicalHistory_Empty() throws Exception {
        when(medicalCheckupService.getStudentMedicalHistory(eq("24521234"), isNull()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/student/medical-history")
                        .principal(studentPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /api/student/medical-history - Chặn IDOR khi cố tình truyền studentId khác hoặc parameter pollution (403 Forbidden)")
    void getMyMedicalHistory_Forbidden_WhenAccessingOtherStudent() throws Exception {
        when(medicalCheckupService.getStudentMedicalHistory(eq("24521234"), eq(99L)))
                .thenThrow(new AccessDeniedException("Sinh viên tuyệt đối không được phép xem lịch sử khám của sinh viên khác"));

        mockMvc.perform(get("/api/student/medical-history")
                        .param("studentId", "99")
                        .principal(studentPrincipal))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        // Kiểm tra chặn xung đột giữa studentId và student_id ngay tại Controller
        mockMvc.perform(get("/api/student/medical-history")
                        .param("studentId", "2")
                        .param("student_id", "99")
                        .principal(studentPrincipal))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
        verify(medicalCheckupService, never()).getStudentMedicalHistory(eq("24521234"), eq(2L));
    }

    @Test
    @DisplayName("GET /api/student/medical-history - Kiểm tra annotation @PreAuthorize chỉ cho phép SINH_VIEN")
    void controllerRequiresStudentRole() {
        PreAuthorize annotation = StudentMedicalHistoryController.class.getAnnotation(PreAuthorize.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("hasRole('SINH_VIEN')");
    }
}
