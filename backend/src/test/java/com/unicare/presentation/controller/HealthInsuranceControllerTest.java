package com.unicare.presentation.controller;

import com.unicare.application.dto.request.HealthInsuranceRequest;
import com.unicare.application.dto.response.HealthInsuranceResponse;
import com.unicare.application.service.HealthInsuranceService;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.enums.HealthInsuranceStatus;
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

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HealthInsuranceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HealthInsuranceService healthInsuranceService;

    @InjectMocks
    private HealthInsuranceController healthInsuranceController;

    private HealthInsuranceResponse sampleResponse;

    private final String validJson = """
            {
              "cardNumber": "DN4010123456789",
              "studentId": 10,
              "effectiveDate": "2026-01-01",
              "expirationDate": "2026-12-31",
              "initialHospital": "Bệnh viện Bạch Mai"
            }
            """;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(healthInsuranceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        LocalDate today = LocalDate.now();
        sampleResponse = HealthInsuranceResponse.builder()
                .id(1L)
                .studentId(10L)
                .studentCode("SV001")
                .studentFullName("Nguyen Van A")
                .cardNumber("DN4010123456789")
                .issueDate(today.minusMonths(6))
                .expirationDate(today.plusMonths(6))
                .registeredHospital("Bệnh viện Bạch Mai")
                .status(HealthInsuranceStatus.DANG_HIEU_LUC)
                .statusDescription("Đang hiệu lực")
                .daysRemaining(180L)
                .build();
    }

    @Test
    @DisplayName("GET /api/health-insurances - Lấy danh sách thẻ BHYT thành công")
    void getHealthInsurances_Success() throws Exception {
        when(healthInsuranceService.getHealthInsurances(eq("DN401"), eq("DANG_HIEU_LUC"), any()))
                .thenReturn(List.of(sampleResponse));

        Principal principal = () -> "admin_user";

        mockMvc.perform(get("/api/health-insurances")
                        .principal(principal)
                        .param("search", "DN401")
                        .param("status", "DANG_HIEU_LUC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].cardNumber").value("DN4010123456789"))
                .andExpect(jsonPath("$.data[0].status").value("DANG_HIEU_LUC"));
    }

    @Test
    @DisplayName("GET /api/health-insurances/{id} - Lấy chi tiết thẻ BHYT theo ID thành công")
    void getHealthInsuranceById_Success() throws Exception {
        when(healthInsuranceService.getHealthInsuranceById(eq(1L), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/health-insurances/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.cardNumber").value("DN4010123456789"));
    }

    @Test
    @DisplayName("GET /api/health-insurances/{id} - Thẻ không tồn tại trả về 404")
    void getHealthInsuranceById_NotFound() throws Exception {
        when(healthInsuranceService.getHealthInsuranceById(eq(999L), any()))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy thông tin thẻ BHYT với ID: 999"));

        mockMvc.perform(get("/api/health-insurances/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Không tìm thấy thông tin thẻ BHYT với ID: 999"));
    }

    @Test
    @DisplayName("GET /api/health-insurances/student/{studentId} - Lấy BHYT theo sinh viên thành công")
    void getHealthInsuranceByStudentId_Success() throws Exception {
        when(healthInsuranceService.getHealthInsuranceByStudentId(eq(10L), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/health-insurances/student/{studentId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentId").value(10))
                .andExpect(jsonPath("$.data.cardNumber").value("DN4010123456789"));
    }

    @Test
    @DisplayName("POST /api/health-insurances - Thêm mới thẻ BHYT thành công")
    void createHealthInsurance_Success() throws Exception {
        when(healthInsuranceService.createHealthInsurance(any(HealthInsuranceRequest.class), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/health-insurances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Thêm thẻ BHYT thành công"))
                .andExpect(jsonPath("$.data.cardNumber").value("DN4010123456789"));
    }

    @Test
    @DisplayName("POST /api/health-insurances - Mã thẻ không hợp lệ trả về 400")
    void createHealthInsurance_InvalidCardNumber_ReturnsBadRequest() throws Exception {
        String invalidCardJson = """
                {
                  "cardNumber": "INVALID_BHYT_123",
                  "studentId": 10,
                  "issueDate": "2026-01-01",
                  "expirationDate": "2026-12-31",
                  "registeredHospital": "BV A"
                }
                """;

        mockMvc.perform(post("/api/health-insurances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCardJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/health-insurances - Trùng mã thẻ trả về 409 Conflict")
    void createHealthInsurance_DuplicateCardNumber_ReturnsConflict() throws Exception {
        when(healthInsuranceService.createHealthInsurance(any(HealthInsuranceRequest.class), any()))
                .thenThrow(new DuplicateResourceException("Mã thẻ BHYT đã tồn tại trên hệ thống"));

        mockMvc.perform(post("/api/health-insurances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Mã thẻ BHYT đã tồn tại trên hệ thống"));
    }

    @Test
    @DisplayName("PUT /api/health-insurances/{id} - Cập nhật thẻ BHYT thành công")
    void updateHealthInsurance_Success() throws Exception {
        when(healthInsuranceService.updateHealthInsurance(eq(1L), any(HealthInsuranceRequest.class), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/health-insurances/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật thẻ BHYT thành công"))
                .andExpect(jsonPath("$.data.cardNumber").value("DN4010123456789"));
    }

    @Test
    @DisplayName("DELETE /api/health-insurances/{id} - Xóa thẻ BHYT thành công")
    void deleteHealthInsurance_Success() throws Exception {
        doNothing().when(healthInsuranceService).deleteHealthInsurance(eq(1L), any());

        mockMvc.perform(delete("/api/health-insurances/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa thẻ BHYT thành công"));
    }
}
