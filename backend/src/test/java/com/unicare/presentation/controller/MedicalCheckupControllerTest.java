package com.unicare.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicare.application.dto.request.PrescribedMedicineRequest;
import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.dto.response.PrescribedMedicineResponse;
import com.unicare.application.service.MedicalCheckupService;
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
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MedicalCheckupControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MedicalCheckupService medicalCheckupService;

    @InjectMocks
    private MedicalCheckupController medicalCheckupController;

    private MedicalCheckupDetailResponse sampleDetailResponse;
    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(medicalCheckupController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        mockPrincipal = () -> "bs001";

        sampleDetailResponse = MedicalCheckupDetailResponse.builder()
                .id(1L)
                .checkupCode("PK2611040001")
                .studentId(10L)
                .studentCode("24521234")
                .studentName("Nguyễn Văn Sinh Viên")
                .staffId(20L)
                .doctorName("Bác sĩ Nguyễn Văn A")
                .status("DRAFT")
                .prescribedMedicines(List.of(
                        PrescribedMedicineResponse.builder()
                                .id(100L)
                                .medicineName("Paracetamol 500mg")
                                .quantity(10)
                                .dosageInstruction("Uống 1 viên khi sốt")
                                .build()
                ))
                .build();
    }

    @Test
    @DisplayName("GET /api/medical-records/{id}: Lấy chi tiết phiếu khám thành công (200 OK)")
    void getCheckupDetail_Success() throws Exception {
        when(medicalCheckupService.getCheckupDetail(eq(1L), eq("bs001"))).thenReturn(sampleDetailResponse);

        mockMvc.perform(get("/api/medical-records/1")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.checkupCode").value("PK2611040001"))
                .andExpect(jsonPath("$.data.prescribedMedicines[0].medicineName").value("Paracetamol 500mg"));
    }

    @Test
    @DisplayName("GET /api/medical-records/{id}: Phiếu khám không tồn tại -> 404 Not Found")
    void getCheckupDetail_NotFound() throws Exception {
        when(medicalCheckupService.getCheckupDetail(eq(999L), eq("bs001")))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy phiếu khám với ID: 999"));

        mockMvc.perform(get("/api/medical-records/999")
                        .principal(mockPrincipal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Không tìm thấy phiếu khám với ID: 999"));
    }

    @Test
    @DisplayName("GET /api/medical-records/{id}: Không có quyền truy cập -> 403 Forbidden")
    void getCheckupDetail_Forbidden() throws Exception {
        when(medicalCheckupService.getCheckupDetail(eq(1L), eq("bs001")))
                .thenThrow(new AccessDeniedException("Sinh viên chỉ được phép xem thông tin phiếu khám của chính mình"));

        mockMvc.perform(get("/api/medical-records/1")
                        .principal(mockPrincipal))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/medical-records/{id}/prescriptions: Bác sĩ cập nhật đơn thuốc thành công (200 OK)")
    void updatePrescription_Success() throws Exception {
        when(medicalCheckupService.updatePrescription(eq(1L), any(UpdatePrescriptionRequest.class), eq("bs001")))
                .thenReturn(sampleDetailResponse);

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Paracetamol 500mg", 10, "Uống 1 viên khi sốt"),
                        new PrescribedMedicineRequest("Vitamin C", 20, "Uống 1 viên mỗi sáng")
                ))
                .build();

        mockMvc.perform(put("/api/medical-records/1/prescriptions")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật đơn thuốc thành công"))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("PUT /api/medical-records/{id}/prescriptions: Khám tâm lý hoặc không cần kê thuốc (200 OK)")
    void updatePrescription_Success_NoMedicines() throws Exception {
        MedicalCheckupDetailResponse noMedResponse = MedicalCheckupDetailResponse.builder()
                .id(1L)
                .checkupType("PSYCHOLOGICAL")
                .status("DRAFT")
                .prescribedMedicines(Collections.emptyList())
                .build();

        when(medicalCheckupService.updatePrescription(eq(1L), any(UpdatePrescriptionRequest.class), eq("bs001")))
                .thenReturn(noMedResponse);

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(Collections.emptyList())
                .build();

        mockMvc.perform(put("/api/medical-records/1/prescriptions")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.prescribedMedicines").isEmpty());
    }

    @Test
    @DisplayName("PUT /api/medical-records/{id}/prescriptions: Phiếu khám đã COMPLETED -> 400 Bad Request khóa Read-only")
    void updatePrescription_Failed_WhenCompletedLock() throws Exception {
        when(medicalCheckupService.updatePrescription(eq(1L), any(UpdatePrescriptionRequest.class), eq("bs001")))
                .thenThrow(new BadRequestException("Phiếu khám đã hoàn tất (COMPLETED), đơn thuốc đã bị khóa vĩnh viễn và không thể chỉnh sửa"));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Paracetamol 500mg", 10, "Uống 1 viên khi sốt")
                ))
                .build();

        mockMvc.perform(put("/api/medical-records/1/prescriptions")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Phiếu khám đã hoàn tất (COMPLETED), đơn thuốc đã bị khóa vĩnh viễn và không thể chỉnh sửa"));
    }

    @Test
    @DisplayName("PUT /api/medical-records/{id}/prescriptions: Dữ liệu thuốc không hợp lệ (số lượng <= 0) -> 400 Bad Request")
    void updatePrescription_Failed_InvalidValidation() throws Exception {
        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Paracetamol 500mg", 0, "Uống khi sốt")
                ))
                .build();

        mockMvc.perform(put("/api/medical-records/1/prescriptions")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
