package com.unicare.application.service;

import com.unicare.application.dto.request.CreateMedicalCheckupRequest;
import com.unicare.application.dto.request.CreateTransferRecordRequest;
import com.unicare.application.dto.request.PrescribedMedicineRequest;
import com.unicare.application.dto.request.UpdateEmergencyProgressRequest;
import com.unicare.application.dto.request.UpdateMedicalCheckupRequest;
import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.EmergencyCasePageResponse;
import com.unicare.application.dto.response.EmergencyStatsResponse;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.dto.response.MedicalCheckupSummaryResponse;
import com.unicare.application.dto.response.PrescribedMedicineResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.dto.response.TransferRecordResponse;
import com.unicare.application.dto.response.TransferReferralPrintResponse;
import com.unicare.application.service.impl.MedicalCheckupServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.*;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;
import com.unicare.domain.enums.UserStatus;
import com.unicare.domain.repository.AppointmentRepository;
import com.unicare.domain.repository.DiseaseGroupRepository;
import com.unicare.domain.repository.HealthInsuranceRepository;
import com.unicare.domain.repository.HealthRecordRepository;
import com.unicare.domain.repository.MedicalCheckupRepository;
import com.unicare.domain.repository.MedicalStaffRepository;
import com.unicare.domain.repository.StudentRepository;
import com.unicare.domain.repository.TransferRecordRepository;
import com.unicare.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalCheckupServiceImplTest {

    @Mock
    private MedicalCheckupRepository medicalCheckupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private MedicalStaffRepository medicalStaffRepository;

    @Mock
    private DiseaseGroupRepository diseaseGroupRepository;

    @Mock
    private TransferRecordRepository transferRecordRepository;

    @Mock
    private HealthRecordRepository healthRecordRepository;

    @Mock
    private HealthInsuranceRepository healthInsuranceRepository;

    private MedicalCheckupServiceImpl medicalCheckupService;

    private User studentUser;
    private User otherStudentUser;
    private User doctorUser;
    private User otherDoctorUser;
    private User adminUser;

    private Student student;
    private Student otherStudent;
    private MedicalStaff doctorStaff;
    private MedicalStaff otherDoctorStaff;

    private Shift generalShift;
    private Shift psychologicalShift;
    private Appointment confirmedAppointment;
    private DiseaseGroup fluDiseaseGroup;

    private MedicalCheckup draftCheckup;
    private MedicalCheckup completedCheckup;

    @BeforeEach
    void setUp() {
        medicalCheckupService = new MedicalCheckupServiceImpl(
                medicalCheckupRepository,
                userRepository,
                appointmentRepository,
                studentRepository,
                medicalStaffRepository,
                diseaseGroupRepository,
                transferRecordRepository,
                healthRecordRepository,
                healthInsuranceRepository
        );

        Faculty faculty = Faculty.builder()
                .id(1)
                .facultyCode("CNPM")
                .facultyName("Công nghệ Phần mềm")
                .build();

        AcademicClass academicClass = AcademicClass.builder()
                .id(1)
                .classCode("SE104.O11")
                .className("Kỹ thuật phần mềm 2024")
                .academicYear("K19")
                .faculty(faculty)
                .build();

        studentUser = User.builder()
                .id(10L)
                .username("sv001")
                .fullName("Nguyễn Văn Sinh Viên")
                .email("sv001@gm.uit.edu.vn")
                .phoneNumber("0901234567")
                .role(Role.SINH_VIEN)
                .status(UserStatus.ACTIVE)
                .build();

        student = Student.builder()
                .id(10L)
                .user(studentUser)
                .studentCode("24521234")
                .academicClass(academicClass)
                .build();
        studentUser.setStudent(student);

        otherStudentUser = User.builder()
                .id(11L)
                .username("sv002")
                .fullName("Trần Thị Khác")
                .role(Role.SINH_VIEN)
                .status(UserStatus.ACTIVE)
                .build();

        otherStudent = Student.builder()
                .id(11L)
                .user(otherStudentUser)
                .studentCode("24520579")
                .academicClass(academicClass)
                .build();
        otherStudentUser.setStudent(otherStudent);

        doctorUser = User.builder()
                .id(20L)
                .username("bs001")
                .fullName("Bác sĩ Nguyễn Văn A")
                .role(Role.NHAN_VIEN_Y_TE)
                .status(UserStatus.ACTIVE)
                .build();

        doctorStaff = MedicalStaff.builder()
                .id(20L)
                .user(doctorUser)
                .staffCode("NV001")
                .title("Bác sĩ chuyên khoa I")
                .specialty("KHAM_DA_KHOA")
                .build();
        doctorUser.setMedicalStaff(doctorStaff);

        otherDoctorUser = User.builder()
                .id(21L)
                .username("bs002")
                .fullName("Bác sĩ Lê Khác Ca")
                .role(Role.NHAN_VIEN_Y_TE)
                .status(UserStatus.ACTIVE)
                .build();

        otherDoctorStaff = MedicalStaff.builder()
                .id(21L)
                .user(otherDoctorUser)
                .staffCode("NV002")
                .title("Bác sĩ đa khoa")
                .specialty("KHAM_DA_KHOA")
                .build();
        otherDoctorUser.setMedicalStaff(otherDoctorStaff);

        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .fullName("Ban Quản Lý UniCare")
                .role(Role.BAN_QUAN_LY)
                .status(UserStatus.ACTIVE)
                .build();

        generalShift = Shift.builder()
                .id(50L)
                .shiftDate(LocalDate.now())
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .shiftType(ServiceType.KHAM_DA_KHOA)
                .staff(doctorStaff)
                .room("Phòng 101")
                .maxSlots(10)
                .bookedSlots(1)
                .status(ShiftStatus.OPEN)
                .build();

        psychologicalShift = Shift.builder()
                .id(51L)
                .shiftDate(LocalDate.now())
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .shiftType(ServiceType.TU_VAN_TAM_LY)
                .staff(doctorStaff)
                .room("Phòng Tâm lý 202")
                .maxSlots(5)
                .bookedSlots(1)
                .status(ShiftStatus.OPEN)
                .build();

        confirmedAppointment = Appointment.builder()
                .id(100L)
                .appointmentCode("LH2611120001")
                .student(student)
                .shift(generalShift)
                .reasonSymptoms("Đau đầu, sốt 38.5 độ từ tối qua")
                .status("CONFIRMED")
                .createdAt(OffsetDateTime.now().minusHours(3))
                .build();

        fluDiseaseGroup = DiseaseGroup.builder()
                .id(5)
                .groupCode("CUM")
                .groupName("Cúm mùa / Cảm lạnh")
                .build();

        PrescribedMedicine sampleMed = PrescribedMedicine.builder()
                .id(100L)
                .medicineName("Paracetamol 500mg")
                .quantity(10)
                .dosageInstruction("Uống 1 viên khi sốt > 38.5 độ")
                .build();

        draftCheckup = MedicalCheckup.builder()
                .id(1L)
                .checkupCode("PK2611040001")
                .student(student)
                .staff(doctorStaff)
                .appointment(confirmedAppointment)
                .checkupType("GENERAL")
                .isEmergency(false)
                .symptoms("Sốt nhẹ, ho khan")
                .diagnosisNotes("Cảm cúm thông thường")
                .treatmentSuggestions("Nghỉ ngơi, uống nhiều nước")
                .diseaseGroup(fluDiseaseGroup)
                .status("DRAFT")
                .createdAt(OffsetDateTime.now())
                .prescribedMedicines(new ArrayList<>(List.of(sampleMed)))
                .build();
        sampleMed.setMedicalCheckup(draftCheckup);

        completedCheckup = MedicalCheckup.builder()
                .id(2L)
                .checkupCode("PK2611040002")
                .student(student)
                .staff(doctorStaff)
                .appointment(confirmedAppointment)
                .checkupType("GENERAL")
                .isEmergency(false)
                .symptoms("Sốt cao")
                .diagnosisNotes("Sốt siêu vi")
                .status("COMPLETED")
                .createdAt(OffsetDateTime.now().minusHours(2))
                .completedAt(OffsetDateTime.now().minusHours(1))
                .prescribedMedicines(new ArrayList<>(List.of(sampleMed)))
                .build();
    }

    // =========================================================================
    // 1. Tests for searchStudentsForIntake (Tiếp nhận khẩn cấp: tra cứu nhanh SV)
    // =========================================================================

    @Test
    @DisplayName("Tra cứu nhanh SV theo MSSV hoặc Họ tên/Lớp phục vụ tiếp nhận khẩn cấp: Thành công")
    void searchStudentsForIntake_Success() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(studentRepository.searchStudentsByKeyword("24521234")).thenReturn(List.of(student));

        List<StudentPersonalInfoResponse> results = medicalCheckupService.searchStudentsForIntake("  24521234  ", "bs001");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStudentId()).isEqualTo(10L);
        assertThat(results.get(0).getStudentCode()).isEqualTo("24521234");
        assertThat(results.get(0).getFullName()).isEqualTo("Nguyễn Văn Sinh Viên");
        assertThat(results.get(0).getClassCode()).isEqualTo("SE104.O11");

        when(studentRepository.searchStudentsByKeyword("")).thenReturn(List.of());
        assertThat(medicalCheckupService.searchStudentsForIntake(null, "bs001")).isEmpty();
    }

    @Test
    @DisplayName("Tra cứu nhanh SV: Chặn Ban quản lý và Sinh viên gọi API")
    void searchStudentsForIntake_Forbidden_WhenNotDoctor() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertThatThrownBy(() -> medicalCheckupService.searchStudentsForIntake("2452", "admin"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Ban quản lý không được phép truy cập");

        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        assertThatThrownBy(() -> medicalCheckupService.searchStudentsForIntake("2452", "sv001"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ tài khoản Bác sĩ / Nhân viên y tế");
    }

    // =========================================================================
    // 2. Tests for createCheckup (Tiếp nhận Theo hẹn CONFIRMED & Tiếp nhận Khẩn cấp)
    // =========================================================================

    @Test
    @DisplayName("Tiếp nhận sinh viên từ lịch hẹn CONFIRMED: Tự điền triệu chứng ban đầu và tạo phiếu DRAFT thành công")
    void createCheckup_FromConfirmedAppointment_Draft_Success() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(appointmentRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(confirmedAppointment));
        when(medicalCheckupRepository.findByAppointmentIdWithDetails(100L)).thenReturn(Optional.empty());
        when(diseaseGroupRepository.findById(5)).thenReturn(Optional.of(fluDiseaseGroup));
        when(medicalCheckupRepository.existsByCheckupCode(anyString())).thenReturn(false);
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(invocation -> {
            MedicalCheckup arg = invocation.getArgument(0);
            arg.setId(10L);
            return arg;
        });

        CreateMedicalCheckupRequest request = CreateMedicalCheckupRequest.builder()
                .appointmentId(100L)
                .diseaseGroupId(5)
                .diagnosisNotes("Theo dõi cảm cúm")
                .treatmentSuggestions("Cho uống hạ sốt")
                .build();

        MedicalCheckupDetailResponse response = medicalCheckupService.createCheckup(request, "bs001");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCheckupCode()).startsWith("PK");
        assertThat(response.getAppointmentId()).isEqualTo(100L);
        assertThat(response.getStudentId()).isEqualTo(10L);
        assertThat(response.getIsEmergency()).isFalse();
        assertThat(response.getCheckupType()).isEqualTo("GENERAL");
        assertThat(response.getSymptoms()).isEqualTo("Đau đầu, sốt 38.5 độ từ tối qua");
        assertThat(response.getDiagnosisNotes()).isEqualTo("Theo dõi cảm cúm");
        assertThat(response.getDiseaseGroupId()).isEqualTo(5);
        assertThat(response.getStatus()).isEqualTo("DRAFT");
        assertThat(response.getCompletedAt()).isNull();
        // Khi mới lưu DRAFT, lịch hẹn vẫn giữ nguyên CONFIRMED
        assertThat(confirmedAppointment.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("Tiếp nhận khẩn cấp / cấp cứu theo MSSV (studentCode) không qua lịch hẹn: Tạo phiếu khẩn cấp thành công")
    void createCheckup_EmergencyIntake_ByStudentCode_Success() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(studentRepository.findByStudentCodeWithDetails("24521234")).thenReturn(Optional.of(student));
        when(medicalCheckupRepository.existsByCheckupCode(anyString())).thenReturn(false);
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(invocation -> {
            MedicalCheckup arg = invocation.getArgument(0);
            arg.setId(15L);
            return arg;
        });

        CreateMedicalCheckupRequest request = CreateMedicalCheckupRequest.builder()
                .studentCode("24521234")
                .isEmergency(true)
                .symptoms("Chóng mặt, tụt huyết áp tại sân thể chất")
                .treatmentSuggestions("Cho nằm nghỉ, uống trà đường ấm, đo huyết áp")
                .diagnosisNotes("Hạ đường huyết nhẹ")
                .status("COMPLETED")
                .build();

        MedicalCheckupDetailResponse response = medicalCheckupService.createCheckup(request, "bs001");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getAppointmentId()).isNull();
        assertThat(response.getIsEmergency()).isTrue();
        assertThat(response.getStudentCode()).isEqualTo("24521234");
        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Tiếp nhận khẩn cấp theo studentId: Thành công")
    void createCheckup_EmergencyIntake_ByStudentId_Success() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(studentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(student));
        when(medicalCheckupRepository.existsByCheckupCode(anyString())).thenReturn(false);
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(i -> i.getArgument(0));

        CreateMedicalCheckupRequest request = CreateMedicalCheckupRequest.builder()
                .studentId(10L)
                .symptoms("Trầy xước cổ chân")
                .treatmentSuggestions("Sát trùng Povidine, băng gạc")
                .build();

        MedicalCheckupDetailResponse response = medicalCheckupService.createCheckup(request, "bs001");

        assertThat(response.getIsEmergency()).isTrue();
        assertThat(response.getStatus()).isEqualTo("DRAFT");
        assertThat(response.getStudentId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Tiếp nhận lịch hẹn: Lỗi khi lịch hẹn không ở trạng thái CONFIRMED hoặc đã có phiếu khám trước đó")
    void createCheckup_FromAppointment_InvalidStatusOrDuplicate() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));

        // 1. Đã có phiếu khám gắn với lịch hẹn
        when(appointmentRepository.findByIdWithDetails(100L)).thenReturn(Optional.of(confirmedAppointment));
        when(medicalCheckupRepository.findByAppointmentIdWithDetails(100L)).thenReturn(Optional.of(draftCheckup));

        CreateMedicalCheckupRequest req = CreateMedicalCheckupRequest.builder().appointmentId(100L).build();
        assertThatThrownBy(() -> medicalCheckupService.createCheckup(req, "bs001"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã được tiếp nhận và lập phiếu khám trước đó");

        // 2. Lịch hẹn đã bị CANCELED
        when(medicalCheckupRepository.findByAppointmentIdWithDetails(100L)).thenReturn(Optional.empty());
        confirmedAppointment.setStatus("CANCELED");
        assertThatThrownBy(() -> medicalCheckupService.createCheckup(req, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Chỉ có thể tiếp nhận lịch hẹn đang ở trạng thái Đã xác nhận (CONFIRMED)");

        // 3. Bác sĩ khác ca cố tình tiếp nhận lịch hẹn không thuộc mình phụ trách
        confirmedAppointment.setStatus("CONFIRMED");
        when(userRepository.findByUsername("bs002")).thenReturn(Optional.of(otherDoctorUser));
        assertThatThrownBy(() -> medicalCheckupService.createCheckup(req, "bs002"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ bác sĩ phụ trách ca khám mới có quyền tiếp nhận lịch hẹn này");

        // 4. Đánh dấu isEmergency = true khi có appointmentId
        CreateMedicalCheckupRequest emergencyWithAppt = CreateMedicalCheckupRequest.builder()
                .appointmentId(100L)
                .isEmergency(true)
                .build();
        assertThatThrownBy(() -> medicalCheckupService.createCheckup(emergencyWithAppt, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không thể đánh dấu là ca cấp cứu khẩn cấp");
    }

    @Test
    @DisplayName("Tiếp nhận lỗi đầu vào: Request null, thiếu studentId/appointmentId, hoặc MSSV không tồn tại")
    void createCheckup_ValidationFailures() {
        assertThatThrownBy(() -> medicalCheckupService.createCheckup(null, "bs001"))
                .isInstanceOf(BadRequestException.class);

        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));

        CreateMedicalCheckupRequest emptyReq = new CreateMedicalCheckupRequest();
        assertThatThrownBy(() -> medicalCheckupService.createCheckup(emptyReq, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Vui lòng chọn lịch hẹn (appointmentId) hoặc truyền mã sinh viên");

        when(studentRepository.findByStudentCodeWithDetails("99999999")).thenReturn(Optional.empty());
        when(studentRepository.findByStudentCode("99999999")).thenReturn(Optional.empty());
        CreateMedicalCheckupRequest unknownMssv = CreateMedicalCheckupRequest.builder()
                .studentCode("99999999")
                .build();
        assertThatThrownBy(() -> medicalCheckupService.createCheckup(unknownMssv, "bs001"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy sinh viên với MSSV: 99999999");
    }

    // =========================================================================
    // 3. Tests for updateCheckup & completeCheckup (DRAFT vs COMPLETED Read-only Lock)
    // =========================================================================

    @Test
    @DisplayName("Lưu nháp (DRAFT): Cho phép chỉnh sửa triệu chứng, chẩn đoán, biện pháp sơ cứu, nhóm bệnh dịch tễ nhiều lần")
    void updateCheckup_Draft_AllowsEditing() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));
        when(diseaseGroupRepository.findById(5)).thenReturn(Optional.of(fluDiseaseGroup));
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(i -> i.getArgument(0));

        UpdateMedicalCheckupRequest updateRequest = UpdateMedicalCheckupRequest.builder()
                .symptoms("Sốt 39 độ, đau họng, sổ mũi")
                .diagnosisNotes("Viêm mũi họng cấp / Cúm")
                .treatmentSuggestions("Chườm ấm hạ sốt, súc họng nước muối")
                .diseaseGroupId(5)
                .status("DRAFT")
                .build();

        MedicalCheckupDetailResponse response = medicalCheckupService.updateCheckup(1L, updateRequest, "bs001");

        assertThat(response.getStatus()).isEqualTo("DRAFT");
        assertThat(response.getSymptoms()).isEqualTo("Sốt 39 độ, đau họng, sổ mũi");
        assertThat(response.getDiagnosisNotes()).isEqualTo("Viêm mũi họng cấp / Cúm");
        assertThat(response.getTreatmentSuggestions()).isEqualTo("Chườm ấm hạ sốt, súc họng nước muối");
        assertThat(response.getDiseaseGroupId()).isEqualTo(5);
        assertThat(response.getCompletedAt()).isNull();
        assertThat(confirmedAppointment.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("Hoàn tất phiếu khám (COMPLETED qua PUT /api/medical-records/{id}): Đổi lịch hẹn sang COMPLETED, ghi nhận completed_at và khóa Read-only")
    void updateCheckup_TransitionToCompleted_UpdatesAppointmentAndLocksReadOnly() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));
        when(diseaseGroupRepository.findById(5)).thenReturn(Optional.of(fluDiseaseGroup));
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(i -> i.getArgument(0));

        UpdateMedicalCheckupRequest completeRequest = UpdateMedicalCheckupRequest.builder()
                .symptoms("Sốt 38.8 độ, đau rát họng")
                .diagnosisNotes("Viêm họng cấp")
                .treatmentSuggestions("Hạ sốt tại trạm, nghỉ ngơi")
                .diseaseGroupId(5)
                .status("COMPLETED")
                .build();

        MedicalCheckupDetailResponse response = medicalCheckupService.updateCheckup(1L, completeRequest, "bs001");

        assertThat(response.getStatus()).isEqualTo("COMPLETED");
        assertThat(response.getCompletedAt()).isNotNull();
        assertThat(confirmedAppointment.getStatus()).isEqualTo("COMPLETED");
        verify(appointmentRepository).save(confirmedAppointment);

        // Thử chỉnh sửa lại sau khi đã COMPLETED -> Bị chặn cứng Read-only vĩnh viễn!
        UpdateMedicalCheckupRequest editAfterComplete = UpdateMedicalCheckupRequest.builder()
                .symptoms("Thử sửa lại triệu chứng sau khi đã khóa")
                .diagnosisNotes("Thử sửa chẩn đoán")
                .status("DRAFT")
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updateCheckup(1L, editAfterComplete, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Phiếu khám đã hoàn tất (COMPLETED), hồ sơ đã bị khóa Read-only vĩnh viễn và không thể chỉnh sửa");
    }

    @Test
    @DisplayName("Hoàn tất phiếu khám qua completeCheckup: Thiếu triệu chứng hoặc chẩn đoán sẽ bị từ chối")
    void completeCheckup_ValidationAndSuccess() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        // 1. Thiếu triệu chứng
        draftCheckup.setSymptoms(null);
        assertThatThrownBy(() -> medicalCheckupService.completeCheckup(1L, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Vui lòng nhập triệu chứng (symptoms) trước khi hoàn tất phiếu khám");

        // 2. Thiếu chẩn đoán
        draftCheckup.setSymptoms("Đau bụng thượng vị");
        draftCheckup.setDiagnosisNotes("   ");
        assertThatThrownBy(() -> medicalCheckupService.completeCheckup(1L, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Vui lòng nhập kết luận chẩn đoán (diagnosisNotes) trước khi hoàn tất phiếu khám");

        // 3. Đủ thông tin -> Hoàn tất thành công
        draftCheckup.setDiagnosisNotes("Viêm dạ dày cấp");
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(i -> i.getArgument(0));

        MedicalCheckupDetailResponse completed = medicalCheckupService.completeCheckup(1L, "bs001");
        assertThat(completed.getStatus()).isEqualTo("COMPLETED");
        assertThat(completed.getCompletedAt()).isNotNull();
        assertThat(confirmedAppointment.getStatus()).isEqualTo("COMPLETED");

        // 4. Gọi hoàn tất lần nữa trên phiếu đã COMPLETED -> Bị chặn
        assertThatThrownBy(() -> medicalCheckupService.completeCheckup(1L, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("khóa Read-only vĩnh viễn");
    }

    @Test
    @DisplayName("Quản lý xóa phiếu khám: Cho phép xóa bản nháp DRAFT, chặn cứng vĩnh viễn khi đã COMPLETED")
    void deleteCheckup_AllowsDraft_BlocksCompletedForever() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));

        // 1. Phiếu đã COMPLETED -> Chặn xóa vĩnh viễn
        when(medicalCheckupRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(completedCheckup));
        assertThatThrownBy(() -> medicalCheckupService.deleteCheckup(2L, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Phiếu khám đã hoàn tất (COMPLETED), hồ sơ đã bị khóa Read-only vĩnh viễn và không thể xóa");
        verify(medicalCheckupRepository, never()).delete(completedCheckup);

        // 2. Bác sĩ khác cố xóa phiếu DRAFT không thuộc mình -> Chặn 403
        when(userRepository.findByUsername("bs002")).thenReturn(Optional.of(otherDoctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));
        assertThatThrownBy(() -> medicalCheckupService.deleteCheckup(1L, "bs002"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ bác sĩ phụ trách ca khám mới có quyền xóa phiếu khám này");

        // 3. Bác sĩ phụ trách xóa phiếu DRAFT -> Thành công
        medicalCheckupService.deleteCheckup(1L, "bs001");
        verify(medicalCheckupRepository).delete(draftCheckup);
    }

    @Test
    @DisplayName("Tra cứu phiếu khám theo mã lịch hẹn (getCheckupByAppointment): Phân quyền đúng cho Bác sĩ, Sinh viên và chặn Admin")
    void getCheckupByAppointment_SuccessAndSecurity() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByAppointmentIdWithDetails(100L)).thenReturn(Optional.of(draftCheckup));

        MedicalCheckupDetailResponse doctorRes = medicalCheckupService.getCheckupByAppointment(100L, "bs001");
        assertThat(doctorRes.getId()).isEqualTo(1L);
        assertThat(doctorRes.getAppointmentId()).isEqualTo(100L);

        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        MedicalCheckupDetailResponse studentRes = medicalCheckupService.getCheckupByAppointment(100L, "sv001");
        assertThat(studentRes.getId()).isEqualTo(1L);

        when(userRepository.findByUsername("sv002")).thenReturn(Optional.of(otherStudentUser));
        assertThatThrownBy(() -> medicalCheckupService.getCheckupByAppointment(100L, "sv002"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Sinh viên chỉ được phép xem thông tin phiếu khám của chính mình");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertThatThrownBy(() -> medicalCheckupService.getCheckupByAppointment(100L, "admin"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Ban quản lý không có quyền truy cập");
    }

    @Test
    @DisplayName("Kiểm tra các nhánh chuẩn hóa loại phiếu, kết quả định kỳ, trạng thái và fallback MedicalStaff")
    void checkupNormalizationAndEdgeBranches() {
        // 1. Constructor 2 tham số backward-compatible
        MedicalCheckupServiceImpl twoArgService = new MedicalCheckupServiceImpl(medicalCheckupRepository, userRepository);
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));
        assertThat(twoArgService.getCheckupDetail(1L, "bs001").getId()).isEqualTo(1L);

        // 2. Bác sĩ lấy MedicalStaff từ medicalStaffRepository khi user.medicalStaff == null
        doctorUser.setMedicalStaff(null);
        when(medicalStaffRepository.findById(20L)).thenReturn(Optional.of(doctorStaff));
        when(appointmentRepository.findByIdWithDetails(101L)).thenReturn(Optional.of(
                Appointment.builder()
                        .id(101L)
                        .appointmentCode("LH2611120002")
                        .student(student)
                        .shift(psychologicalShift)
                        .status("CONFIRMED")
                        .build()
        ));
        when(medicalCheckupRepository.findByAppointmentIdWithDetails(101L)).thenReturn(Optional.empty());
        when(medicalCheckupRepository.existsByCheckupCode(anyString())).thenReturn(false);
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(i -> i.getArgument(0));

        CreateMedicalCheckupRequest psychReq = CreateMedicalCheckupRequest.builder()
                .appointmentId(101L)
                .studentId(10L)
                .studentCode("24521234")
                .periodicResult("DAT")
                .diagnosisNotes("Tâm lý ổn định")
                .status("DA_HOAN_TAT")
                .build();
        MedicalCheckupDetailResponse psychRes = medicalCheckupService.createCheckup(psychReq, "bs001");
        assertThat(psychRes.getCheckupType()).isEqualTo("PSYCHOLOGICAL");
        assertThat(psychRes.getPeriodicResult()).isEqualTo("PASSED");
        assertThat(psychRes.getStatus()).isEqualTo("COMPLETED");

        // 3. Lỗi loại phiếu, kết quả định kỳ, trạng thái không hợp lệ
        UpdateMedicalCheckupRequest invalidTypeReq = UpdateMedicalCheckupRequest.builder()
                .checkupType("INVALID_TYPE")
                .build();
        assertThatThrownBy(() -> medicalCheckupService.updateCheckup(1L, invalidTypeReq, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Loại phiếu khám không hợp lệ");

        UpdateMedicalCheckupRequest invalidPeriodicReq = UpdateMedicalCheckupRequest.builder()
                .periodicResult("UNKNOWN")
                .build();
        assertThatThrownBy(() -> medicalCheckupService.updateCheckup(1L, invalidPeriodicReq, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Kết quả khám định kỳ không hợp lệ");

        UpdateMedicalCheckupRequest invalidStatusReq = UpdateMedicalCheckupRequest.builder()
                .status("ARCHIVED")
                .build();
        assertThatThrownBy(() -> medicalCheckupService.updateCheckup(1L, invalidStatusReq, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Trạng thái phiếu khám không hợp lệ");
    }

    // =========================================================================
    // 4. Existing tests for getCheckupDetail & updatePrescription (SP4-02)
    // =========================================================================

    @Test
    @DisplayName("Sinh viên xem chi tiết phiếu khám của chính mình: Thành công và trả về đầy đủ đơn thuốc")
    void getCheckupDetail_Success_ByStudent_OwnCheckup() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        MedicalCheckupDetailResponse response = medicalCheckupService.getCheckupDetail(1L, "sv001");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCheckupCode()).isEqualTo("PK2611040001");
        assertThat(response.getStudentId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo("DRAFT");
        assertThat(response.getPrescribedMedicines()).hasSize(1);
        assertThat(response.getPrescribedMedicines().get(0).getMedicineName()).isEqualTo("Paracetamol 500mg");
    }

    @Test
    @DisplayName("Bác sĩ xem chi tiết phiếu khám của sinh viên: Thành công để phục vụ tra cứu tiền sử bệnh")
    void getCheckupDetail_Success_ByDoctor() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        MedicalCheckupDetailResponse response = medicalCheckupService.getCheckupDetail(1L, "bs001");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getDoctorName()).isEqualTo("Bác sĩ Nguyễn Văn A");
    }

    @Test
    @DisplayName("Sinh viên cố tình xem phiếu khám của sinh viên khác: Chặn với AccessDeniedException")
    void getCheckupDetail_Forbidden_ByStudent_OtherStudentCheckup() {
        when(userRepository.findByUsername("sv002")).thenReturn(Optional.of(otherStudentUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        assertThatThrownBy(() -> medicalCheckupService.getCheckupDetail(1L, "sv002"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Sinh viên chỉ được phép xem thông tin phiếu khám của chính mình");
    }

    @Test
    @DisplayName("Ban Quản lý cố xem chi tiết phiếu khám: Chặn với AccessDeniedException vì tính riêng tư bệnh án")
    void getCheckupDetail_Forbidden_ByAdmin() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        assertThatThrownBy(() -> medicalCheckupService.getCheckupDetail(1L, "admin"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Ban quản lý không có quyền truy cập hồ sơ bệnh án chi tiết của sinh viên");
    }

    @Test
    @DisplayName("Xem chi tiết phiếu khám không tồn tại: Ném ResourceNotFoundException")
    void getCheckupDetail_NotFound() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(medicalCheckupRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalCheckupService.getCheckupDetail(999L, "sv001"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy phiếu khám với ID: 999");
    }

    @Test
    @DisplayName("Bác sĩ phụ trách cập nhật đơn thuốc khi phiếu khám ở trạng thái DRAFT: Thành công")
    void updatePrescription_Success_WhenDraft() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(i -> i.getArgument(0));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Paracetamol 500mg", 10, "Uống 1 viên khi sốt"),
                        new PrescribedMedicineRequest("Vitamin C 500mg", 20, "Uống 1 viên mỗi sáng sau ăn")
                ))
                .build();

        MedicalCheckupDetailResponse response = medicalCheckupService.updatePrescription(1L, request, "bs001");

        assertThat(response).isNotNull();
        assertThat(response.getPrescribedMedicines()).hasSize(2);
        assertThat(response.getPrescribedMedicines())
                .extracting(PrescribedMedicineResponse::getMedicineName)
                .containsExactly("Paracetamol 500mg", "Vitamin C 500mg");

        verify(medicalCheckupRepository).save(draftCheckup);
    }

    @Test
    @DisplayName("Trường hợp không kê thuốc : Cập nhật thành công danh sách rỗng")
    void updatePrescription_Success_WhenNoMedicinesPrescribed() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(i -> i.getArgument(0));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(new ArrayList<>())
                .build();

        MedicalCheckupDetailResponse response = medicalCheckupService.updatePrescription(1L, request, "bs001");

        assertThat(response).isNotNull();
        assertThat(response.getPrescribedMedicines()).isEmpty();
        verify(medicalCheckupRepository).save(draftCheckup);
    }

    @Test
    @DisplayName("Cố tình sửa đơn thuốc khi phiếu khám đã COMPLETED: Bị khóa Read-only, ném BadRequestException")
    void updatePrescription_Failed_WhenCompleted() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(completedCheckup));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Kháng sinh Amoxicillin", 14, "Uống ngày 2 viên")
                ))
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updatePrescription(2L, request, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Phiếu khám đã hoàn tất (COMPLETED), đơn thuốc đã bị khóa vĩnh viễn và không thể chỉnh sửa");

        verify(medicalCheckupRepository, never()).save(any());
    }

    @Test
    @DisplayName("Bác sĩ không phụ trách phiếu khám cố tình sửa đơn thuốc: Ném AccessDeniedException")
    void updatePrescription_Forbidden_WhenNotAssignedDoctor() {
        when(userRepository.findByUsername("bs002")).thenReturn(Optional.of(otherDoctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Kháng sinh Amoxicillin", 14, "Uống ngày 2 viên")
                ))
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updatePrescription(1L, request, "bs002"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ bác sĩ phụ trách ca khám mới có quyền kê đơn thuốc cho phiếu khám này");

        verify(medicalCheckupRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tài khoản Sinh viên cố tình gọi kê đơn thuốc: Ném AccessDeniedException")
    void updatePrescription_Forbidden_WhenStudentRole() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Paracetamol 500mg", 5, "Uống khi sốt")
                ))
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updatePrescription(1L, request, "sv001"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ tài khoản Bác sĩ / Nhân viên y tế mới có quyền kê đơn thuốc");

        verify(medicalCheckupRepository, never()).save(any());
    }

    @Test
    @DisplayName("Kê đơn thuốc trùng tên trong cùng một lần kê: Ném BadRequestException")
    void updatePrescription_Failed_DuplicateMedicineName() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Paracetamol 500mg", 10, "Uống 1 viên khi sốt"),
                        new PrescribedMedicineRequest("  paracetamol 500mg  ", 5, "Uống thêm nếu đau đầu")
                ))
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updatePrescription(1L, request, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("bị trùng lặp trong đơn thuốc");

        verify(medicalCheckupRepository, never()).save(any());
    }

    @Test
    @DisplayName("Kê đơn thuốc có số lượng không hợp lệ (<= 0): Ném BadRequestException")
    void updatePrescription_Failed_InvalidQuantity() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("Paracetamol 500mg", 0, "Uống khi sốt")
                ))
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updatePrescription(1L, request, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Số lượng thuốc phải lớn hơn hoặc bằng 1");
    }

    @Test
    @DisplayName("Kê đơn thuốc có tên rỗng: Ném BadRequestException")
    void updatePrescription_Failed_BlankMedicineName() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        UpdatePrescriptionRequest request = UpdatePrescriptionRequest.builder()
                .medicines(List.of(
                        new PrescribedMedicineRequest("   ", 10, "Uống khi sốt")
                ))
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updatePrescription(1L, request, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Tên thuốc không được để trống");
    }

    // =========================================================================
    // 5. Tests for getStudentMedicalHistory (Timeline phía Sinh viên)
    // =========================================================================

    @Test
    @DisplayName("Timeline lịch sử khám phía Sinh viên: Trả đủ Đa khoa, Tâm lý, Định kỳ, Cấp cứu + Đơn thuốc + Chuyển viện, sắp xếp mới nhất trước và lọc bỏ DRAFT")
    void getStudentMedicalHistory_Success_AllTypesWithMedicinesAndTransferRecord() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));

        OffsetDateTime now = OffsetDateTime.now();

        PrescribedMedicine med1 = PrescribedMedicine.builder()
                .id(101L)
                .medicineName("Paracetamol 500mg")
                .quantity(10)
                .dosageInstruction("Uống 1 viên khi sốt")
                .build();
        PrescribedMedicine med2 = PrescribedMedicine.builder()
                .id(102L)
                .medicineName("Vitamin C 500mg")
                .quantity(15)
                .dosageInstruction("Uống 1 viên sau ăn sáng")
                .build();

        // 1. Khám đa khoa theo lịch hẹn, có đơn thuốc (cũ nhất)
        MedicalCheckup generalCheckup = MedicalCheckup.builder()
                .id(10L)
                .checkupCode("PK2611010001")
                .student(student)
                .staff(doctorStaff)
                .appointment(confirmedAppointment)
                .checkupType("GENERAL")
                .isEmergency(false)
                .symptoms("Sốt, ho khan")
                .diagnosisNotes("Cảm cúm")
                .treatmentSuggestions("Nghỉ ngơi")
                .diseaseGroup(fluDiseaseGroup)
                .status("COMPLETED")
                .createdAt(now.minusDays(5))
                .completedAt(now.minusDays(5))
                .prescribedMedicines(new ArrayList<>(List.of(med2, med1)))
                .build();

        // 2. Tư vấn tâm lý (không thuốc, không chuyển viện)
        Appointment psychAppointment = Appointment.builder()
                .id(102L)
                .appointmentCode("LH2611030001")
                .student(student)
                .shift(psychologicalShift)
                .status("COMPLETED")
                .build();
        MedicalCheckup psychCheckup = MedicalCheckup.builder()
                .id(11L)
                .checkupCode("PK2611030001")
                .student(student)
                .staff(doctorStaff)
                .appointment(psychAppointment)
                .checkupType("PSYCHOLOGICAL")
                .isEmergency(false)
                .symptoms("Mất ngủ trước kỳ thi")
                .diagnosisNotes("Căng thẳng nhẹ")
                .treatmentSuggestions("Thực hành thở 4-7-8")
                .status("COMPLETED")
                .createdAt(now.minusDays(3))
                .completedAt(now.minusDays(3))
                .build();

        // 3. Khám định kỳ (không qua lịch hẹn, periodicResult = PASSED)
        MedicalCheckup periodicCheckup = MedicalCheckup.builder()
                .id(12L)
                .checkupCode("PK2611050001")
                .student(student)
                .staff(doctorStaff)
                .appointment(null)
                .checkupType("GENERAL")
                .isEmergency(false)
                .periodicResult("PASSED")
                .symptoms("Khám sức khỏe định kỳ đầu năm")
                .diagnosisNotes("Sức khỏe loại 1, đạt chuẩn")
                .status("COMPLETED")
                .createdAt(now.minusDays(2))
                .completedAt(now.minusDays(2))
                .build();

        // 4. Ca cấp cứu có hồ sơ chuyển viện (mới nhất)
        MedicalCheckup emergencyTransferCheckup = MedicalCheckup.builder()
                .id(13L)
                .checkupCode("PK2611100001")
                .student(student)
                .staff(doctorStaff)
                .appointment(null)
                .checkupType("GENERAL")
                .isEmergency(true)
                .symptoms("Ngất xỉu, huyết áp tăng cao")
                .diagnosisNotes("Cơn tăng huyết áp kịch phát")
                .treatmentSuggestions("Sơ cứu tại chỗ và chuyển tuyến trên")
                .status("COMPLETED")
                .createdAt(now.minusDays(1))
                .completedAt(now.minusDays(1))
                .build();
        TransferRecord transferRecord = TransferRecord.builder()
                .id(1L)
                .recordCode("CV2611100001")
                .medicalCheckup(emergencyTransferCheckup)
                .targetHospital("Bệnh viện Đa khoa Khu vực Thủ Đức")
                .reasonCategory("CAP_CUU_NANG")
                .transferReason("Cần theo dõi tim mạch chuyên sâu")
                .currentCondition("Đã tỉnh, huyết áp 150/90")
                .firstAidNotes("Nằm nghỉ, thở oxy")
                .transferredAt(now.minusDays(1))
                .build();
        emergencyTransferCheckup.setTransferRecord(transferRecord);

        // Phiếu nháp DRAFT và phiếu của SV khác (phòng vệ nếu repo trả lẫn) -> phải bị lọc bỏ
        MedicalCheckup otherStudentCheckup = MedicalCheckup.builder()
                .id(99L)
                .checkupCode("PK2611110099")
                .student(otherStudent)
                .staff(doctorStaff)
                .checkupType("GENERAL")
                .status("COMPLETED")
                .createdAt(now)
                .completedAt(now)
                .build();

        when(medicalCheckupRepository.findCompletedTimelineByStudentIdWithDetails(10L))
                .thenReturn(List.of(generalCheckup, draftCheckup, otherStudentCheckup, periodicCheckup, emergencyTransferCheckup, psychCheckup));

        List<MedicalCheckupDetailResponse> timeline = medicalCheckupService.getStudentMedicalHistory("sv001", null);

        assertThat(timeline).hasSize(4);
        assertThat(timeline)
                .extracting(MedicalCheckupDetailResponse::getCheckupCode)
                .containsExactly("PK2611100001", "PK2611050001", "PK2611030001", "PK2611010001");

        // Kiểm tra item 0: Cấp cứu + Chuyển viện
        MedicalCheckupDetailResponse item0 = timeline.get(0);
        assertThat(item0.getIsEmergency()).isTrue();
        assertThat(item0.getAppointmentId()).isNull();
        assertThat(item0.getTimelineCategory()).isEqualTo("GENERAL");
        TransferRecordResponse transferRes = item0.getTransferRecord();
        assertThat(transferRes).isNotNull();
        assertThat(transferRes.getRecordCode()).isEqualTo("CV2611100001");
        assertThat(transferRes.getTargetHospital()).isEqualTo("Bệnh viện Đa khoa Khu vực Thủ Đức");
        assertThat(transferRes.getReasonCategory()).isEqualTo("CAP_CUU_NANG");

        // Kiểm tra item 1: Khám định kỳ
        MedicalCheckupDetailResponse item1 = timeline.get(1);
        assertThat(item1.getIsPeriodic()).isTrue();
        assertThat(item1.getPeriodicResult()).isEqualTo("PASSED");
        assertThat(item1.getTimelineCategory()).isEqualTo("PERIODIC");
        assertThat(item1.getTransferRecord()).isNull();

        // Kiểm tra item 2: Tư vấn tâm lý + Ca khám
        MedicalCheckupDetailResponse item2 = timeline.get(2);
        assertThat(item2.getIsPeriodic()).isFalse();
        assertThat(item2.getTimelineCategory()).isEqualTo("PSYCHOLOGICAL");
        assertThat(item2.getRoom()).isEqualTo("Phòng Tâm lý 202");
        assertThat(item2.getPrescribedMedicines()).isEmpty();

        // Kiểm tra item 3: Khám đa khoa + Đơn thuốc sắp xếp theo ID tăng dần
        MedicalCheckupDetailResponse item3 = timeline.get(3);
        assertThat(item3.getTimelineCategory()).isEqualTo("GENERAL");
        assertThat(item3.getShiftDate()).isEqualTo(generalShift.getShiftDate());
        assertThat(item3.getPrescribedMedicines())
                .extracting(PrescribedMedicineResponse::getMedicineName)
                .containsExactly("Paracetamol 500mg", "Vitamin C 500mg");
    }

    @Test
    @DisplayName("Timeline lịch sử khám phía Sinh viên: Chưa từng đi khám trả danh sách rỗng và cho phép truyền đúng studentId của chính mình")
    void getStudentMedicalHistory_EmptyList_AndMatchingStudentId() {
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        when(medicalCheckupRepository.findCompletedTimelineByStudentIdWithDetails(10L))
                .thenReturn(List.of());

        List<MedicalCheckupDetailResponse> result = medicalCheckupService.getStudentMedicalHistory("sv001", 10L);

        assertThat(result).isEmpty();
        verify(medicalCheckupRepository).findCompletedTimelineByStudentIdWithDetails(10L);
    }

    @Test
    @DisplayName("Bảo mật Timeline phía Sinh viên: Chặn tuyệt đối IDOR khi truyền studentId khác và chặn Bác sĩ/Ban quản lý")
    void getStudentMedicalHistory_Security_BlocksOtherStudentAndNonStudentRoles() {
        // 1. Sinh viên sv001 (id=10) cố tình truyền requestedStudentId=11 của sv002 -> Chặn 403
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        assertThatThrownBy(() -> medicalCheckupService.getStudentMedicalHistory("sv001", 11L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Sinh viên tuyệt đối không được phép xem lịch sử khám của sinh viên khác");
        verify(medicalCheckupRepository, never()).findCompletedTimelineByStudentIdWithDetails(any());

        // 2. Bác sĩ gọi endpoint riêng của sinh viên -> Chặn 403
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        assertThatThrownBy(() -> medicalCheckupService.getStudentMedicalHistory("bs001", null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ tài khoản sinh viên");

        // 3. Ban quản lý gọi endpoint -> Chặn 403
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertThatThrownBy(() -> medicalCheckupService.getStudentMedicalHistory("admin", null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ tài khoản sinh viên");

        // 4. Chưa đăng nhập (principal null) -> Chặn 403
        assertThatThrownBy(() -> medicalCheckupService.getStudentMedicalHistory(null, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Yêu cầu đăng nhập");
    }

    @Test
    @DisplayName("Lập Hồ sơ Chuyển viện (POST /api/medical-records/{id}/transfer): Thành công, tự động kết thúc ca khám và khóa Read-only")
    void createTransferRecord_Success_And_AutoCompleteAndLock() {
        draftCheckup.setIsEmergency(true);
        draftCheckup.setSymptoms(null);
        draftCheckup.setDiagnosisNotes(null);
        draftCheckup.setTreatmentSuggestions(null);
        draftCheckup.setAppointment(confirmedAppointment);

        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));
        when(transferRecordRepository.existsByMedicalCheckupId(1L)).thenReturn(false);
        when(transferRecordRepository.existsByRecordCode(anyString())).thenReturn(false);
        when(transferRecordRepository.save(any(TransferRecord.class))).thenAnswer(inv -> {
            TransferRecord tr = inv.getArgument(0);
            tr.setId(77L);
            return tr;
        });
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(inv -> inv.getArgument(0));

        OffsetDateTime transferTime = OffsetDateTime.now();
        CreateTransferRecordRequest request = new CreateTransferRecordRequest(
                "Bệnh viện Đa khoa Khu vực Thủ Đức",
                "CAP_CUU_NANG",
                "Nghi ngờ viêm ruột thừa cấp cần can thiệp ngoại khoa",
                "Đau hố chậu phải dữ dội, sốt 38.5 độ C",
                "Theo dõi sinh hiệu, chườm ấm, đặt đường truyền tĩnh mạch",
                "Không từ chối",
                transferTime
        );

        MedicalCheckupDetailResponse res = medicalCheckupService.createTransferRecord(1L, request, "bs001");

        assertThat(res).isNotNull();
        assertThat(res.getStatus()).isEqualTo("COMPLETED");
        assertThat(res.getCompletedAt()).isEqualTo(transferTime);
        assertThat(res.getCurrentStep()).isEqualTo("CHUYEN_VIEN");
        assertThat(res.getCurrentStepLabel()).isEqualTo("Chuyển viện ngoài");
        assertThat(confirmedAppointment.getStatus()).isEqualTo("COMPLETED");
        assertThat(res.getTransferRecord()).isNotNull();
        assertThat(res.getTransferRecord().getId()).isEqualTo(77L);
        assertThat(res.getTransferRecord().getRecordCode()).startsWith("CV");
        assertThat(res.getTransferRecord().getTargetHospital()).isEqualTo("Bệnh viện Đa khoa Khu vực Thủ Đức");
        assertThat(res.getTransferRecord().getReasonCategory()).isEqualTo("CAP_CUU_NANG");
        assertThat(res.getTransferRecord().getRefusalNotes()).isEqualTo("Không từ chối");

        // Kiểm tra khóa Read-only: Gọi lại createTransferRecord hoặc updateCheckup đều bị chặn
        assertThatThrownBy(() -> medicalCheckupService.createTransferRecord(1L, request, "bs001"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã được xác nhận và khóa chỉ đọc");

        assertThatThrownBy(() -> medicalCheckupService.updateCheckup(1L, new UpdateMedicalCheckupRequest(), "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Read-only");
    }

    @Test
    @DisplayName("Lập Hồ sơ Chuyển viện: Kiểm tra đầy đủ các nhánh validate (QUA_TAI, THIEU_THIET_BI, lỗi dữ liệu & phân quyền)")
    void createTransferRecord_ValidationAndSecurityErrors() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));

        // 1. Request null
        assertThatThrownBy(() -> medicalCheckupService.createTransferRecord(1L, null, "bs001"))
                .isInstanceOf(BadRequestException.class);

        // 2. Bệnh viện trống hoặc quá 150 ký tự
        assertThatThrownBy(() -> medicalCheckupService.createTransferRecord(
                1L, new CreateTransferRecordRequest("  ", "QUA_TAI", "Lý do", "Tình trạng", "Sơ cứu", null, null), "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Bệnh viện tuyến trên");

        assertThatThrownBy(() -> medicalCheckupService.createTransferRecord(
                1L, new CreateTransferRecordRequest("B".repeat(151), "QUA_TAI", "Lý do", "Tình trạng", "Sơ cứu", null, null), "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("150 ký tự");

        // 3. Phân loại lý do trống hoặc sai
        assertThatThrownBy(() -> medicalCheckupService.createTransferRecord(
                1L, new CreateTransferRecordRequest("BV A", "  ", "Lý do", "Tình trạng", "Sơ cứu", null, null), "bs001"))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> medicalCheckupService.createTransferRecord(
                1L, new CreateTransferRecordRequest("BV A", "SAI_LY_DO", "Lý do", "Tình trạng", "Sơ cứu", null, null), "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("QUA_TAI, THIEU_THIET_BI hoặc CAP_CUU_NANG");

        // 4. Lý do chi tiết / Tình trạng hiện tại / Biện pháp sơ cứu trống
        assertThatThrownBy(() -> medicalCheckupService.createTransferRecord(
                1L, new CreateTransferRecordRequest("BV A", "QUA_TAI", "  ", "Tình trạng", "Sơ cứu", null, null), "bs001"))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> medicalCheckupService.createTransferRecord(
                1L, new CreateTransferRecordRequest("BV A", "THIEU_THIET_BI", "Lý do", "  ", "Sơ cứu", null, null), "bs001"))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> medicalCheckupService.createTransferRecord(
                1L, new CreateTransferRecordRequest("BV A", "THIEU_THIET_BI", "Lý do", "Tình trạng", "  ", null, null), "bs001"))
                .isInstanceOf(BadRequestException.class);

        // 5. Phiếu khám đã COMPLETED trước đó (nhưng chưa có transferRecord) -> chặn lập chuyển viện
        when(medicalCheckupRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(completedCheckup));
        CreateTransferRecordRequest validReq = new CreateTransferRecordRequest(
                "BV Chợ Rẫy", "THIEU_THIET_BI", "Thiếu máy chụp CT", "Đau đầu", "Giảm đau", null, null
        );
        assertThatThrownBy(() -> medicalCheckupService.createTransferRecord(2L, validReq, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Read-only");

        // 6. Bác sĩ khác không phụ trách phiếu khám -> 403
        when(userRepository.findByUsername("bs002")).thenReturn(Optional.of(otherDoctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));
        assertThatThrownBy(() -> medicalCheckupService.createTransferRecord(1L, validReq, "bs002"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Kết xuất Giấy giới thiệu chuyển viện (GET /api/medical-records/{id}/transfer/print): Đầy đủ dữ liệu bản in & bảo mật")
    void getTransferReferralPaper_Success_And_SecurityChecks() {
        student.setDateOfBirth(LocalDate.of(2005, 4, 12));
        student.setGender("Nam");

        TransferRecord tr = TransferRecord.builder()
                .id(88L)
                .recordCode("CV2611180001")
                .medicalCheckup(completedCheckup)
                .targetHospital("Bệnh viện Đại học Y Dược TP.HCM")
                .reasonCategory("THIEU_THIET_BI")
                .transferReason("Cần chụp cộng hưởng từ chuyên sâu")
                .currentCondition("Tỉnh táo, sinh hiệu ổn")
                .firstAidNotes("Đã cố định tạm thời")
                .refusalNotes(null)
                .transferredAt(OffsetDateTime.now())
                .build();
        completedCheckup.setTransferRecord(tr);
        completedCheckup.setDiseaseGroup(fluDiseaseGroup);
        completedCheckup.addPrescribedMedicine(PrescribedMedicine.builder()
                .id(1L).medicineName("Paracetamol").quantity(2).dosageInstruction("Uống 1 viên").build());

        HealthRecord hr = new HealthRecord();
        hr.setHeight(BigDecimal.valueOf(172.0));
        hr.setWeight(BigDecimal.valueOf(64.0));
        hr.setBloodType("A");
        hr.setBmi(BigDecimal.valueOf(21.6));
        hr.setAllergies("Không dị ứng");
        hr.setUnderlyingDiseases("Không có");

        HealthInsurance hi = HealthInsurance.builder()
                .cardNumber("SV4792452000100")
                .initialHospital("BV Thủ Đức")
                .effectiveDate(LocalDate.of(2026, 1, 1))
                .expirationDate(LocalDate.of(2026, 12, 31))
                .build();

        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(completedCheckup));
        when(healthRecordRepository.findByStudentId(10L)).thenReturn(Optional.of(hr));
        when(healthInsuranceRepository.findByStudentIdWithDetails(10L)).thenReturn(Optional.of(hi));

        TransferReferralPrintResponse printData = medicalCheckupService.getTransferReferralPaper(2L, "bs001");

        assertThat(printData).isNotNull();
        assertThat(printData.getReferringUnitName()).isEqualTo(TransferReferralPrintResponse.REFERRING_UNIT_NAME);
        assertThat(printData.isReadOnly()).isTrue();
        assertThat(printData.getTransferId()).isEqualTo(88L);
        assertThat(printData.getRecordCode()).isEqualTo("CV2611180001");
        assertThat(printData.getTargetHospital()).isEqualTo("Bệnh viện Đại học Y Dược TP.HCM");
        assertThat(printData.getReasonCategory()).isEqualTo("THIEU_THIET_BI");
        assertThat(printData.getReasonCategoryLabel()).isEqualTo("Thiếu thiết bị / thuốc chuyên khoa");
        assertThat(printData.getStudentName()).isEqualTo("Nguyễn Văn Sinh Viên");
        assertThat(printData.getClassCode()).isEqualTo("SE104.O11");
        assertThat(printData.getFacultyName()).isEqualTo("Công nghệ Phần mềm");
        assertThat(printData.getInsuranceCardNumber()).isEqualTo("SV4792452000100");
        assertThat(printData.getHeight()).isEqualTo(172.0);
        assertThat(printData.getBmi()).isEqualTo(21.6);
        assertThat(printData.getDoctorName()).isEqualTo("Bác sĩ Nguyễn Văn A");
        assertThat(printData.getPrescribedMedicines()).hasSize(2);

        // Chính sinh viên của ca khám cũng có thể xem giấy giới thiệu chuyển viện của mình
        when(userRepository.findByUsername("sv001")).thenReturn(Optional.of(studentUser));
        TransferReferralPrintResponse studentPrint = medicalCheckupService.getTransferReferralPaper(2L, "sv001");
        assertThat(studentPrint.getRecordCode()).isEqualTo("CV2611180001");

        // Chặn sinh viên khác (IDOR) -> 403
        when(userRepository.findByUsername("sv002")).thenReturn(Optional.of(otherStudentUser));
        assertThatThrownBy(() -> medicalCheckupService.getTransferReferralPaper(2L, "sv002"))
                .isInstanceOf(AccessDeniedException.class);

        // Chặn Ban quản lý (BAN_QUAN_LY) -> 403
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertThatThrownBy(() -> medicalCheckupService.getTransferReferralPaper(2L, "admin"))
                .isInstanceOf(AccessDeniedException.class);

        // Phiếu khám chưa có hồ sơ chuyển viện -> 404
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));
        when(transferRecordRepository.findByMedicalCheckupId(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> medicalCheckupService.getTransferReferralPaper(1L, "bs001"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("chưa có hồ sơ chuyển viện");
    }

    @Test
    @DisplayName("Tiếp nhận khẩn cấp: Tạo phiếu, lấy danh sách lọc/phân trang, cập nhật tiến độ 4 bước và thống kê")
    void emergencyIntake_FullLifecycle_List_Progress_And_Stats() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(studentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(student));
        when(medicalCheckupRepository.existsByCheckupCode(anyString())).thenReturn(false);
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(inv -> {
            MedicalCheckup mc = inv.getArgument(0);
            if (mc.getId() == null) {
                mc.setId(500L);
            }
            return mc;
        });

        // 1. Tạo phiếu tiếp nhận khẩn cấp
        CreateMedicalCheckupRequest intakeReq = CreateMedicalCheckupRequest.builder()
                .studentId(10L)
                .isEmergency(true)
                .emergencyLevel("KHAN_CAP")
                .emergencyType("Chấn thương thể thao")
                .reporterContact("Nguyễn Văn Bạn - 0909111222")
                .symptoms("Trật khớp cổ chân phải khi đá bóng")
                .build();

        MedicalCheckupDetailResponse created = medicalCheckupService.createCheckup(intakeReq, "bs001");
        assertThat(created.getId()).isEqualTo(500L);
        assertThat(created.getIsEmergency()).isTrue();
        assertThat(created.getEmergencyLevel()).isEqualTo("KHAN_CAP");
        assertThat(created.getEmergencyType()).isEqualTo("Chấn thương thể thao");
        assertThat(created.getReporterContact()).isEqualTo("Nguyễn Văn Bạn - 0909111222");
        assertThat(created.getCurrentStep()).isEqualTo("TIEP_NHAN");
        assertThat(created.getCurrentStepLabel()).isEqualTo("Đã tiếp nhận");

        // 2. Cập nhật tiến độ ca cấp cứu sang SO_CUU -> KET_THUC
        MedicalCheckup emergencyEntity = MedicalCheckup.builder()
                .id(500L)
                .checkupCode(created.getCheckupCode())
                .student(student)
                .staff(doctorStaff)
                .checkupType("GENERAL")
                .isEmergency(true)
                .emergencyLevel("KHAN_CAP")
                .emergencyType("Chấn thương thể thao")
                .reporterContact("Nguyễn Văn Bạn - 0909111222")
                .currentStep("TIEP_NHAN")
                .symptoms("Trật khớp cổ chân phải")
                .status("DRAFT")
                .createdAt(OffsetDateTime.now())
                .build();

        when(medicalCheckupRepository.findByIdWithDetails(500L)).thenReturn(Optional.of(emergencyEntity));

        UpdateEmergencyProgressRequest firstAidProgress = new UpdateEmergencyProgressRequest(
                "SO_CUU", "Chườm đá lạnh, băng thun cố định cổ chân", "Chấn thương phần mềm cổ chân"
        );
        MedicalCheckupDetailResponse afterFirstAid = medicalCheckupService.updateEmergencyProgress(500L, firstAidProgress, "bs001");
        assertThat(afterFirstAid.getCurrentStep()).isEqualTo("SO_CUU");
        assertThat(afterFirstAid.getCurrentStepLabel()).isEqualTo("Sơ cứu ban đầu");
        assertThat(afterFirstAid.getStatus()).isEqualTo("DRAFT");
        assertThat(afterFirstAid.getTreatmentSuggestions()).isEqualTo("Chườm đá lạnh, băng thun cố định cổ chân");

        // 3. Danh sách ca cấp cứu (lọc & phân trang) và Thống kê trước khi kết thúc
        MedicalCheckup transferredEmergency = MedicalCheckup.builder()
                .id(501L)
                .checkupCode("PK2611180501")
                .student(otherStudent)
                .staff(doctorStaff)
                .checkupType("GENERAL")
                .isEmergency(true)
                .emergencyLevel("CAP_CUU")
                .emergencyType("Ngất xỉu")
                .currentStep("CHUYEN_VIEN")
                .status("COMPLETED")
                .createdAt(OffsetDateTime.now())
                .completedAt(OffsetDateTime.now())
                .build();

        when(medicalCheckupRepository.findEmergencyCasesWithDetails())
                .thenReturn(List.of(emergencyEntity, transferredEmergency));

        EmergencyCasePageResponse pageRes = medicalCheckupService.getEmergencyCases(
                "24521234", "KHAN_CAP", "Chấn thương thể thao", "SO_CUU", "DRAFT", LocalDate.now(), 0, 10, "bs001"
        );
        assertThat(pageRes.getTotalElements()).isEqualTo(1L);
        assertThat(pageRes.getItems()).hasSize(1);
        assertThat(pageRes.getContent()).hasSize(1);
        assertThat(pageRes.getCases()).hasSize(1);
        assertThat(pageRes.getItems().get(0).getCheckupCode()).isEqualTo(created.getCheckupCode());

        EmergencyStatsResponse stats = medicalCheckupService.getEmergencyStats("bs001");
        assertThat(stats.getInProgressCount()).isEqualTo(1L);
        assertThat(stats.getProcessingCount()).isEqualTo(1L);
        assertThat(stats.getFirstAidCount()).isEqualTo(1L);
        assertThat(stats.getTransferringCount()).isEqualTo(1L);
        assertThat(stats.getCompletedTodayCount()).isEqualTo(1L);

        // 4. Chuyển bước sang KET_THUC -> tự động hoàn tất ca khám
        UpdateEmergencyProgressRequest finishProgress = new UpdateEmergencyProgressRequest("KET_THUC", null, null);
        MedicalCheckupDetailResponse finished = medicalCheckupService.updateEmergencyProgress(500L, finishProgress, "bs001");
        assertThat(finished.getCurrentStep()).isEqualTo("KET_THUC");
        assertThat(finished.getCurrentStepLabel()).isEqualTo("Kết thúc ca");
        assertThat(finished.getStatus()).isEqualTo("COMPLETED");
        assertThat(finished.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Kiểm tra các nhánh biên của DTO và validate lỗi trong Tiếp nhận khẩn cấp")
    void emergencyIntake_ValidationAndDtoEdgeCases() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));

        // 1. updateEmergencyProgress với request rỗng hoặc bước không hợp lệ hoặc không phải ca cấp cứu
        assertThatThrownBy(() -> medicalCheckupService.updateEmergencyProgress(1L, null, "bs001"))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> medicalCheckupService.updateEmergencyProgress(1L, new UpdateEmergencyProgressRequest("  ", null, null), "bs001"))
                .isInstanceOf(BadRequestException.class);

        draftCheckup.setIsEmergency(false);
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));
        assertThatThrownBy(() -> medicalCheckupService.updateEmergencyProgress(
                1L, new UpdateEmergencyProgressRequest("SO_CUU", null, null), "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("ca tiếp nhận khẩn cấp");

        draftCheckup.setIsEmergency(true);
        assertThatThrownBy(() -> medicalCheckupService.updateEmergencyProgress(
                1L, new UpdateEmergencyProgressRequest("BUOC_SAI", null, null), "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("TIEP_NHAN, SO_CUU, CHUYEN_VIEN hoặc KET_THUC");

        // 2. Độ dài vượt quá giới hạn khi tạo phiếu khẩn cấp
        when(studentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(student));
        CreateMedicalCheckupRequest longLevelReq = CreateMedicalCheckupRequest.builder()
                .studentId(10L).isEmergency(true).emergencyLevel("L".repeat(25)).build();
        assertThatThrownBy(() -> medicalCheckupService.createCheckup(longLevelReq, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("20 ký tự");

        CreateMedicalCheckupRequest longTypeReq = CreateMedicalCheckupRequest.builder()
                .studentId(10L).isEmergency(true).emergencyType("T".repeat(55)).build();
        assertThatThrownBy(() -> medicalCheckupService.createCheckup(longTypeReq, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("50 ký tự");

        CreateMedicalCheckupRequest longContactReq = CreateMedicalCheckupRequest.builder()
                .studentId(10L).isEmergency(true).reporterContact("C".repeat(155)).build();
        assertThatThrownBy(() -> medicalCheckupService.createCheckup(longContactReq, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("150 ký tự");

        // 3. DTO coverage cho EmergencyCasePageResponse, EmergencyStatsResponse, TransferReferralPrintResponse
        EmergencyCasePageResponse emptyPage = new EmergencyCasePageResponse();
        emptyPage.setItems(null);
        emptyPage.setPage(0);
        emptyPage.setSize(10);
        emptyPage.setTotalElements(0L);
        emptyPage.setTotalPages(0);
        emptyPage.setHasNext(false);
        emptyPage.setHasPrevious(false);
        assertThat(emptyPage.getItems()).isEmpty();
        assertThat(emptyPage.getPage()).isZero();
        assertThat(emptyPage.getSize()).isEqualTo(10);
        assertThat(emptyPage.getTotalElements()).isZero();
        assertThat(emptyPage.getTotalPages()).isZero();
        assertThat(emptyPage.isHasNext()).isFalse();
        assertThat(emptyPage.isHasPrevious()).isFalse();

        EmergencyStatsResponse statsDto = new EmergencyStatsResponse();
        statsDto.setInProgressCount(2L);
        statsDto.setFirstAidCount(1L);
        statsDto.setTransferringCount(1L);
        statsDto.setCompletedTodayCount(3L);
        assertThat(statsDto.getInProgressCount()).isEqualTo(2L);
        assertThat(statsDto.getFirstAidCount()).isEqualTo(1L);
        assertThat(statsDto.getTransferringCount()).isEqualTo(1L);
        assertThat(statsDto.getCompletedTodayCount()).isEqualTo(3L);

        assertThat(TransferReferralPrintResponse.fromEntities(null, null, null, null)).isNull();
        assertThat(TransferReferralPrintResponse.fromEntities(draftCheckup, null, null, null)).isNull();
        assertThat(TransferReferralPrintResponse.resolveReasonCategoryLabel(null)).isNull();
        assertThat(TransferReferralPrintResponse.resolveReasonCategoryLabel("QUA_TAI")).isEqualTo("Quá tải");
        assertThat(TransferReferralPrintResponse.resolveReasonCategoryLabel("CAP_CUU_NANG")).contains("Cấp cứu nặng");
        assertThat(TransferReferralPrintResponse.resolveReasonCategoryLabel("OTHER")).isEqualTo("OTHER");

        // Kiểm tra các nhánh null-safe trong TransferReferralPrintResponse.fromEntities
        TransferRecord trMinimal = TransferRecord.builder().id(99L).recordCode("CV99").targetHospital("BV").reasonCategory("QUA_TAI").build();
        MedicalCheckup checkupMinimal = new MedicalCheckup();
        checkupMinimal.setId(99L);
        checkupMinimal.setPrescribedMedicines(null);
        HealthRecord emptyHr = new HealthRecord();
        TransferReferralPrintResponse minimalPrint = TransferReferralPrintResponse.fromEntities(checkupMinimal, trMinimal, emptyHr, null);
        assertThat(minimalPrint).isNotNull();
        assertThat(minimalPrint.getHeight()).isNull();
        assertThat(minimalPrint.getWeight()).isNull();
        assertThat(minimalPrint.getBmi()).isNull();
        assertThat(minimalPrint.getDiseaseGroupName()).isNull();
        assertThat(minimalPrint.getDoctorName()).isNull();

        Student studentNoUserNoClass = Student.builder().id(15L).studentCode("SV15").build();
        MedicalStaff staffNoUser = MedicalStaff.builder().id(25L).staffCode("NV25").build();
        checkupMinimal.setStudent(studentNoUserNoClass);
        checkupMinimal.setStaff(staffNoUser);
        TransferReferralPrintResponse partialPrint = TransferReferralPrintResponse.fromEntities(checkupMinimal, trMinimal, null, null);
        assertThat(partialPrint.getStudentId()).isEqualTo(15L);
        assertThat(partialPrint.getStaffId()).isEqualTo(25L);

        // Kiểm tra MedicalCheckupDetailResponse.resolveEffectiveStep & resolveStepLabel
        MedicalCheckup emNoStepDraft = MedicalCheckup.builder().isEmergency(true).status("DRAFT").currentStep("  ").build();
        assertThat(MedicalCheckupDetailResponse.resolveEffectiveStep(emNoStepDraft)).isEqualTo("TIEP_NHAN");
        MedicalCheckup emNoStepCompleted = MedicalCheckup.builder().isEmergency(true).status("COMPLETED").build();
        assertThat(MedicalCheckupDetailResponse.resolveEffectiveStep(emNoStepCompleted)).isEqualTo("KET_THUC");
        MedicalCheckup emNoStepTransferred = MedicalCheckup.builder().isEmergency(true).transferRecord(trMinimal).build();
        assertThat(MedicalCheckupDetailResponse.resolveEffectiveStep(emNoStepTransferred)).isEqualTo("CHUYEN_VIEN");
        assertThat(MedicalCheckupDetailResponse.resolveStepLabel("CUSTOM_STEP")).isEqualTo("CUSTOM_STEP");

        CreateTransferRecordRequest transferReqDto = new CreateTransferRecordRequest();
        OffsetDateTime now = OffsetDateTime.now();
        transferReqDto.setTargetHospital("BV 1");
        transferReqDto.setReasonCategory("QUA_TAI");
        transferReqDto.setTransferReason("Lý do");
        transferReqDto.setCurrentCondition("Tình trạng");
        transferReqDto.setFirstAidNotes("Sơ cứu");
        transferReqDto.setRefusalNotes("Ghi chú");
        transferReqDto.setTransferredAt(now);
        assertThat(transferReqDto.getTargetHospital()).isEqualTo("BV 1");
        assertThat(transferReqDto.getReasonCategory()).isEqualTo("QUA_TAI");
        assertThat(transferReqDto.getTransferReason()).isEqualTo("Lý do");
        assertThat(transferReqDto.getCurrentCondition()).isEqualTo("Tình trạng");
        assertThat(transferReqDto.getFirstAidNotes()).isEqualTo("Sơ cứu");
        assertThat(transferReqDto.getRefusalNotes()).isEqualTo("Ghi chú");
        assertThat(transferReqDto.getTransferredAt()).isEqualTo(now);

        UpdateEmergencyProgressRequest progressReqDto = new UpdateEmergencyProgressRequest();
        progressReqDto.setCurrentStep("SO_CUU");
        progressReqDto.setTreatmentSuggestions("Sơ cứu");
        progressReqDto.setDiagnosisNotes("Chẩn đoán");
        assertThat(progressReqDto.getCurrentStep()).isEqualTo("SO_CUU");
        assertThat(progressReqDto.getTreatmentSuggestions()).isEqualTo("Sơ cứu");
        assertThat(progressReqDto.getDiagnosisNotes()).isEqualTo("Chẩn đoán");
    }

    @Test
    @DisplayName("Kiểm tra cập nhật phiếu khám cấp cứu (updateCheckup), bộ lọc danh sách ca cấp cứu & các nhánh phụ")
    void emergencyCheckup_UpdateAndFilterBranches() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(studentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(student));
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Tạo phiếu cấp cứu truyền sẵn currentStep = KET_THUC
        CreateMedicalCheckupRequest createDoneReq = CreateMedicalCheckupRequest.builder()
                .studentId(10L)
                .isEmergency(true)
                .currentStep("KET_THUC")
                .symptoms("Sốt cao")
                .diagnosisNotes("Sốt siêu vi")
                .build();
        MedicalCheckupDetailResponse createdDone = medicalCheckupService.createCheckup(createDoneReq, "bs001");
        assertThat(createdDone.getStatus()).isEqualTo("COMPLETED");
        assertThat(createdDone.getCurrentStep()).isEqualTo("KET_THUC");

        // 2. Cập nhật phiếu khám cấp cứu (updateCheckup với isEmergency = true)
        draftCheckup.setAppointment(null);
        draftCheckup.setIsEmergency(true);
        draftCheckup.setCurrentStep(null);
        when(medicalCheckupRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(draftCheckup));

        UpdateMedicalCheckupRequest updateEmReq = UpdateMedicalCheckupRequest.builder()
                .isEmergency(true)
                .emergencyLevel("CAP_CUU")
                .emergencyType("Ngộ độc thực phẩm")
                .reporterContact("Quản lý KTX")
                .currentStep("CHUYEN_VIEN")
                .symptoms("Đau bụng, nôn ói")
                .diagnosisNotes("Theo dõi ngộ độc")
                .treatmentSuggestions("Bù dịch")
                .status("DRAFT")
                .build();
        MedicalCheckupDetailResponse updatedEm = medicalCheckupService.updateCheckup(1L, updateEmReq, "bs001");
        assertThat(updatedEm.getEmergencyLevel()).isEqualTo("CAP_CUU");
        assertThat(updatedEm.getEmergencyType()).isEqualTo("Ngộ độc thực phẩm");
        assertThat(updatedEm.getReporterContact()).isEqualTo("Quản lý KTX");
        assertThat(updatedEm.getCurrentStep()).isEqualTo("CHUYEN_VIEN");

        // Cập nhật không truyền currentStep khi currentStep trên entity đang null -> tự gán TIEP_NHAN
        draftCheckup.setCurrentStep(null);
        UpdateMedicalCheckupRequest updateDefaultStep = UpdateMedicalCheckupRequest.builder()
                .symptoms("Đau đầu")
                .diagnosisNotes("Theo dõi")
                .status("COMPLETED")
                .build();
        MedicalCheckupDetailResponse completedEm = medicalCheckupService.updateCheckup(1L, updateDefaultStep, "bs001");
        assertThat(completedEm.getCurrentStep()).isEqualTo("KET_THUC");
        assertThat(completedEm.getStatus()).isEqualTo("COMPLETED");

        // 3. Lập hồ sơ chuyển viện cho ca khám thường (không phải cấp cứu, đã có sẵn symptoms/diagnosis/treatment, không có appointment)
        draftCheckup.setStatus("DRAFT");
        draftCheckup.setIsEmergency(false);
        draftCheckup.setSymptoms("Đã có triệu chứng");
        draftCheckup.setDiagnosisNotes("Đã có chẩn đoán");
        draftCheckup.setTreatmentSuggestions("Đã có hướng xử trí");
        when(transferRecordRepository.existsByMedicalCheckupId(1L)).thenReturn(false);
        when(transferRecordRepository.save(any(TransferRecord.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateTransferRecordRequest transferOverload = new CreateTransferRecordRequest(
                "BV Đa khoa", "OVERLOAD", "Quá tải", "Ổn định", "Sơ cứu", null, null
        );
        MedicalCheckupDetailResponse transferredNormal = medicalCheckupService.createTransferRecord(1L, transferOverload, "bs001");
        assertThat(transferredNormal.getTransferRecord().getReasonCategory()).isEqualTo("QUA_TAI");

        // 4. getEmergencyCases với các nhánh bộ lọc null, ALL, tìm theo checkupCode, tìm theo fullName và không khớp
        MedicalCheckup c1 = MedicalCheckup.builder()
                .id(601L)
                .checkupCode("PK2611180601")
                .student(student)
                .staff(doctorStaff)
                .isEmergency(true)
                .emergencyLevel("CAP_CUU")
                .emergencyType("Sốt cao")
                .currentStep("TIEP_NHAN")
                .status("DRAFT")
                .createdAt(OffsetDateTime.now())
                .build();
        when(medicalCheckupRepository.findEmergencyCasesWithDetails()).thenReturn(List.of(c1));

        // Bộ lọc hoàn toàn null & page/size âm
        EmergencyCasePageResponse allPage = medicalCheckupService.getEmergencyCases(
                null, "ALL", null, null, null, null, -1, 0, "bs001"
        );
        assertThat(allPage.getTotalElements()).isEqualTo(1L);

        // Tìm theo checkupCode
        assertThat(medicalCheckupService.getEmergencyCases(
                "PK2611180601", null, null, "INTAKE", null, null, null, null, "bs001"
        ).getTotalElements()).isEqualTo(1L);

        // Tìm theo họ tên sinh viên
        assertThat(medicalCheckupService.getEmergencyCases(
                "Nguyễn Văn Sinh Viên", null, null, null, null, null, 0, 5, "bs001"
        ).getTotalElements()).isEqualTo(1L);

        // Tìm từ khóa không khớp & các bộ lọc không khớp
        assertThat(medicalCheckupService.getEmergencyCases(
                "KHONG_TON_TAI", null, null, null, null, null, 0, 5, "bs001"
        ).getTotalElements()).isZero();
        assertThat(medicalCheckupService.getEmergencyCases(
                null, "THAP", null, null, null, null, 0, 5, "bs001"
        ).getTotalElements()).isZero();
        assertThat(medicalCheckupService.getEmergencyCases(
                null, null, "Dị ứng", null, null, null, 0, 5, "bs001"
        ).getTotalElements()).isZero();
        assertThat(medicalCheckupService.getEmergencyCases(
                null, null, null, "SO_CUU", null, null, 0, 5, "bs001"
        ).getTotalElements()).isZero();
        assertThat(medicalCheckupService.getEmergencyCases(
                null, null, null, null, "COMPLETED", null, 0, 5, "bs001"
        ).getTotalElements()).isZero();
        assertThat(medicalCheckupService.getEmergencyCases(
                null, null, null, null, null, LocalDate.now().minusDays(5), 0, 5, "bs001"
        ).getTotalElements()).isZero();
    }

    // =========================================================================
    // 6. Tests for SP5-01: Psychological Checkup & Periodic Health Checkup
    // =========================================================================

    @Test
    @DisplayName("SP5-01 Tư vấn tâm lý: Tạo nháp, cập nhật 3 trường chuyên môn, xóa nhóm bệnh dịch tễ và hoàn tất thành công")
    void psychologicalCheckup_CreateDraftUpdateAndComplete_Success() {
        Appointment psychAppt = Appointment.builder()
                .id(201L)
                .appointmentCode("LH2611210001")
                .student(student)
                .shift(psychologicalShift)
                .reasonSymptoms("Căng thẳng, mất ngủ trước kỳ thi")
                .status("CONFIRMED")
                .build();

        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(appointmentRepository.findByIdWithDetails(201L)).thenReturn(Optional.of(psychAppt));
        when(medicalCheckupRepository.findByAppointmentIdWithDetails(201L)).thenReturn(Optional.empty());
        when(medicalCheckupRepository.existsByCheckupCode(anyString())).thenReturn(false);
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(inv -> {
            MedicalCheckup mc = inv.getArgument(0);
            if (mc.getId() == null) {
                mc.setId(701L);
            }
            return mc;
        });

        // 1. Tạo nháp từ lịch hẹn Tư vấn tâm lý (chỉ cần biểu hiện & đề xuất giải tỏa, chưa cần tóm tắt tư vấn)
        CreateMedicalCheckupRequest createDraftReq = CreateMedicalCheckupRequest.builder()
                .appointmentId(201L)
                .checkupType("TU_VAN_TAM_LY")
                .symptoms("Mất ngủ 2 tuần, lo âu, giảm tập trung")
                .treatmentSuggestions("Hướng dẫn hít thở sâu 4-7-8")
                .status("DRAFT")
                .build();

        MedicalCheckupDetailResponse draftRes = medicalCheckupService.createCheckup(createDraftReq, "bs001");
        assertThat(draftRes.getId()).isEqualTo(701L);
        assertThat(draftRes.getCheckupType()).isEqualTo("PSYCHOLOGICAL");
        assertThat(draftRes.getCheckupTypeLabel()).isEqualTo("Tư vấn tâm lý");
        assertThat(draftRes.getTimelineCategory()).isEqualTo("PSYCHOLOGICAL");
        assertThat(draftRes.getIsPeriodic()).isFalse();
        assertThat(draftRes.getPeriodicResultLabel()).isNull();
        assertThat(draftRes.getDiseaseGroupId()).isNull();
        assertThat(draftRes.getSymptoms()).isEqualTo("Mất ngủ 2 tuần, lo âu, giảm tập trung");
        assertThat(draftRes.getTreatmentSuggestions()).isEqualTo("Hướng dẫn hít thở sâu 4-7-8");
        assertThat(draftRes.getStatus()).isEqualTo("DRAFT");
        assertThat(psychAppt.getStatus()).isEqualTo("CONFIRMED");

        // 2. Cập nhật và hoàn tất qua PUT /api/medical-records/{id}
        MedicalCheckup existingPsychEntity = MedicalCheckup.builder()
                .id(701L)
                .checkupCode(draftRes.getCheckupCode())
                .student(student)
                .staff(doctorStaff)
                .appointment(psychAppt)
                .checkupType("GENERAL")
                .diseaseGroup(fluDiseaseGroup)
                .isEmergency(false)
                .status("DRAFT")
                .createdAt(OffsetDateTime.now())
                .build();
        when(medicalCheckupRepository.findByIdWithDetails(701L)).thenReturn(Optional.of(existingPsychEntity));

        UpdateMedicalCheckupRequest completeReq = UpdateMedicalCheckupRequest.builder()
                .checkupType("PSYCHOLOGICAL")
                .symptoms("Lo âu kéo dài, hay giật mình khi ngủ")
                .diagnosisNotes("Áp lực học tập mức độ vừa, chưa có dấu hiệu trầm cảm nặng")
                .treatmentSuggestions("Chia nhỏ thời gian ôn tập, tập thể dục 30 phút/ngày, hẹn tái tư vấn sau 2 tuần")
                .status("COMPLETED")
                .build();

        MedicalCheckupDetailResponse completedRes = medicalCheckupService.updateCheckup(701L, completeReq, "bs001");
        assertThat(completedRes.getStatus()).isEqualTo("COMPLETED");
        assertThat(completedRes.getCheckupType()).isEqualTo("PSYCHOLOGICAL");
        assertThat(completedRes.getCheckupTypeLabel()).isEqualTo("Tư vấn tâm lý");
        assertThat(completedRes.getDiseaseGroupId()).isNull();
        assertThat(completedRes.getSymptoms()).isEqualTo("Lo âu kéo dài, hay giật mình khi ngủ");
        assertThat(completedRes.getDiagnosisNotes()).isEqualTo("Áp lực học tập mức độ vừa, chưa có dấu hiệu trầm cảm nặng");
        assertThat(completedRes.getTreatmentSuggestions()).contains("hẹn tái tư vấn sau 2 tuần");
        assertThat(completedRes.getCompletedAt()).isNotNull();
        assertThat(psychAppt.getStatus()).isEqualTo("COMPLETED");
        verify(appointmentRepository).save(psychAppt);
    }

    @Test
    @DisplayName("SP5-01 Tư vấn tâm lý: Chặn hoàn tất (COMPLETED) khi thiếu Tóm tắt buổi tư vấn trên createCheckup, updateCheckup và completeCheckup")
    void psychologicalCheckup_RequireDiagnosisNotesBeforeCompletion() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(studentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(student));

        // 1. Qua createCheckup
        CreateMedicalCheckupRequest createWithoutSummary = CreateMedicalCheckupRequest.builder()
                .studentId(10L)
                .isEmergency(false)
                .checkupType("PSYCHOLOGICAL")
                .symptoms("Lo âu nhẹ")
                .treatmentSuggestions("Nghe nhạc thư giãn")
                .diagnosisNotes("   ")
                .status("COMPLETED")
                .build();

        assertThatThrownBy(() -> medicalCheckupService.createCheckup(createWithoutSummary, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("tóm tắt buổi tư vấn");

        // 2. Qua updateCheckup
        MedicalCheckup draftPsych = MedicalCheckup.builder()
                .id(702L)
                .checkupCode("PK2611210702")
                .student(student)
                .staff(doctorStaff)
                .checkupType("PSYCHOLOGICAL")
                .isEmergency(false)
                .symptoms("Mất ngủ")
                .treatmentSuggestions("Thư giãn")
                .status("DRAFT")
                .createdAt(OffsetDateTime.now())
                .build();
        when(medicalCheckupRepository.findByIdWithDetails(702L)).thenReturn(Optional.of(draftPsych));

        UpdateMedicalCheckupRequest updateWithoutSummary = UpdateMedicalCheckupRequest.builder()
                .checkupType("PSYCHOLOGICAL")
                .symptoms("Mất ngủ")
                .diagnosisNotes(null)
                .status("COMPLETED")
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updateCheckup(702L, updateWithoutSummary, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("tóm tắt buổi tư vấn");

        // 3. Qua completeCheckup
        assertThatThrownBy(() -> medicalCheckupService.completeCheckup(702L, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("tóm tắt buổi tư vấn");

        verify(medicalCheckupRepository, never()).save(any());
    }

    @Test
    @DisplayName("SP5-01 Khám sức khỏe định kỳ: Hoàn tất với PASSED / FAILED (và alias DAT / KHONG_DAT, KHAM_DINH_KY) và ánh xạ SummaryResponse")
    void periodicCheckup_PassedAndFailed_SuccessAndResponseMapping() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(studentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(student));
        when(medicalCheckupRepository.existsByCheckupCode(anyString())).thenReturn(false);
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(inv -> {
            MedicalCheckup mc = inv.getArgument(0);
            if (mc.getId() == null) {
                mc.setId(703L);
            }
            return mc;
        });

        // 1. Tạo & hoàn tất Khám định kỳ với KHAM_DINH_KY + DAT -> chuẩn hóa về checkupType=GENERAL, periodicResult=PASSED
        CreateMedicalCheckupRequest passedReq = CreateMedicalCheckupRequest.builder()
                .studentId(10L)
                .isEmergency(false)
                .checkupType("KHAM_DINH_KY")
                .isPeriodic(true)
                .periodicResult("DAT")
                .symptoms("Khám tổng quát đầu khóa: mạch 76 lần/phút, huyết áp 115/75 mmHg")
                .diagnosisNotes("Thể lực tốt, đủ điều kiện học tập")
                .status("COMPLETED")
                .build();

        MedicalCheckupDetailResponse passedRes = medicalCheckupService.createCheckup(passedReq, "bs001");
        assertThat(passedRes.getCheckupType()).isEqualTo("GENERAL");
        assertThat(passedRes.getCheckupTypeLabel()).isEqualTo("Khám sức khỏe định kỳ");
        assertThat(passedRes.getPeriodicResult()).isEqualTo("PASSED");
        assertThat(passedRes.getPeriodicResultLabel()).isEqualTo("Đạt");
        assertThat(passedRes.getIsPeriodic()).isTrue();
        assertThat(passedRes.getTimelineCategory()).isEqualTo("PERIODIC");
        assertThat(passedRes.getStatus()).isEqualTo("COMPLETED");

        // 2. Tạo nháp rồi cập nhật FAILED qua alias KHONG_DAT
        CreateMedicalCheckupRequest draftPeriodicReq = CreateMedicalCheckupRequest.builder()
                .studentId(10L)
                .isEmergency(false)
                .checkupType("PERIODIC")
                .isPeriodic(true)
                .symptoms("Huyết áp cao 155/95 mmHg")
                .status("DRAFT")
                .build();

        MedicalCheckupDetailResponse draftRes = medicalCheckupService.createCheckup(draftPeriodicReq, "bs001");
        assertThat(draftRes.getCheckupType()).isEqualTo("GENERAL");
        assertThat(draftRes.getPeriodicResult()).isNull();
        assertThat(draftRes.getStatus()).isEqualTo("DRAFT");

        MedicalCheckup draftPeriodicEntity = MedicalCheckup.builder()
                .id(704L)
                .checkupCode("PK2611210704")
                .student(student)
                .staff(doctorStaff)
                .checkupType("GENERAL")
                .isEmergency(false)
                .status("DRAFT")
                .createdAt(OffsetDateTime.now())
                .build();
        when(medicalCheckupRepository.findByIdWithDetails(704L)).thenReturn(Optional.of(draftPeriodicEntity));

        UpdateMedicalCheckupRequest failedUpdateReq = UpdateMedicalCheckupRequest.builder()
                .isPeriodic(true)
                .periodicResult("KHONG_DAT")
                .symptoms("Huyết áp 155/95 mmHg, nhịp tim nhanh")
                .diagnosisNotes("Không đạt tiêu chuẩn sức khỏe định kỳ, cần khám chuyên khoa tim mạch")
                .status("COMPLETED")
                .build();

        MedicalCheckupDetailResponse failedRes = medicalCheckupService.updateCheckup(704L, failedUpdateReq, "bs001");
        assertThat(failedRes.getPeriodicResult()).isEqualTo("FAILED");
        assertThat(failedRes.getPeriodicResultLabel()).isEqualTo("Không đạt");
        assertThat(failedRes.getIsPeriodic()).isTrue();
        assertThat(failedRes.getTimelineCategory()).isEqualTo("PERIODIC");
        assertThat(failedRes.getCheckupTypeLabel()).isEqualTo("Khám sức khỏe định kỳ");

        // 3. Kiểm tra MedicalCheckupSummaryResponse đồng nhất isPeriodic & timelineCategory
        MedicalCheckupSummaryResponse summary = MedicalCheckupSummaryResponse.fromEntity(draftPeriodicEntity);
        assertThat(summary.getPeriodicResult()).isEqualTo("FAILED");
        assertThat(summary.getIsPeriodic()).isTrue();
        assertThat(summary.getTimelineCategory()).isEqualTo("PERIODIC");
        assertThat(MedicalCheckupDetailResponse.resolveCheckupTypeLabel("GENERAL", false)).isEqualTo("Khám đa khoa");
        assertThat(MedicalCheckupDetailResponse.resolvePeriodicResultLabel("UNKNOWN")).isNull();
    }

    @Test
    @DisplayName("SP5-01 Khám sức khỏe định kỳ: Chặn hoàn tất nếu bật cờ định kỳ nhưng thiếu periodicResult, triệu chứng hoặc kết luận")
    void periodicCheckup_RequirePeriodicResultSymptomsAndDiagnosisBeforeCompletion() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(studentRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(student));

        // 1. isPeriodic = true nhưng periodicResult = null khi tạo COMPLETED
        CreateMedicalCheckupRequest missingPeriodicResultCreate = CreateMedicalCheckupRequest.builder()
                .studentId(10L)
                .isEmergency(false)
                .isPeriodic(true)
                .symptoms("Khám định kỳ")
                .diagnosisNotes("Đủ điều kiện")
                .status("COMPLETED")
                .build();

        assertThatThrownBy(() -> medicalCheckupService.createCheckup(missingPeriodicResultCreate, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("kết quả khám sức khỏe định kỳ (PASSED / FAILED)");

        // 2. checkupType = PERIODIC nhưng periodicResult = null khi update COMPLETED
        MedicalCheckup draftGeneral = MedicalCheckup.builder()
                .id(705L)
                .checkupCode("PK2611210705")
                .student(student)
                .staff(doctorStaff)
                .checkupType("GENERAL")
                .isEmergency(false)
                .status("DRAFT")
                .createdAt(OffsetDateTime.now())
                .build();
        when(medicalCheckupRepository.findByIdWithDetails(705L)).thenReturn(Optional.of(draftGeneral));

        UpdateMedicalCheckupRequest missingPeriodicResultUpdate = UpdateMedicalCheckupRequest.builder()
                .checkupType("PERIODIC")
                .symptoms("Khám định kỳ")
                .diagnosisNotes("Sức khỏe bình thường")
                .status("COMPLETED")
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updateCheckup(705L, missingPeriodicResultUpdate, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("kết quả khám sức khỏe định kỳ (PASSED / FAILED)");

        // 3. Có periodicResult = PASSED nhưng thiếu symptoms -> chặn
        UpdateMedicalCheckupRequest missingSymptomsUpdate = UpdateMedicalCheckupRequest.builder()
                .isPeriodic(true)
                .periodicResult("PASSED")
                .symptoms("   ")
                .diagnosisNotes("Sức khỏe loại 1")
                .status("COMPLETED")
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updateCheckup(705L, missingSymptomsUpdate, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("triệu chứng (symptoms)");

        // 4. Có periodicResult = FAILED và symptoms nhưng thiếu diagnosisNotes -> chặn
        UpdateMedicalCheckupRequest missingDiagnosisUpdate = UpdateMedicalCheckupRequest.builder()
                .isPeriodic(true)
                .periodicResult("FAILED")
                .symptoms("Thị lực giảm")
                .diagnosisNotes("")
                .status("COMPLETED")
                .build();

        assertThatThrownBy(() -> medicalCheckupService.updateCheckup(705L, missingDiagnosisUpdate, "bs001"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("kết luận chẩn đoán (diagnosisNotes)");
    }

    @Test
    @DisplayName("SP5-01 Khám sức khỏe định kỳ: Cho phép xóa cờ periodicResult khi cập nhật phiếu nháp với isPeriodic = false hoặc periodicResult rỗng/NONE")
    void periodicCheckup_ClearPeriodicResultOnDraftUpdate() {
        when(userRepository.findByUsername("bs001")).thenReturn(Optional.of(doctorUser));
        when(medicalCheckupRepository.save(any(MedicalCheckup.class))).thenAnswer(inv -> inv.getArgument(0));

        MedicalCheckup draftWithPeriodic = MedicalCheckup.builder()
                .id(706L)
                .checkupCode("PK2611210706")
                .student(student)
                .staff(doctorStaff)
                .checkupType("GENERAL")
                .periodicResult("PASSED")
                .isEmergency(false)
                .symptoms("Khám sức khỏe")
                .status("DRAFT")
                .createdAt(OffsetDateTime.now())
                .build();
        when(medicalCheckupRepository.findByIdWithDetails(706L)).thenReturn(Optional.of(draftWithPeriodic));

        // 1. Tắt cờ isPeriodic = false -> xóa periodicResult
        UpdateMedicalCheckupRequest turnOffPeriodicReq = UpdateMedicalCheckupRequest.builder()
                .isPeriodic(false)
                .symptoms("Đau họng")
                .status("DRAFT")
                .build();

        MedicalCheckupDetailResponse afterTurnOff = medicalCheckupService.updateCheckup(706L, turnOffPeriodicReq, "bs001");
        assertThat(afterTurnOff.getPeriodicResult()).isNull();
        assertThat(afterTurnOff.getIsPeriodic()).isFalse();
        assertThat(afterTurnOff.getTimelineCategory()).isEqualTo("GENERAL");

        // 2. Gán lại PASSED rồi xóa bằng periodicResult = "" và "NONE"
        draftWithPeriodic.setPeriodicResult("PASSED");
        UpdateMedicalCheckupRequest emptyPeriodicReq = UpdateMedicalCheckupRequest.builder()
                .periodicResult("")
                .symptoms("Đau họng")
                .status("DRAFT")
                .build();
        assertThat(medicalCheckupService.updateCheckup(706L, emptyPeriodicReq, "bs001").getPeriodicResult()).isNull();

        draftWithPeriodic.setPeriodicResult("FAILED");
        UpdateMedicalCheckupRequest nonePeriodicReq = UpdateMedicalCheckupRequest.builder()
                .periodicResult("NONE")
                .symptoms("Đau họng")
                .status("DRAFT")
                .build();
        assertThat(medicalCheckupService.updateCheckup(706L, nonePeriodicReq, "bs001").getPeriodicResult()).isNull();
    }
}
