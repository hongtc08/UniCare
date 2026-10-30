package com.unicare.presentation.controller;

import com.unicare.application.dto.request.ShiftRequest;
import com.unicare.application.dto.response.ShiftResponse;
import com.unicare.application.service.ShiftService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ShiftControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ShiftService shiftService;

    @InjectMocks
    private ShiftController shiftController;

    private ShiftResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(shiftController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = new ShiftResponse();
        sampleResponse.setId(1L);
        sampleResponse.setDoctorId(10L);
        sampleResponse.setDoctorUsername("bs01");
        sampleResponse.setDoctorName("BS. Nguyễn Văn Y");
        sampleResponse.setShiftDate(LocalDate.of(2026, 10, 28));
        sampleResponse.setStartTime(LocalTime.of(8, 0));
        sampleResponse.setEndTime(LocalTime.of(9, 0));
        sampleResponse.setServiceType(ServiceType.KHAM_DA_KHOA);
        sampleResponse.setRoom("Phòng 101");
        sampleResponse.setMaxQuantity(10);
        sampleResponse.setBookedQuantity(0);
        sampleResponse.setAvailableSlots(10);
        sampleResponse.setStatus(ShiftStatus.CON_TRONG);
    }

    @Test
    @DisplayName("GET /api/shifts - Trả về danh sách ca khám thành công")
    void getShifts_ReturnsList() throws Exception {
        when(shiftService.getShifts(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/shifts")
                        .param("serviceType", "KHAM_DA_KHOA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].serviceType").value("KHAM_DA_KHOA"))
                .andExpect(jsonPath("$.data[0].status").value("CON_TRONG"));
    }

    @Test
    @DisplayName("GET /api/shifts/{id} - Trả về chi tiết ca khám hoặc 404 nếu không tồn tại")
    void getShiftById_Success_And_NotFound() throws Exception {
        when(shiftService.getShiftById(1L)).thenReturn(sampleResponse);
        when(shiftService.getShiftById(999L))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy ca khám với mã ID: 999"));

        mockMvc.perform(get("/api/shifts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.doctorName").value("BS. Nguyễn Văn Y"));

        mockMvc.perform(get("/api/shifts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/shifts - Mở ca khám mới hợp lệ trả về 201 Created")
    void createShift_Success() throws Exception {
        String jsonBody = """
                {
                  "doctorId": 10,
                  "shiftDate": "2026-10-28",
                  "startTime": "08:00",
                  "endTime": "09:00",
                  "serviceType": "KHAM_DA_KHOA",
                  "room": "Phòng 101",
                  "maxQuantity": 10
                }
                """;

        when(shiftService.createShift(any(ShiftRequest.class), eq("admin01"))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/shifts")
                        .principal(() -> "admin01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.maxQuantity").value(10));
    }

    @Test
    @DisplayName("POST /api/shifts - Dữ liệu thiếu hoặc số lượng < 1 trả về 400 Bad Request")
    void createShift_ValidationError_Returns400() throws Exception {
        String invalidJson = """
                {
                  "doctorId": 10,
                  "room": "P1",
                  "maxQuantity": 0
                }
                """;

        mockMvc.perform(post("/api/shifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/shifts - Trùng khung giờ bác sĩ trong ngày trả về 409 Conflict")
    void createShift_Overlap_Returns409() throws Exception {
        String jsonBody = """
                {
                  "nhanVienYTeId": 10,
                  "ngayKham": "2026-10-28",
                  "gioBatDau": "08:00",
                  "gioKetThuc": "09:00",
                  "loaiCaKham": "TU_VAN_TAM_LY",
                  "phongKham": "Phòng 102",
                  "soLuongToiDa": 5
                }
                """;

        when(shiftService.createShift(any(ShiftRequest.class), any()))
                .thenThrow(new DuplicateResourceException("Bác sĩ đã có ca khám trùng khung giờ trong ngày."));

        mockMvc.perform(post("/api/shifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bác sĩ đã có ca khám trùng khung giờ trong ngày."));
    }

    @Test
    @DisplayName("PUT /api/shifts/{id} - Cập nhật ca khám thành công trả về 200 OK")
    void updateShift_Success() throws Exception {
        String jsonBody = """
                {
                  "doctorId": 10,
                  "shiftDate": "2026-10-28",
                  "startTime": "08:00",
                  "endTime": "09:30",
                  "serviceType": "KHAM_DA_KHOA",
                  "room": "Phòng 101",
                  "maxQuantity": 15
                }
                """;

        when(shiftService.updateShift(eq(1L), any(ShiftRequest.class), eq("bs01"))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/shifts/1")
                        .principal(() -> "bs01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật ca khám thành công"));
    }

    @Test
    @DisplayName("DELETE /api/shifts/{id} - Xóa thành công và chặn xóa khi đã có lịch hẹn")
    void deleteShift_Success_And_BlockedWhenBooked() throws Exception {
        mockMvc.perform(delete("/api/shifts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa ca khám thành công"));

        doThrow(new BadRequestException("Ca khám đã có 2 lịch hẹn. Vui lòng hủy hoặc dời tất cả lịch hẹn trước khi xóa ca khám."))
                .when(shiftService).deleteShift(2L);

        mockMvc.perform(delete("/api/shifts/2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Ca khám đã có 2 lịch hẹn. Vui lòng hủy hoặc dời tất cả lịch hẹn trước khi xóa ca khám."));
    }
}
