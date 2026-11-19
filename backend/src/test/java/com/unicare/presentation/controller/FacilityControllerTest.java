package com.unicare.presentation.controller;

import com.unicare.application.dto.response.FacilityResponse;
import com.unicare.application.service.ShiftService;
import com.unicare.presentation.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FacilityControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ShiftService shiftService;

    @InjectMocks
    private FacilityController facilityController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(facilityController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/facilities - Trả về danh sách cơ sở y tế gồm ID và tên")
    void getFacilities_Success() throws Exception {
        when(shiftService.getFacilities()).thenReturn(List.of(
                new FacilityResponse(1L, "Trung tâm Y tế Cơ sở Chính")
        ));

        mockMvc.perform(get("/api/facilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Trung tâm Y tế Cơ sở Chính"));
    }
}
