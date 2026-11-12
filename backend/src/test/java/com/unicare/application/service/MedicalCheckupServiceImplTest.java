package com.unicare.application.service;

import com.unicare.application.dto.request.CreateMedicalCheckupRequest;
import com.unicare.application.dto.request.PrescribedMedicineRequest;
import com.unicare.application.dto.request.UpdateMedicalCheckupRequest;
import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.dto.response.PrescribedMedicineResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
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
import com.unicare.domain.repository.MedicalCheckupRepository;
import com.unicare.domain.repository.MedicalStaffRepository;
import com.unicare.domain.repository.StudentRepository;
import com.unicare.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

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
                diseaseGroupRepository
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
}
