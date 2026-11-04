package com.unicare.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicare.application.dto.request.CancelAppointmentRequest;
import com.unicare.application.dto.request.RescheduleAppointmentRequest;
import com.unicare.application.dto.response.AppointmentResponse;
import com.unicare.application.service.AppointmentService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private AppointmentResponse sampleResponse;
    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        mockPrincipal = () -> "sv001";

        sampleResponse = AppointmentResponse.builder()
                .id(1L)
                .appointmentCode("LH2611040001")
                .studentId(10L)
                .status("CONFIRMED")
                .build();
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/cancel - Thành công")
    void cancelAppointment_Success() throws Exception {
        CancelAppointmentRequest request = new CancelAppointmentRequest("Em có lịch thi");
        sampleResponse.setStatus("CANCELED");
        sampleResponse.setCanceledBy("STUDENT");

        when(appointmentService.cancelAppointment(eq(1L), any(CancelAppointmentRequest.class), eq("sv001")))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/appointments/1/cancel")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELED"));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/cancel - Lỗi Validation khi để trống lý do")
    void cancelAppointment_ValidationError() throws Exception {
        CancelAppointmentRequest request = new CancelAppointmentRequest("");

        mockMvc.perform(put("/api/appointments/1/cancel")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/reschedule - Thành công")
    void rescheduleAppointment_Success() throws Exception {
        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L, "Dời sang chiều");
        sampleResponse.setShiftId(102L);

        when(appointmentService.rescheduleAppointment(eq(1L), any(RescheduleAppointmentRequest.class), eq("sv001")))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/appointments/1/reschedule")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shiftId").value(102L));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/reschedule - Lỗi Validation khi thiếu newShiftId")
    void rescheduleAppointment_ValidationError() throws Exception {
        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(null, "Dời sang chiều");

        mockMvc.perform(put("/api/appointments/1/reschedule")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/appointments/my-appointments - Lấy danh sách thành công")
    void getMyAppointments_Success() throws Exception {
        when(appointmentService.getStudentAppointments("sv001", null))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/appointments/my-appointments")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }

    @Test
    @DisplayName("GET /api/appointments/my-appointments - Lọc theo trạng thái thành công")
    void getMyAppointments_WithStatusFilter() throws Exception {
        when(appointmentService.getStudentAppointments("sv001", "CONFIRMED"))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/appointments/my-appointments")
                        .param("status", "CONFIRMED")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/appointments/{id} - Lấy chi tiết lịch hẹn thành công")
    void getAppointmentById_Success() throws Exception {
        when(appointmentService.getAppointmentById(1L, "sv001"))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/appointments/1")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));
    }
}
