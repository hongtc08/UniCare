package com.unicare.presentation.controller;

import com.unicare.application.dto.request.MoodLogRequest;
import com.unicare.application.dto.response.MoodLogHistoryResponse;
import com.unicare.application.dto.response.MoodLogResponse;
import com.unicare.application.service.MoodLogService;
import com.unicare.common.exception.DuplicateResourceException;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MoodLogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MoodLogService moodLogService;

    @InjectMocks
    private MoodLogController moodLogController;

    private Principal studentPrincipal;
    private MoodLogResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(moodLogController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        studentPrincipal = () -> "24520579";

        sampleResponse = MoodLogResponse.builder()
                .id(1L)
                .studentId(2L)
                .studentCode("24520579")
                .studentName("Tăng Chấn Hồng")
                .moodLevel(4)
                .moodLabel("Vui vẻ")
                .notes("Tập thể dục buổi sáng sảng khoái")
                .logTime(OffsetDateTime.parse("2026-11-22T08:30:00+07:00"))
                .logDate(LocalDate.of(2026, 11, 22))
                .build();
    }

    @Test
    @DisplayName("POST /api/mental-health/mood-logs - Tạo mới nhật ký tâm trạng thành công (201 Created)")
    void createMoodLog_Success() throws Exception {
        String requestJson = """
                {
                  "moodLevel": 4,
                  "notes": "Tập thể dục buổi sáng sảng khoái",
                  "logTime": "2026-11-22T08:30:00+07:00"
                }
                """;

        when(moodLogService.createMoodLog(any(MoodLogRequest.class), eq("24520579")))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/mental-health/mood-logs")
                        .principal(studentPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Ghi nhận nhật ký tâm trạng thành công"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.moodLevel").value(4))
                .andExpect(jsonPath("$.data.moodLabel").value("Vui vẻ"))
                .andExpect(jsonPath("$.data.notes").value("Tập thể dục buổi sáng sảng khoái"));
    }

    @Test
    @DisplayName("POST /api/mental-health/mood-logs - Validation sai thang điểm hoặc ghi chú quá dài trả về 400 Bad Request")
    void createMoodLog_InvalidInput_Returns400() throws Exception {
        String invalidLevelJson = """
                {
                  "moodLevel": 6,
                  "notes": "Không hợp lệ"
                }
                """;

        mockMvc.perform(post("/api/mental-health/mood-logs")
                        .principal(studentPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLevelJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.moodLevel").exists());

        String missingLevelJson = """
                {
                  "notes": "Thiếu mức tâm trạng"
                }
                """;

        mockMvc.perform(post("/api/mental-health/mood-logs")
                        .principal(studentPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingLevelJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.moodLevel").exists());
    }

    @Test
    @DisplayName("POST /api/mental-health/mood-logs - Ghi trùng ngày trả về 409 Conflict")
    void createMoodLog_DuplicateSameDay_Returns409() throws Exception {
        String requestJson = """
                {
                  "mood_level": 5,
                  "notes": "Ghi lần 2 trong ngày"
                }
                """;

        when(moodLogService.createMoodLog(any(MoodLogRequest.class), eq("24520579")))
                .thenThrow(new DuplicateResourceException("Bạn đã ghi nhật ký tâm trạng trong ngày 2026-11-22"));

        mockMvc.perform(post("/api/mental-health/mood-logs")
                        .principal(studentPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/mental-health/mood-logs/{id} - Cập nhật thành công (200), sai ID (400/404) và chặn sửa của người khác (403)")
    void updateMoodLogById_Responses() throws Exception {
        String requestJson = """
                {
                  "moodLevel": 5,
                  "notes": "Cập nhật tâm trạng cuối ngày"
                }
                """;

        MoodLogResponse updated = MoodLogResponse.builder()
                .id(1L)
                .studentId(2L)
                .moodLevel(5)
                .moodLabel("Tuyệt vời")
                .notes("Cập nhật tâm trạng cuối ngày")
                .build();

        when(moodLogService.updateMoodLog(eq(1L), any(MoodLogRequest.class), eq("24520579")))
                .thenReturn(updated);

        mockMvc.perform(put("/api/mental-health/mood-logs/1")
                        .principal(studentPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cập nhật nhật ký tâm trạng thành công"))
                .andExpect(jsonPath("$.data.moodLevel").value(5))
                .andExpect(jsonPath("$.data.moodLabel").value("Tuyệt vời"));

        when(moodLogService.updateMoodLog(eq(999L), any(MoodLogRequest.class), eq("24520579")))
                .thenThrow(new ResourceNotFoundException("Không tìm thấy nhật ký tâm trạng với mã ID: 999"));
        mockMvc.perform(put("/api/mental-health/mood-logs/999")
                        .principal(studentPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        when(moodLogService.updateMoodLog(eq(20L), any(MoodLogRequest.class), eq("24520579")))
                .thenThrow(new AccessDeniedException("Sinh viên chỉ được phép cập nhật nhật ký tâm trạng của chính mình"));
        mockMvc.perform(put("/api/mental-health/mood-logs/20")
                        .principal(studentPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Bạn không có quyền thực hiện thao tác này"));

        mockMvc.perform(put("/api/mental-health/mood-logs/abc")
                        .principal(studentPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/mental-health/mood-logs - Cập nhật bản ghi trong ngày hoặc qua id trong body")
    void updateMoodLogByRequest_Responses() throws Exception {
        String requestJson = """
                {
                  "moodLevel": 3,
                  "notes": "Bình thường"
                }
                """;

        MoodLogResponse updated = MoodLogResponse.builder()
                .id(1L)
                .studentId(2L)
                .moodLevel(3)
                .moodLabel("Bình thường")
                .notes("Bình thường")
                .build();

        when(moodLogService.updateMoodLogByRequest(any(MoodLogRequest.class), eq("24520579")))
                .thenReturn(updated);

        mockMvc.perform(put("/api/mental-health/mood-logs")
                        .principal(studentPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.moodLevel").value(3));
    }

    @Test
    @DisplayName("GET /api/mental-health/mood-logs - Truy vấn theo tuần, tháng, khoảng ngày và kiểm tra bảo mật IDOR")
    void getMyMoodLogs_Responses() throws Exception {
        MoodLogHistoryResponse weekHistory = MoodLogHistoryResponse.builder()
                .period("WEEK")
                .fromDate(LocalDate.of(2026, 11, 16))
                .toDate(LocalDate.of(2026, 11, 22))
                .totalLogs(1)
                .averageMoodLevel(4.0)
                .logs(List.of(sampleResponse))
                .build();

        when(moodLogService.getMyMoodLogs(
                eq("24520579"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(weekHistory);

        mockMvc.perform(get("/api/mental-health/mood-logs")
                        .principal(studentPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lấy lịch sử tâm trạng thành công"))
                .andExpect(jsonPath("$.data.period").value("WEEK"))
                .andExpect(jsonPath("$.data.totalLogs").value(1))
                .andExpect(jsonPath("$.data.averageMoodLevel").value(4.0))
                .andExpect(jsonPath("$.data.logs[0].moodLevel").value(4));

        MoodLogHistoryResponse monthHistory = MoodLogHistoryResponse.builder()
                .period("MONTH")
                .fromDate(LocalDate.of(2026, 11, 1))
                .toDate(LocalDate.of(2026, 11, 30))
                .totalLogs(1)
                .averageMoodLevel(4.0)
                .logs(List.of(sampleResponse))
                .build();

        when(moodLogService.getMyMoodLogs(
                eq("24520579"), isNull(), eq("MONTH"), isNull(), eq(2026), eq(11), isNull(), isNull()))
                .thenReturn(monthHistory);

        mockMvc.perform(get("/api/mental-health/mood-logs")
                        .param("period", "MONTH")
                        .param("year", "2026")
                        .param("month", "11")
                        .principal(studentPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period").value("MONTH"))
                .andExpect(jsonPath("$.data.totalLogs").value(1));

        // Kiểm tra fallback tham số range khi không truyền period
        when(moodLogService.getMyMoodLogs(
                eq("24520579"), isNull(), eq("month"), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(monthHistory);
        mockMvc.perform(get("/api/mental-health/mood-logs")
                        .param("range", "month")
                        .principal(studentPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period").value("MONTH"));

        // Sai định dạng ngày trả về 400 Bad Request
        mockMvc.perform(get("/api/mental-health/mood-logs")
                        .param("date", "invalid-date")
                        .principal(studentPrincipal))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // Chặn IDOR khi truyền studentId khác
        when(moodLogService.getMyMoodLogs(
                eq("24520579"), eq(99L), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenThrow(new AccessDeniedException("Sinh viên chỉ được phép xem nhật ký tâm trạng của chính mình"));
        mockMvc.perform(get("/api/mental-health/mood-logs")
                        .param("studentId", "99")
                        .principal(studentPrincipal))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        // Chặn xung đột parameter pollution giữa studentId và student_id
        mockMvc.perform(get("/api/mental-health/mood-logs")
                        .param("studentId", "2")
                        .param("student_id", "99")
                        .principal(studentPrincipal))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
        verify(moodLogService, never()).getMyMoodLogs(
                eq("24520579"), eq(2L), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Kiểm tra annotation @PreAuthorize chỉ cho phép SINH_VIEN")
    void controllerRequiresStudentRole() {
        PreAuthorize annotation = MoodLogController.class.getAnnotation(PreAuthorize.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("hasRole('SINH_VIEN')");
    }
}
