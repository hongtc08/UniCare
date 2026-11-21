package com.unicare.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicare.application.dto.request.CreateMedicalCheckupRequest;
import com.unicare.application.dto.request.CreateTransferRecordRequest;
import com.unicare.application.dto.request.PrescribedMedicineRequest;
import com.unicare.application.dto.request.UpdateEmergencyProgressRequest;
import com.unicare.application.dto.request.UpdateMedicalCheckupRequest;
import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.EmergencyCasePageResponse;
import com.unicare.application.dto.response.EmergencyStatsResponse;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.dto.response.PrescribedMedicineResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.dto.response.TransferRecordResponse;
import com.unicare.application.dto.response.TransferReferralPrintResponse;
import com.unicare.application.service.MedicalCheckupService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.presentation.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private MedicalCheckupDetailResponse completedDetailResponse;
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
                .checkupType("GENERAL")
                .isEmergency(false)
                .studentId(10L)
                .studentCode("24521234")
                .studentName("Nguyễn Văn Sinh Viên")
                .staffId(20L)
                .doctorName("Bác sĩ Nguyễn Văn A")
                .appointmentId(100L)
                .appointmentCode("LH2611120001")
                .symptoms("Sốt 38.5 độ, đau họng")
                .diagnosisNotes("Viêm họng cấp")
                .treatmentSuggestions("Nghỉ ngơi, uống nhiều nước")
                .diseaseGroupId(5)
                .diseaseGroupName("Cúm mùa")
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

        completedDetailResponse = MedicalCheckupDetailResponse.builder()
                .id(1L)
                .checkupCode("PK2611040001")
                .checkupType("GENERAL")
                .isEmergency(false)
                .studentId(10L)
                .studentCode("24521234")
                .studentName("Nguyễn Văn Sinh Viên")
                .staffId(20L)
                .doctorName("Bác sĩ Nguyễn Văn A")
                .appointmentId(100L)
                .appointmentCode("LH2611120001")
                .symptoms("Sốt 38.5 độ, đau họng")
                .diagnosisNotes("Viêm họng cấp")
                .treatmentSuggestions("Nghỉ ngơi, uống nhiều nước")
                .diseaseGroupId(5)
                .diseaseGroupName("Cúm mùa")
                .status("COMPLETED")
                .completedAt(OffsetDateTime.now())
                .prescribedMedicines(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("GET /api/medical-records/students/search: Tra cứu nhanh sinh viên phục vụ tiếp nhận khẩn cấp (200 OK)")
    void searchStudentsForIntake_Success() throws Exception {
        StudentPersonalInfoResponse studentInfo = new StudentPersonalInfoResponse();
        studentInfo.setStudentId(10L);
        studentInfo.setStudentCode("24521234");
        studentInfo.setFullName("Nguyễn Văn Sinh Viên");
        studentInfo.setClassCode("SE104.O11");

        when(medicalCheckupService.searchStudentsForIntake(eq("24521234"), eq("bs001")))
                .thenReturn(List.of(studentInfo));

        mockMvc.perform(get("/api/medical-records/students/search")
                        .param("keyword", "24521234")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].studentCode").value("24521234"))
                .andExpect(jsonPath("$.data[0].fullName").value("Nguyễn Văn Sinh Viên"));
    }

    @Test
    @DisplayName("POST /api/medical-records: Tiếp nhận từ lịch hẹn CONFIRMED và tạo phiếu DRAFT thành công (201 Created)")
    void createCheckup_Draft_Success() throws Exception {
        when(medicalCheckupService.createCheckup(any(CreateMedicalCheckupRequest.class), eq("bs001")))
                .thenReturn(sampleDetailResponse);

        CreateMedicalCheckupRequest request = CreateMedicalCheckupRequest.builder()
                .appointmentId(100L)
                .symptoms("Sốt 38.5 độ, đau họng")
                .diagnosisNotes("Viêm họng cấp")
                .diseaseGroupId(5)
                .status("DRAFT")
                .build();

        mockMvc.perform(post("/api/medical-records")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tiếp nhận sinh viên và tạo phiếu khám thành công"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    @DisplayName("POST /api/medical-records: Tiếp nhận khẩn cấp và hoàn tất ngay (201 Created)")
    void createCheckup_EmergencyAndComplete_Success() throws Exception {
        when(medicalCheckupService.createCheckup(any(CreateMedicalCheckupRequest.class), eq("bs001")))
                .thenReturn(completedDetailResponse);

        CreateMedicalCheckupRequest request = CreateMedicalCheckupRequest.builder()
                .studentCode("24521234")
                .isEmergency(true)
                .symptoms("Chóng mặt")
                .diagnosisNotes("Hạ đường huyết")
                .treatmentSuggestions("Cho uống trà đường")
                .status("COMPLETED")
                .build();

        mockMvc.perform(post("/api/medical-records")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tiếp nhận và hoàn tất phiếu khám thành công"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /api/medical-records: Lịch hẹn đã được lập phiếu khám trước đó -> 409 Conflict")
    void createCheckup_DuplicateAppointment_Conflict() throws Exception {
        when(medicalCheckupService.createCheckup(any(CreateMedicalCheckupRequest.class), eq("bs001")))
                .thenThrow(new DuplicateResourceException("Lịch hẹn này đã được tiếp nhận và lập phiếu khám trước đó"));

        CreateMedicalCheckupRequest request = CreateMedicalCheckupRequest.builder()
                .appointmentId(100L)
                .build();

        mockMvc.perform(post("/api/medical-records")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PUT /api/medical-records/{id}: Lưu nháp (DRAFT) và Hoàn tất (COMPLETED) phiếu khám thành công (200 OK)")
    void updateCheckup_DraftAndComplete_Success() throws Exception {
        when(medicalCheckupService.updateCheckup(eq(1L), any(UpdateMedicalCheckupRequest.class), eq("bs001")))
                .thenReturn(sampleDetailResponse);

        UpdateMedicalCheckupRequest draftReq = UpdateMedicalCheckupRequest.builder()
                .symptoms("Sốt 38.5 độ")
                .diagnosisNotes("Viêm họng cấp")
                .status("DRAFT")
                .build();

        mockMvc.perform(put("/api/medical-records/1")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(draftReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lưu nháp phiếu khám thành công"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        when(medicalCheckupService.updateCheckup(eq(1L), any(UpdateMedicalCheckupRequest.class), eq("bs001")))
                .thenReturn(completedDetailResponse);

        UpdateMedicalCheckupRequest completeReq = UpdateMedicalCheckupRequest.builder()
                .symptoms("Sốt 38.5 độ")
                .diagnosisNotes("Viêm họng cấp")
                .status("COMPLETED")
                .build();

        mockMvc.perform(put("/api/medical-records/1")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Hoàn tất ca khám và khóa phiếu khám thành công"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("PUT /api/medical-records/{id}: Chặn sửa khi phiếu đã COMPLETED (400 Bad Request)")
    void updateCheckup_BlockedWhenCompleted() throws Exception {
        when(medicalCheckupService.updateCheckup(eq(1L), any(UpdateMedicalCheckupRequest.class), eq("bs001")))
                .thenThrow(new BadRequestException("Phiếu khám đã hoàn tất (COMPLETED), hồ sơ đã bị khóa Read-only vĩnh viễn và không thể chỉnh sửa"));

        UpdateMedicalCheckupRequest req = UpdateMedicalCheckupRequest.builder()
                .symptoms("Cố tình sửa")
                .build();

        mockMvc.perform(put("/api/medical-records/1")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Phiếu khám đã hoàn tất (COMPLETED), hồ sơ đã bị khóa Read-only vĩnh viễn và không thể chỉnh sửa"));
    }

    @Test
    @DisplayName("PUT /api/medical-records/{id}/complete: Hoàn tất trực tiếp phiếu khám thành công (200 OK)")
    void completeCheckup_Success() throws Exception {
        when(medicalCheckupService.completeCheckup(eq(1L), eq("bs001")))
                .thenReturn(completedDetailResponse);

        mockMvc.perform(put("/api/medical-records/1/complete")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("DELETE /api/medical-records/{id}: Xóa phiếu nháp DRAFT thành công và chặn xóa khi COMPLETED")
    void deleteCheckup_DraftSuccess_CompletedBlocked() throws Exception {
        doNothing().when(medicalCheckupService).deleteCheckup(1L, "bs001");

        mockMvc.perform(delete("/api/medical-records/1")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Xóa phiếu khám nháp thành công"));

        doThrow(new BadRequestException("Phiếu khám đã hoàn tất (COMPLETED), hồ sơ đã bị khóa Read-only vĩnh viễn và không thể xóa"))
                .when(medicalCheckupService).deleteCheckup(2L, "bs001");

        mockMvc.perform(delete("/api/medical-records/2")
                        .principal(mockPrincipal))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Phiếu khám đã hoàn tất (COMPLETED), hồ sơ đã bị khóa Read-only vĩnh viễn và không thể xóa"));
    }

    @Test
    @DisplayName("GET /api/medical-records/by-appointment/{appointmentId}: Lấy phiếu khám theo lịch hẹn thành công (200 OK)")
    void getCheckupByAppointment_Success() throws Exception {
        when(medicalCheckupService.getCheckupByAppointment(eq(100L), eq("bs001")))
                .thenReturn(sampleDetailResponse);

        mockMvc.perform(get("/api/medical-records/by-appointment/100")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.appointmentId").value(100L));
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

    @Test
    @DisplayName("POST /api/medical-records/{id}/transfer: Bác sĩ lập hồ sơ chuyển viện & kết thúc ca khám thành công (201 Created)")
    void createTransferRecord_Success() throws Exception {
        TransferRecordResponse trRes = TransferRecordResponse.builder()
                .id(77L)
                .recordCode("CV2611180001")
                .checkupId(1L)
                .targetHospital("Bệnh viện Đa khoa Khu vực Thủ Đức")
                .reasonCategory("CAP_CUU_NANG")
                .transferReason("Viêm ruột thừa cấp")
                .currentCondition("Đau bụng hố chậu phải")
                .firstAidNotes("Theo dõi sinh hiệu")
                .build();
        sampleDetailResponse.setStatus("COMPLETED");
        sampleDetailResponse.setTransferRecord(trRes);

        when(medicalCheckupService.createTransferRecord(eq(1L), any(CreateTransferRecordRequest.class), eq("bs001")))
                .thenReturn(sampleDetailResponse);

        CreateTransferRecordRequest request = new CreateTransferRecordRequest(
                "Bệnh viện Đa khoa Khu vực Thủ Đức",
                "CAP_CUU_NANG",
                "Viêm ruột thừa cấp",
                "Đau bụng hố chậu phải",
                "Theo dõi sinh hiệu",
                null,
                null
        );

        mockMvc.perform(post("/api/medical-records/1/transfer")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.transferRecord.recordCode").value("CV2611180001"))
                .andExpect(jsonPath("$.data.transferRecord.targetHospital").value("Bệnh viện Đa khoa Khu vực Thủ Đức"));
    }

    @Test
    @DisplayName("GET /api/medical-records/{id}/transfer/print: Kết xuất bản in Giấy giới thiệu chuyển viện thành công (200 OK)")
    void getTransferReferralPaper_Success() throws Exception {
        TransferReferralPrintResponse printRes = new TransferReferralPrintResponse();
        printRes.setTransferId(77L);
        printRes.setRecordCode("CV2611180001");
        printRes.setCheckupId(1L);
        printRes.setCheckupCode("PK2611120001");
        printRes.setTargetHospital("Bệnh viện Đa khoa Khu vực Thủ Đức");
        printRes.setReasonCategory("CAP_CUU_NANG");
        printRes.setReasonCategoryLabel("Cấp cứu nặng vượt khả năng chuyên môn");
        printRes.setStudentName("Nguyễn Văn A");
        printRes.setStudentCode("24520001");

        when(medicalCheckupService.getTransferReferralPaper(1L, "bs001")).thenReturn(printRes);

        mockMvc.perform(get("/api/medical-records/1/transfer/print")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recordCode").value("CV2611180001"))
                .andExpect(jsonPath("$.data.targetHospital").value("Bệnh viện Đa khoa Khu vực Thủ Đức"))
                .andExpect(jsonPath("$.data.readOnly").value(true));
    }

    @Test
    @DisplayName("POST /api/medical-records/emergencies, GET /emergencies, GET /emergencies/stats và PUT /{id}/progress")
    void emergencyEndpoints_Success() throws Exception {
        sampleDetailResponse.setIsEmergency(true);
        sampleDetailResponse.setEmergencyLevel("KHAN_CAP");
        sampleDetailResponse.setEmergencyType("Chấn thương");
        sampleDetailResponse.setReporterContact("0901234567");
        sampleDetailResponse.setCurrentStep("TIEP_NHAN");

        when(medicalCheckupService.createCheckup(any(CreateMedicalCheckupRequest.class), eq("bs001")))
                .thenReturn(sampleDetailResponse);

        CreateMedicalCheckupRequest intakeReq = CreateMedicalCheckupRequest.builder()
                .studentId(10L)
                .emergencyLevel("KHAN_CAP")
                .emergencyType("Chấn thương")
                .reporterContact("0901234567")
                .symptoms("Trật khớp")
                .build();

        mockMvc.perform(post("/api/medical-records/emergencies")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(intakeReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isEmergency").value(true))
                .andExpect(jsonPath("$.data.emergencyLevel").value("KHAN_CAP"))
                .andExpect(jsonPath("$.data.currentStep").value("TIEP_NHAN"));

        // GET /api/medical-records/emergencies
        EmergencyCasePageResponse pageRes = new EmergencyCasePageResponse(List.of(sampleDetailResponse), 0, 10, 1L);
        when(medicalCheckupService.getEmergencyCases(any(), any(), any(), any(), any(), any(), eq(0), eq(10), eq("bs001")))
                .thenReturn(pageRes);

        mockMvc.perform(get("/api/medical-records/emergencies")
                        .param("page", "0")
                        .param("size", "10")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].checkupCode").value("PK2611040001"));

        // GET /api/medical-records/emergencies/stats
        EmergencyStatsResponse statsRes = new EmergencyStatsResponse(2L, 1L, 1L, 3L);
        when(medicalCheckupService.getEmergencyStats("bs001")).thenReturn(statsRes);

        mockMvc.perform(get("/api/medical-records/emergencies/stats")
                        .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.inProgressCount").value(2))
                .andExpect(jsonPath("$.data.firstAidCount").value(1))
                .andExpect(jsonPath("$.data.transferringCount").value(1))
                .andExpect(jsonPath("$.data.completedTodayCount").value(3));

        // PUT /api/medical-records/1/progress
        sampleDetailResponse.setCurrentStep("SO_CUU");
        when(medicalCheckupService.updateEmergencyProgress(eq(1L), any(UpdateEmergencyProgressRequest.class), eq("bs001")))
                .thenReturn(sampleDetailResponse);

        UpdateEmergencyProgressRequest progressReq = new UpdateEmergencyProgressRequest("SO_CUU", "Băng bó", "Chấn thương nhẹ");
        mockMvc.perform(put("/api/medical-records/1/progress")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progressReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.currentStep").value("SO_CUU"));

        // Kiểm tra nhánh principal == null & isEmergency đã truyền sẵn cho các endpoint mới
        CreateTransferRecordRequest trReq = new CreateTransferRecordRequest(
                "BV Thủ Đức", "QUA_TAI", "Lý do", "Tình trạng", "Sơ cứu", null, null
        );
        when(medicalCheckupService.createTransferRecord(eq(1L), any(CreateTransferRecordRequest.class), eq(null)))
                .thenReturn(sampleDetailResponse);
        mockMvc.perform(post("/api/medical-records/1/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trReq)))
                .andExpect(status().isCreated());

        when(medicalCheckupService.getTransferReferralPaper(1L, null)).thenReturn(new TransferReferralPrintResponse());
        mockMvc.perform(get("/api/medical-records/1/transfer"))
                .andExpect(status().isOk());

        CreateMedicalCheckupRequest explicitEmReq = CreateMedicalCheckupRequest.builder()
                .studentId(10L)
                .isEmergency(true)
                .build();
        when(medicalCheckupService.createCheckup(any(CreateMedicalCheckupRequest.class), eq(null)))
                .thenReturn(sampleDetailResponse);
        mockMvc.perform(post("/api/medical-records/emergencies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(explicitEmReq)))
                .andExpect(status().isCreated());

        when(medicalCheckupService.getEmergencyCases(any(), any(), any(), any(), any(), any(), any(), any(), eq(null)))
                .thenReturn(pageRes);
        mockMvc.perform(get("/api/medical-records/emergencies"))
                .andExpect(status().isOk());

        when(medicalCheckupService.getEmergencyStats(null)).thenReturn(statsRes);
        mockMvc.perform(get("/api/medical-records/emergencies/stats"))
                .andExpect(status().isOk());

        when(medicalCheckupService.updateEmergencyProgress(eq(1L), any(UpdateEmergencyProgressRequest.class), eq(null)))
                .thenReturn(sampleDetailResponse);
        mockMvc.perform(put("/api/medical-records/1/step")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(progressReq)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SP5-01 POST & PUT /api/medical-records: Hỗ trợ JSON alias cho Tư vấn tâm lý và Khám sức khỏe định kỳ")
    void psychologicalAndPeriodicAliases_Success() throws Exception {
        MedicalCheckupDetailResponse psychRes = MedicalCheckupDetailResponse.builder()
                .id(11L)
                .checkupCode("PK2611210011")
                .checkupType("PSYCHOLOGICAL")
                .checkupTypeLabel("Tư vấn tâm lý")
                .timelineCategory("PSYCHOLOGICAL")
                .isPeriodic(false)
                .symptoms("Mất ngủ, lo âu trước kỳ thi")
                .diagnosisNotes("Căng thẳng học đường mức nhẹ")
                .treatmentSuggestions("Thực hành thở 4-7-8, nghỉ ngơi hợp lý")
                .status("COMPLETED")
                .completedAt(OffsetDateTime.now())
                .prescribedMedicines(Collections.emptyList())
                .build();

        when(medicalCheckupService.createCheckup(any(CreateMedicalCheckupRequest.class), eq("bs001")))
                .thenReturn(psychRes);

        String psychPayload = """
                {
                  "appointmentId": 201,
                  "loaiPhieu": "PSYCHOLOGICAL",
                  "bieuHien": "Mất ngủ, lo âu trước kỳ thi",
                  "tomTatTuVan": "Căng thẳng học đường mức nhẹ",
                  "deXuatGiaiToa": "Thực hành thở 4-7-8, nghỉ ngơi hợp lý",
                  "trangThai": "COMPLETED"
                }
                """;

        mockMvc.perform(post("/api/medical-records")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(psychPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.checkupType").value("PSYCHOLOGICAL"))
                .andExpect(jsonPath("$.data.checkupTypeLabel").value("Tư vấn tâm lý"))
                .andExpect(jsonPath("$.data.timelineCategory").value("PSYCHOLOGICAL"))
                .andExpect(jsonPath("$.data.symptoms").value("Mất ngủ, lo âu trước kỳ thi"))
                .andExpect(jsonPath("$.data.diagnosisNotes").value("Căng thẳng học đường mức nhẹ"))
                .andExpect(jsonPath("$.data.treatmentSuggestions").value("Thực hành thở 4-7-8, nghỉ ngơi hợp lý"));

        ArgumentCaptor<CreateMedicalCheckupRequest> createCaptor = ArgumentCaptor.forClass(CreateMedicalCheckupRequest.class);
        verify(medicalCheckupService).createCheckup(createCaptor.capture(), eq("bs001"));
        assertThat(createCaptor.getValue().getCheckupType()).isEqualTo("PSYCHOLOGICAL");
        assertThat(createCaptor.getValue().getSymptoms()).isEqualTo("Mất ngủ, lo âu trước kỳ thi");
        assertThat(createCaptor.getValue().getDiagnosisNotes()).isEqualTo("Căng thẳng học đường mức nhẹ");
        assertThat(createCaptor.getValue().getTreatmentSuggestions()).isEqualTo("Thực hành thở 4-7-8, nghỉ ngơi hợp lý");
        assertThat(createCaptor.getValue().getStatus()).isEqualTo("COMPLETED");

        MedicalCheckupDetailResponse periodicRes = MedicalCheckupDetailResponse.builder()
                .id(12L)
                .checkupCode("PK2611210012")
                .checkupType("GENERAL")
                .checkupTypeLabel("Khám sức khỏe định kỳ")
                .timelineCategory("PERIODIC")
                .isPeriodic(true)
                .periodicResult("PASSED")
                .periodicResultLabel("Đạt")
                .symptoms("Khám tổng quát định kỳ")
                .diagnosisNotes("Đủ điều kiện sức khỏe học tập")
                .status("COMPLETED")
                .completedAt(OffsetDateTime.now())
                .prescribedMedicines(Collections.emptyList())
                .build();

        when(medicalCheckupService.updateCheckup(eq(12L), any(UpdateMedicalCheckupRequest.class), eq("bs001")))
                .thenReturn(periodicRes);

        String periodicPayload = """
                {
                  "isPeriodic": true,
                  "ketLuanDinhKy": "PASSED",
                  "trieuChung": "Khám tổng quát định kỳ",
                  "chanDoan": "Đủ điều kiện sức khỏe học tập",
                  "status": "COMPLETED"
                }
                """;

        mockMvc.perform(put("/api/medical-records/12")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(periodicPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isPeriodic").value(true))
                .andExpect(jsonPath("$.data.periodicResult").value("PASSED"))
                .andExpect(jsonPath("$.data.periodicResultLabel").value("Đạt"))
                .andExpect(jsonPath("$.data.checkupTypeLabel").value("Khám sức khỏe định kỳ"))
                .andExpect(jsonPath("$.data.timelineCategory").value("PERIODIC"));

        ArgumentCaptor<UpdateMedicalCheckupRequest> updateCaptor = ArgumentCaptor.forClass(UpdateMedicalCheckupRequest.class);
        verify(medicalCheckupService).updateCheckup(eq(12L), updateCaptor.capture(), eq("bs001"));
        assertThat(updateCaptor.getValue().getIsPeriodic()).isTrue();
        assertThat(updateCaptor.getValue().getPeriodicResult()).isEqualTo("PASSED");
        assertThat(updateCaptor.getValue().getSymptoms()).isEqualTo("Khám tổng quát định kỳ");
        assertThat(updateCaptor.getValue().getDiagnosisNotes()).isEqualTo("Đủ điều kiện sức khỏe học tập");
    }
}
