package com.unicare.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicare.application.dto.request.CancelAppointmentRequest;
import com.unicare.application.dto.request.RescheduleAppointmentRequest;
import com.unicare.application.dto.response.AppointmentResponse;
import com.unicare.application.service.AppointmentService;
import com.unicare.domain.enums.ServiceType;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DoctorAppointmentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private DoctorAppointmentController doctorAppointmentController;

    private AppointmentResponse sampleResponse;
    private Principal doctorPrincipal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(doctorAppointmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        doctorPrincipal = () -> "bs001";

        sampleResponse = AppointmentResponse.builder()
                .id(1L)
                .appointmentCode("LH2611040001")
                .studentId(10L)
                .studentCode("22520001")
                .studentName("Nguyễn Văn A")
                .shiftId(101L)
                .shiftType(ServiceType.KHAM_DA_KHOA)
                .reasonSymptoms("Đau đầu, sốt nhẹ")
                .status("CONFIRMED")
                .build();
    }

    @Test
    @DisplayName("GET /api/doctor/appointments - Lấy danh sách thành công với bộ lọc date, serviceType, status")
    void getDoctorAppointments_WithServiceType_Success() throws Exception {
        LocalDate filterDate = LocalDate.of(2026, 11, 5);
        when(appointmentService.getDoctorAppointments("bs001", filterDate, ServiceType.KHAM_DA_KHOA, "CONFIRMED"))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/doctor/appointments")
                        .param("date", "2026-11-05")
                        .param("serviceType", "KHAM_DA_KHOA")
                        .param("status", "CONFIRMED")
                        .principal(doctorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].studentCode").value("22520001"))
                .andExpect(jsonPath("$.data[0].reasonSymptoms").value("Đau đầu, sốt nhẹ"));
    }

    @Test
    @DisplayName("GET /api/doctor/appointments - Lấy danh sách thành công với tham số shiftType thay thế")
    void getDoctorAppointments_WithShiftTypeFallback_Success() throws Exception {
        when(appointmentService.getDoctorAppointments("bs001", null, ServiceType.TU_VAN_TAM_LY, null))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/doctor/appointments")
                        .param("shiftType", "TU_VAN_TAM_LY")
                        .principal(doctorPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }

    @Test
    @DisplayName("GET /api/doctor/appointments - Principal null")
    void getDoctorAppointments_NullPrincipal() throws Exception {
        when(appointmentService.getDoctorAppointments(null, null, null, null))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/doctor/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/doctor/appointments/{id}/cancel - Bác sĩ hủy lịch hẹn thành công")
    void cancelAppointmentByDoctor_Success() throws Exception {
        CancelAppointmentRequest request = new CancelAppointmentRequest("Bác sĩ có lịch họp đột xuất");
        sampleResponse.setStatus("CANCELED");
        sampleResponse.setCanceledBy("DOCTOR");
        sampleResponse.setCancelRescheduleReason("Bác sĩ có lịch họp đột xuất");

        when(appointmentService.cancelAppointment(eq(1L), any(CancelAppointmentRequest.class), eq("bs001")))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/doctor/appointments/1/cancel")
                        .principal(doctorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELED"))
                .andExpect(jsonPath("$.data.canceledBy").value("DOCTOR"));
    }

    @Test
    @DisplayName("PUT /api/doctor/appointments/{id}/cancel - Lỗi 400 khi bác sĩ không nhập lý do hủy")
    void cancelAppointmentByDoctor_BlankReason_BadRequest() throws Exception {
        CancelAppointmentRequest request = new CancelAppointmentRequest("   ");

        mockMvc.perform(put("/api/doctor/appointments/1/cancel")
                        .principal(doctorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/doctor/appointments/{id}/cancel - Lỗi 403 khi bác sĩ hủy lịch hẹn không thuộc ca mình phụ trách")
    void cancelAppointmentByDoctor_AccessDenied_Forbidden() throws Exception {
        CancelAppointmentRequest request = new CancelAppointmentRequest("Bác sĩ bận");

        when(appointmentService.cancelAppointment(eq(1L), any(CancelAppointmentRequest.class), eq("bs001")))
                .thenThrow(new AccessDeniedException("Bạn không có quyền thao tác trên lịch hẹn thuộc ca khám của bác sĩ khác."));

        mockMvc.perform(put("/api/doctor/appointments/1/cancel")
                        .principal(doctorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/doctor/appointments/{id}/cancel - Principal null")
    void cancelAppointmentByDoctor_NullPrincipal() throws Exception {
        CancelAppointmentRequest request = new CancelAppointmentRequest("Bác sĩ bận");
        when(appointmentService.cancelAppointment(eq(1L), any(CancelAppointmentRequest.class), isNull()))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/doctor/appointments/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/doctor/appointments/{id}/reschedule - Bác sĩ dời lịch hẹn thành công")
    void rescheduleAppointmentByDoctor_Success() throws Exception {
        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L, "Dời sang ca chiều");
        sampleResponse.setShiftId(102L);

        when(appointmentService.rescheduleAppointment(eq(1L), any(RescheduleAppointmentRequest.class), eq("bs001")))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/doctor/appointments/1/reschedule")
                        .principal(doctorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.shiftId").value(102L));
    }

    @Test
    @DisplayName("PUT /api/doctor/appointments/{id}/reschedule - Lỗi 400 khi bác sĩ để trống lý do dời lịch")
    void rescheduleAppointmentByDoctor_BlankReason_BadRequest() throws Exception {
        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L, "");

        mockMvc.perform(put("/api/doctor/appointments/1/reschedule")
                        .principal(doctorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/doctor/appointments/{id}/reschedule - Lỗi 403 khi bác sĩ dời lịch hẹn của bác sĩ khác")
    void rescheduleAppointmentByDoctor_AccessDenied_Forbidden() throws Exception {
        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L, "Dời lịch");

        when(appointmentService.rescheduleAppointment(eq(1L), any(RescheduleAppointmentRequest.class), eq("bs001")))
                .thenThrow(new AccessDeniedException("Bạn không có quyền thao tác trên lịch hẹn thuộc ca khám của bác sĩ khác."));

        mockMvc.perform(put("/api/doctor/appointments/1/reschedule")
                        .principal(doctorPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/doctor/appointments/{id}/reschedule - Principal null")
    void rescheduleAppointmentByDoctor_NullPrincipal() throws Exception {
        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(102L, "Dời lịch");
        when(appointmentService.rescheduleAppointment(eq(1L), any(RescheduleAppointmentRequest.class), isNull()))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/doctor/appointments/1/reschedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
