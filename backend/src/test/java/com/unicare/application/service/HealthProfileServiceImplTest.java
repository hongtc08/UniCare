/**
 * Unit test kiểm thử logic tính BMI, phân loại BMI, thông tin cá nhân sinh viên, validate và phân quyền trong HealthProfileServiceImpl.
 */
package com.unicare.application.service;

import com.unicare.application.dto.request.HealthProfileRequest;
import com.unicare.application.dto.request.StudentPersonalInfoRequest;
import com.unicare.application.dto.response.HealthProfileResponse;
import com.unicare.application.dto.response.MedicalCheckupSummaryResponse;
import com.unicare.application.dto.response.StudentMedicalOverviewResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.service.impl.HealthProfileServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.AcademicClass;
import com.unicare.domain.entity.Appointment;
import com.unicare.domain.entity.DiseaseGroup;
import com.unicare.domain.entity.Faculty;
import com.unicare.domain.entity.HealthInsurance;
import com.unicare.domain.entity.HealthRecord;
import com.unicare.domain.entity.MedicalCheckup;
import com.unicare.domain.entity.MedicalStaff;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.BloodType;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;
import com.unicare.domain.repository.AcademicClassRepository;
import com.unicare.domain.repository.HealthInsuranceRepository;
import com.unicare.domain.repository.HealthRecordRepository;
import com.unicare.domain.repository.MedicalCheckupRepository;
import com.unicare.domain.repository.StudentRepository;
import com.unicare.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthProfileServiceImplTest {

    @Mock
    private HealthRecordRepository healthRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AcademicClassRepository academicClassRepository;

    @Mock
    private HealthInsuranceRepository healthInsuranceRepository;

    @Mock
    private MedicalCheckupRepository medicalCheckupRepository;

    @InjectMocks
    private HealthProfileServiceImpl healthProfileService;

    private User studentUser;
    private User otherStudentUser;
    private User doctorUser;
    private User adminUser;
    private Student studentEntity;
    private AcademicClass academicClass;
    private Faculty faculty;

    @BeforeEach
    void setUp() {
        studentUser = new User("24520579", "hashed_pwd", "Tăng Chấn Hồng", Role.SINH_VIEN, UserStatus.ACTIVE);
        studentUser.setId(2L);
        studentUser.setEmail("24520579@gm.uit.edu.vn");
        studentUser.setPhoneNumber("0901234567");

        faculty = new Faculty(1, "CNPM", "Công nghệ Phần mềm");
        academicClass = new AcademicClass(10, "SE104.O11", "Kỹ thuật phần mềm K19", "K19", faculty);

        studentEntity = Student.builder()
                .id(2L)
                .user(studentUser)
                .studentCode("24520579")
                .academicClass(academicClass)
                .dateOfBirth(LocalDate.of(2006, 5, 15))
                .gender("Nam")
                .build();

        otherStudentUser = new User("24521234", "hashed_pwd", "Sinh Viên Khác", Role.SINH_VIEN, UserStatus.ACTIVE);
        otherStudentUser.setId(5L);

        doctorUser = new User("NV001", "hashed_pwd", "Bác Sĩ A", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        doctorUser.setId(3L);

        adminUser = new User("admin", "hashed_pwd", "Quản Trị Viên", Role.BAN_QUAN_LY, UserStatus.ACTIVE);
        adminUser.setId(1L);

        lenient().when(studentRepository.findById(2L)).thenReturn(Optional.of(studentEntity));
        lenient().when(studentRepository.findByIdWithDetails(2L)).thenReturn(Optional.of(studentEntity));
        lenient().when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("GET & PUT thông tin cá nhân sinh viên (Họ tên, Ngày sinh, Giới tính, SĐT, Email, Lớp, Khoa) thành công")
    void getAndUpdateStudentPersonalInfo_Success() {
        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));

        StudentPersonalInfoResponse info = healthProfileService.getStudentPersonalInfo("24520579", null);
        assertThat(info).isNotNull();
        assertThat(info.getStudentId()).isEqualTo(2L);
        assertThat(info.getUsername()).isEqualTo("24520579");
        assertThat(info.getStudentCode()).isEqualTo("24520579");
        assertThat(info.getFullName()).isEqualTo("Tăng Chấn Hồng");
        assertThat(info.getDateOfBirth()).isEqualTo(LocalDate.of(2006, 5, 15));
        assertThat(info.getGender()).isEqualTo("Nam");
        assertThat(info.getPhoneNumber()).isEqualTo("0901234567");
        assertThat(info.getPhone()).isEqualTo("0901234567");
        assertThat(info.getEmail()).isEqualTo("24520579@gm.uit.edu.vn");
        assertThat(info.getClassId()).isEqualTo(10);
        assertThat(info.getClassCode()).isEqualTo("SE104.O11");
        assertThat(info.getClassName()).isEqualTo("Kỹ thuật phần mềm K19");
        assertThat(info.getAcademicYear()).isEqualTo("K19");
        assertThat(info.getFacultyId()).isEqualTo(1);
        assertThat(info.getFacultyCode()).isEqualTo("CNPM");
        assertThat(info.getFacultyName()).isEqualTo("Công nghệ Phần mềm");

        when(academicClassRepository.findById(10)).thenReturn(Optional.of(academicClass));

        StudentPersonalInfoRequest updateReq = new StudentPersonalInfoRequest();
        updateReq.setFullName("Tăng Chấn Hồng Mới");
        updateReq.setDateOfBirth(LocalDate.of(2005, 8, 20));
        updateReq.setGender("Nam");
        updateReq.setPhoneNumber("0987654321");
        updateReq.setEmail("hong.new@gm.uit.edu.vn");
        updateReq.setClassId(10);
        updateReq.setClassCode("SE104.O11");

        StudentPersonalInfoResponse updated = healthProfileService.updateStudentPersonalInfo("24520579", null, updateReq);
        assertThat(updated.getFullName()).isEqualTo("Tăng Chấn Hồng Mới");
        assertThat(updated.getDateOfBirth()).isEqualTo(LocalDate.of(2005, 8, 20));
        assertThat(updated.getGender()).isEqualTo("Nam");
        assertThat(updated.getPhoneNumber()).isEqualTo("0987654321");
        assertThat(updated.getEmail()).isEqualTo("hong.new@gm.uit.edu.vn");
    }

    @Test
    @DisplayName("Cập nhật thông tin cá nhân với chuỗi rỗng để xóa email/phone/gender, tìm lớp theo classCode và khởi tạo Student mới")
    void updateStudentPersonalInfo_BlankFieldsAndClassCodeAndNewStudent() {
        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByIdWithDetails(2L)).thenReturn(Optional.empty());
        when(studentRepository.findById(2L)).thenReturn(Optional.empty());
        when(academicClassRepository.findByClassCodeIgnoreCase("SE104.O11")).thenReturn(Optional.of(academicClass));

        StudentPersonalInfoRequest clearReq = new StudentPersonalInfoRequest(
                null,
                null,
                "   ",
                "   ",
                "   ",
                null,
                "SE104.O11"
        );

        StudentPersonalInfoResponse res = healthProfileService.updateStudentPersonalInfo("24520579", null, clearReq);
        assertThat(res.getEmail()).isNull();
        assertThat(res.getPhoneNumber()).isNull();
        assertThat(res.getGender()).isNull();
        assertThat(res.getClassCode()).isEqualTo("SE104.O11");

        // Test 3-arg constructor (academicClassRepository == null)
        HealthProfileServiceImpl threeArgService = new HealthProfileServiceImpl(
                healthRecordRepository, userRepository, studentRepository
        );
        StudentPersonalInfoResponse resNoClassRepo = threeArgService.updateStudentPersonalInfo(
                "24520579", null, new StudentPersonalInfoRequest("Tên Mới", null, null, null, null, 10, "SE104")
        );
        assertThat(resNoClassRepo.getFullName()).isEqualTo("Tên Mới");
    }

    @Test
    @DisplayName("Validate cập nhật thông tin cá nhân: đầy đủ các nhánh lỗi BadRequest, DuplicateResource và ResourceNotFound")
    void updateStudentPersonalInfo_ValidationErrors() {
        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("dup@gm.uit.edu.vn", 2L)).thenReturn(true);

        assertThatThrownBy(() -> healthProfileService.updateStudentPersonalInfo("24520579", null, null))
                .isInstanceOf(BadRequestException.class);

        StudentPersonalInfoRequest blankNameReq = new StudentPersonalInfoRequest(
                "   ", null, null, null, null, null, null
        );
        assertThatThrownBy(() -> healthProfileService.updateStudentPersonalInfo("24520579", null, blankNameReq))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Họ và tên không được để trống");

        StudentPersonalInfoRequest longNameReq = new StudentPersonalInfoRequest(
                "A".repeat(101), null, null, null, null, null, null
        );
        assertThatThrownBy(() -> healthProfileService.updateStudentPersonalInfo("24520579", null, longNameReq))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("100 ký tự");

        StudentPersonalInfoRequest invalidEmailReq = new StudentPersonalInfoRequest(
                null, null, null, null, "invalid-email-no-at", null, null
        );
        assertThatThrownBy(() -> healthProfileService.updateStudentPersonalInfo("24520579", null, invalidEmailReq))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Định dạng email không hợp lệ");

        StudentPersonalInfoRequest longEmailReq = new StudentPersonalInfoRequest(
                null, null, null, null, "a".repeat(95) + "@uit.edu.vn", null, null
        );
        assertThatThrownBy(() -> healthProfileService.updateStudentPersonalInfo("24520579", null, longEmailReq))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Định dạng email không hợp lệ");

        StudentPersonalInfoRequest dupEmailReq = new StudentPersonalInfoRequest(
                "Tăng Chấn Hồng", null, "Nam", "0901234567", "dup@gm.uit.edu.vn", null, null
        );
        assertThatThrownBy(() -> healthProfileService.updateStudentPersonalInfo("24520579", null, dupEmailReq))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã được sử dụng");

        StudentPersonalInfoRequest longPhoneReq = new StudentPersonalInfoRequest(
                null, null, null, "01234567890123456", null, null, null
        );
        assertThatThrownBy(() -> healthProfileService.updateStudentPersonalInfo("24520579", null, longPhoneReq))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("15 ký tự");

        StudentPersonalInfoRequest futureDobReq = new StudentPersonalInfoRequest(
                "Tăng Chấn Hồng", LocalDate.now().plusDays(5), "Nam", "0901234567", null, null, null
        );
        assertThatThrownBy(() -> healthProfileService.updateStudentPersonalInfo("24520579", null, futureDobReq))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ngày sinh không được lớn hơn ngày hiện tại");

        StudentPersonalInfoRequest longGenderReq = new StudentPersonalInfoRequest(
                null, null, "Giới tính quá dài", null, null, null, null
        );
        assertThatThrownBy(() -> healthProfileService.updateStudentPersonalInfo("24520579", null, longGenderReq))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("10 ký tự");

        when(academicClassRepository.findById(999)).thenReturn(Optional.empty());
        StudentPersonalInfoRequest missingClassIdReq = new StudentPersonalInfoRequest(
                null, null, null, null, null, 999, null
        );
        assertThatThrownBy(() -> healthProfileService.updateStudentPersonalInfo("24520579", null, missingClassIdReq))
                .isInstanceOf(ResourceNotFoundException.class);

        when(academicClassRepository.findByClassCodeIgnoreCase("UNKNOWN")).thenReturn(Optional.empty());
        StudentPersonalInfoRequest missingClassCodeReq = new StudentPersonalInfoRequest(
                null, null, null, null, null, null, "UNKNOWN"
        );
        assertThatThrownBy(() -> healthProfileService.updateStudentPersonalInfo("24520579", null, missingClassCodeReq))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Kiểm tra các nhánh mapping của StudentPersonalInfoResponse và Student entity")
    void studentPersonalInfoResponseAndEntity_EdgeBranches() {
        assertThat(StudentPersonalInfoResponse.fromEntities(null, null)).isNull();

        Student orphanStudent = new Student(null, studentUser, "   ", new AcademicClass("C1", "Lớp 1", "K19", null));
        orphanStudent.setUserId(2L);
        assertThat(orphanStudent.getUserId()).isEqualTo(2L);
        orphanStudent.setDateOfBirth(LocalDate.of(2005, 1, 1));
        orphanStudent.setGender("Nữ");

        StudentPersonalInfoResponse fromStudentOnly = StudentPersonalInfoResponse.fromEntities(null, orphanStudent);
        assertThat(fromStudentOnly).isNotNull();
        assertThat(fromStudentOnly.getStudentId()).isEqualTo(2L);
        assertThat(fromStudentOnly.getClassCode()).isEqualTo("C1");
        assertThat(fromStudentOnly.getFacultyName()).isNull();

        Student noUserAndNoId = new Student(null, null, null, null);
        StudentPersonalInfoResponse minimal = StudentPersonalInfoResponse.fromEntities(null, noUserAndNoId);
        assertThat(minimal).isNotNull();
        assertThat(minimal.getStudentId()).isNull();
    }

    @Test
    @DisplayName("Tính BMI làm tròn 1 chữ số thập phân và phân loại chuẩn WHO (Gầy, Bình thường, Thừa cân, Béo phì)")
    void calculateAndClassifyBmi_AllCategories() {
        // 1. Gầy (< 18.5): 170cm, 50kg -> 50 / (1.7^2) = 17.301... -> 17.3
        double bmiThin = healthProfileService.calculateBmi(170.0, 50.0);
        assertThat(bmiThin).isEqualTo(17.3);
        assertThat(healthProfileService.classifyBmi(bmiThin)).isEqualTo(HealthProfileServiceImpl.BMI_UNDERWEIGHT);

        // 2. Bình thường (18.5 - 24.9): 170cm, 65kg -> 65 / 2.89 = 22.491... -> 22.5
        double bmiNormal = healthProfileService.calculateBmi(170.0, 65.0);
        assertThat(bmiNormal).isEqualTo(22.5);
        assertThat(healthProfileService.classifyBmi(bmiNormal)).isEqualTo(HealthProfileServiceImpl.BMI_NORMAL);

        // 3. Thừa cân (25 <= BMI < 30): 165cm, 75kg -> 75 / (1.65^2) = 27.548... -> 27.5
        double bmiOverweight = healthProfileService.calculateBmi(165.0, 75.0);
        assertThat(bmiOverweight).isEqualTo(27.5);
        assertThat(healthProfileService.classifyBmi(bmiOverweight)).isEqualTo(HealthProfileServiceImpl.BMI_OVERWEIGHT);

        // 4. Béo phì (>= 30): 160cm, 85kg -> 85 / (1.6^2) = 33.203... -> 33.2
        double bmiObese = healthProfileService.calculateBmi(160.0, 85.0);
        assertThat(bmiObese).isEqualTo(33.2);
        assertThat(healthProfileService.classifyBmi(bmiObese)).isEqualTo(HealthProfileServiceImpl.BMI_OBESE);
    }

    @Test
    @DisplayName("Sinh viên tự xem và cập nhật thành công hồ sơ sức khỏe của chính mình (tự động khởi tạo Student nếu chưa có)")
    void upsertHealthProfile_StudentSelfCreate_Success() {
        HealthProfileRequest request = new HealthProfileRequest(
                170.0,
                65.0,
                "O",
                "Dị ứng hải sản",
                "Không có",
                "Đã tiêm 3 mũi COVID-19"
        );
        request.setMedicalHistory("Khỏe mạnh");

        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        when(studentRepository.findById(2L)).thenReturn(Optional.empty());
        when(healthRecordRepository.findByStudentId(2L)).thenReturn(Optional.empty());
        when(healthRecordRepository.save(any(HealthRecord.class))).thenAnswer(invocation -> {
            HealthRecord saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        HealthProfileResponse response = healthProfileService.upsertHealthProfile("24520579", 2L, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStudentId()).isEqualTo(2L);
        assertThat(response.getHeight()).isEqualTo(170.0);
        assertThat(response.getWeight()).isEqualTo(65.0);
        assertThat(response.getBloodType()).isEqualTo("O");
        assertThat(response.getBmi()).isEqualTo(22.5);
        assertThat(response.getBmiClassification()).isEqualTo("Bình thường");
        assertThat(response.getAllergies()).isEqualTo("Dị ứng hải sản");
        assertThat(response.getUnderlyingDiseases()).isEqualTo("Không có");
        assertThat(response.getVaccinationHistory()).isEqualTo("Đã tiêm 3 mũi COVID-19");
        assertThat(response.getMedicalHistory()).isEqualTo("Khỏe mạnh");
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("Bác sĩ xem và cập nhật hồ sơ sức khỏe của sinh viên thành công")
    void doctorGetAndUpsertStudentProfile_Success() {
        HealthRecord existing = new HealthRecord();
        existing.setId(10L);
        existing.setStudent(studentEntity);
        existing.setHeight(BigDecimal.valueOf(170.0));
        existing.setWeight(BigDecimal.valueOf(60.0));
        existing.setBloodType("AB");
        existing.setBmi(BigDecimal.valueOf(20.8));
        existing.setBmiClassification("Bình thường");

        when(userRepository.findByUsername("NV001")).thenReturn(Optional.of(doctorUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(studentUser));
        when(healthRecordRepository.findByStudentId(2L)).thenReturn(Optional.of(existing));
        when(healthRecordRepository.save(any(HealthRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        HealthProfileResponse getRes = healthProfileService.getHealthProfile("NV001", 2L);
        assertThat(getRes).isNotNull();
        assertThat(getRes.getBloodType()).isEqualTo("AB");

        HealthProfileRequest updateReq = new HealthProfileRequest(
                172.0,
                76.0,
                "AB",
                "Không",
                "Viêm xoang",
                "Viêm gan B"
        );
        HealthProfileResponse updatedRes = healthProfileService.upsertHealthProfile("NV001", 2L, updateReq);
        assertThat(updatedRes.getHeight()).isEqualTo(172.0);
        assertThat(updatedRes.getWeight()).isEqualTo(76.0);
        assertThat(updatedRes.getBmi()).isEqualTo(25.7);
        assertThat(updatedRes.getBmiClassification()).isEqualTo("Thừa cân");
    }

    @Test
    @DisplayName("Sinh viên chưa có hồ sơ khi gọi GET trả về null")
    void getHealthProfile_WhenNotYetCreated_ReturnsNull() {
        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        when(healthRecordRepository.findByStudentId(2L)).thenReturn(Optional.empty());

        HealthProfileResponse response = healthProfileService.getHealthProfile("24520579", null);
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Chặn Sinh viên xem hoặc cập nhật hồ sơ của sinh viên khác (403 Forbidden)")
    void studentAccessOtherStudentProfile_ThrowsAccessDenied() {
        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));

        assertThatThrownBy(() -> healthProfileService.getHealthProfile("24520579", 5L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("chỉ được phép xem và cập nhật hồ sơ sức khỏe của chính mình");

        HealthProfileRequest request = new HealthProfileRequest(170.0, 60.0, "A", null, null, null);
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", 5L, request))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Chặn Ban quản lý (BAN_QUAN_LY) xem hoặc cập nhật hồ sơ sức khỏe cá nhân (403 Forbidden)")
    void adminAccessProfile_ThrowsAccessDenied() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() -> healthProfileService.getHealthProfile("admin", 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ chính sinh viên đó và Bác sĩ");
    }

    @Test
    @DisplayName("Bác sĩ không truyền studentId hoặc truyền ID không phải sinh viên ném lỗi")
    void doctorInvalidTargetStudent_ThrowsException() {
        when(userRepository.findByUsername("NV001")).thenReturn(Optional.of(doctorUser));

        assertThatThrownBy(() -> healthProfileService.getHealthProfile("NV001", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("studentId");

        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        assertThatThrownBy(() -> healthProfileService.getHealthProfile("NV001", 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không phải là sinh viên");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> healthProfileService.getHealthProfile("NV001", 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Validate chặt chiều cao, cân nặng <= 0 hoặc vượt ngưỡng và nhóm máu sai")
    void validateInvalidMetricsAndBloodType_ThrowsBadRequest() {
        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));

        HealthProfileRequest zeroHeight = new HealthProfileRequest(0.0, 60.0, "A", null, null, null);
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", null, zeroHeight))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Chiều cao phải lớn hơn 0");

        HealthProfileRequest negativeWeight = new HealthProfileRequest(170.0, -5.0, "A", null, null, null);
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", null, negativeWeight))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cân nặng phải lớn hơn 0");

        HealthProfileRequest tooTall = new HealthProfileRequest(350.0, 60.0, "A", null, null, null);
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", null, tooTall))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("tối đa 300 cm");

        HealthProfileRequest tooHeavy = new HealthProfileRequest(170.0, 600.0, "A", null, null, null);
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", null, tooHeavy))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("tối đa 500 kg");

        HealthProfileRequest invalidBlood = new HealthProfileRequest(170.0, 60.0, "XYZ", null, null, null);
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", null, invalidBlood))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Nhóm máu chỉ chấp nhận");

        HealthProfileRequest blankBlood = new HealthProfileRequest(170.0, 60.0, "   ", null, null, null);
        when(healthRecordRepository.findByStudentId(2L)).thenReturn(Optional.empty());
        when(healthRecordRepository.save(any(HealthRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        HealthProfileResponse savedWithNullBlood = healthProfileService.upsertHealthProfile("24520579", null, blankBlood);
        assertThat(savedWithNullBlood.getBloodType()).isNull();

        assertThat(BloodType.isValid(null)).isFalse();
        assertThat(BloodType.isValid("  ")).isFalse();
        assertThat(BloodType.isValid("AB")).isTrue();
    }

    @Test
    @DisplayName("Kiểm tra các trường hợp biên: chưa đăng nhập hoặc request null")
    void edgeCases_NullRequestOrUnauthenticated() {
        assertThatThrownBy(() -> healthProfileService.upsertHealthProfile("24520579", null, null))
                .isInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> healthProfileService.getHealthProfile(null, null))
                .isInstanceOf(AccessDeniedException.class);

        assertThatThrownBy(() -> healthProfileService.getHealthProfile("   ", null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Bác sĩ tra cứu Hồ sơ Y tế Tổng hợp (getStudentMedicalOverview) đầy đủ 4 phần thành công")
    void getStudentMedicalOverview_FullData_Success() {
        when(userRepository.findByUsername("NV001")).thenReturn(Optional.of(doctorUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(studentUser));

        HealthRecord healthRecord = new HealthRecord();
        healthRecord.setId(10L);
        healthRecord.setStudent(studentEntity);
        healthRecord.setHeight(BigDecimal.valueOf(170.0));
        healthRecord.setWeight(BigDecimal.valueOf(65.0));
        healthRecord.setBloodType("O");
        healthRecord.setBmi(BigDecimal.valueOf(22.5));
        healthRecord.setBmiClassification("Bình thường");
        healthRecord.setAllergies("Dị ứng tôm");
        healthRecord.setUnderlyingDiseases("Viêm mũi dị ứng");
        healthRecord.setMedicalHistory("Không phẫu thuật");
        healthRecord.setVaccinationHistory("Đủ 3 mũi");
        when(healthRecordRepository.findByStudentId(2L)).thenReturn(Optional.of(healthRecord));

        HealthInsurance insurance = HealthInsurance.builder()
                .id(20L)
                .cardNumber("SV4792452057900")
                .student(studentEntity)
                .effectiveDate(LocalDate.now().minusMonths(3))
                .expirationDate(LocalDate.now().plusMonths(9))
                .initialHospital("Bệnh viện Đa khoa Khu vực Thủ Đức")
                .issueAt("BHXH TP.HCM")
                .build();
        when(healthInsuranceRepository.findByStudentIdWithDetails(2L)).thenReturn(Optional.of(insurance));

        MedicalStaff staff = MedicalStaff.builder()
                .id(3L)
                .user(doctorUser)
                .staffCode("BS001")
                .title("BAC_SI")
                .specialty("DA_KHOA")
                .build();
        DiseaseGroup dg = new DiseaseGroup(5, "NHOM_HO_HAP", "Bệnh đường hô hấp");
        Appointment appt = Appointment.builder().id(100L).appointmentCode("LH2611120001").build();

        MedicalCheckup checkup = MedicalCheckup.builder()
                .id(50L)
                .checkupCode("PK2611120001")
                .student(studentEntity)
                .staff(staff)
                .appointment(appt)
                .checkupType("KHAM_DA_KHOA")
                .isEmergency(false)
                .periodicResult("LOAI_1")
                .symptoms("Ho, đau họng")
                .diagnosisNotes("Viêm họng cấp")
                .treatmentSuggestions("Uống nhiều nước ấm, nghỉ ngơi")
                .diseaseGroup(dg)
                .status("COMPLETED")
                .createdAt(OffsetDateTime.now().minusDays(2))
                .completedAt(OffsetDateTime.now().minusDays(2))
                .build();
        when(medicalCheckupRepository.findByStudentIdWithDetailsOrderByCreatedAtDesc(2L))
                .thenReturn(List.of(checkup));

        StudentMedicalOverviewResponse overview = healthProfileService.getStudentMedicalOverview("NV001", 2L);

        assertThat(overview).isNotNull();
        assertThat(overview.getStudentInfo()).isNotNull();
        assertThat(overview.getStudentInfo().getFullName()).isEqualTo("Tăng Chấn Hồng");
        assertThat(overview.getStudentInfo().getClassCode()).isEqualTo("SE104.O11");
        assertThat(overview.getStudentInfo().getFacultyName()).isEqualTo("Công nghệ Phần mềm");

        assertThat(overview.getHealthProfile()).isNotNull();
        assertThat(overview.getHealthProfile().getBmi()).isEqualTo(22.5);
        assertThat(overview.getHealthProfile().getAllergies()).isEqualTo("Dị ứng tôm");

        assertThat(overview.getHealthInsurance()).isNotNull();
        assertThat(overview.getHealthInsurance().getCardNumber()).isEqualTo("SV4792452057900");

        assertThat(overview.getPreviousCheckups()).hasSize(1);
        assertThat(overview.getCheckupHistory()).hasSize(1);
        MedicalCheckupSummaryResponse summary = overview.getPreviousCheckups().get(0);
        assertThat(summary.getId()).isEqualTo(50L);
        assertThat(summary.getCheckupCode()).isEqualTo("PK2611120001");
        assertThat(summary.getCheckupType()).isEqualTo("KHAM_DA_KHOA");
        assertThat(summary.getIsEmergency()).isFalse();
        assertThat(summary.getPeriodicResult()).isEqualTo("LOAI_1");
        assertThat(summary.getSymptoms()).isEqualTo("Ho, đau họng");
        assertThat(summary.getDiagnosisNotes()).isEqualTo("Viêm họng cấp");
        assertThat(summary.getTreatmentSuggestions()).isEqualTo("Uống nhiều nước ấm, nghỉ ngơi");
        assertThat(summary.getDiseaseGroupId()).isEqualTo(5);
        assertThat(summary.getDiseaseGroupName()).isEqualTo("Bệnh đường hô hấp");
        assertThat(summary.getStaffId()).isEqualTo(3L);
        assertThat(summary.getStaffCode()).isEqualTo("BS001");
        assertThat(summary.getDoctorName()).isEqualTo("Bác Sĩ A");
        assertThat(summary.getDoctorTitle()).isEqualTo("BAC_SI");
        assertThat(summary.getDoctorSpecialty()).isEqualTo("DA_KHOA");
        assertThat(summary.getAppointmentId()).isEqualTo(100L);
        assertThat(summary.getAppointmentCode()).isEqualTo("LH2611120001");
        assertThat(summary.getStatus()).isEqualTo("COMPLETED");
        assertThat(summary.getCreatedAt()).isNotNull();
        assertThat(summary.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Bác sĩ tra cứu Hồ sơ Y tế Tổng hợp khi sinh viên chưa có HealthRecord/BHYT/lần khám trước")
    void getStudentMedicalOverview_WhenOptionalDataMissing_ReturnsNullSafeOverview() {
        when(userRepository.findByUsername("NV001")).thenReturn(Optional.of(doctorUser));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        when(userRepository.findByUsername("2")).thenReturn(Optional.of(studentUser));
        when(studentRepository.findByIdWithDetails(2L)).thenReturn(Optional.empty());
        when(studentRepository.findById(2L)).thenReturn(Optional.of(studentEntity));
        when(healthRecordRepository.findByStudentId(2L)).thenReturn(Optional.empty());
        when(healthInsuranceRepository.findByStudentIdWithDetails(2L)).thenReturn(Optional.empty());
        when(healthInsuranceRepository.findByStudentId(2L)).thenReturn(Optional.empty());
        when(medicalCheckupRepository.findByStudentIdWithDetailsOrderByCreatedAtDesc(2L)).thenReturn(List.of());

        StudentMedicalOverviewResponse overview = healthProfileService.getStudentMedicalOverview("NV001", 2L);
        assertThat(overview.getStudentInfo()).isNotNull();
        assertThat(overview.getHealthProfile()).isNull();
        assertThat(overview.getHealthInsurance()).isNull();
        assertThat(overview.getPreviousCheckups()).isEmpty();

        // Kiểm tra constructor 4 tham số khi healthInsuranceRepository & medicalCheckupRepository là null
        HealthProfileServiceImpl fourArgService = new HealthProfileServiceImpl(
                healthRecordRepository, userRepository, studentRepository, academicClassRepository
        );
        StudentMedicalOverviewResponse fallbackOverview = fourArgService.getStudentMedicalOverview("NV001", 2L);
        assertThat(fallbackOverview.getHealthInsurance()).isNull();
        assertThat(fallbackOverview.getPreviousCheckups()).isEmpty();
    }

    @Test
    @DisplayName("Kiểm soát bảo mật getStudentMedicalOverview: Chặn BAN_QUAN_LY, chặn SINH_VIEN và kiểm tra lỗi đầu vào")
    void getStudentMedicalOverview_SecurityAndValidationChecks() {
        // 1. Chưa đăng nhập
        assertThatThrownBy(() -> healthProfileService.getStudentMedicalOverview(null, 2L))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> healthProfileService.getStudentMedicalOverview("   ", 2L))
                .isInstanceOf(AccessDeniedException.class);

        // 2. Tài khoản đăng nhập không tồn tại
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> healthProfileService.getStudentMedicalOverview("ghost", 2L))
                .isInstanceOf(ResourceNotFoundException.class);

        // 3. Chặn Ban quản lý (BAN_QUAN_LY) truy cập bệnh án cá nhân
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        assertThatThrownBy(() -> healthProfileService.getStudentMedicalOverview("admin", 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Ban quản lý không được phép truy cập dữ liệu bệnh án cá nhân");

        // 4. Chặn Sinh viên (SINH_VIEN) gọi API dành riêng cho Bác sĩ
        when(userRepository.findByUsername("24520579")).thenReturn(Optional.of(studentUser));
        assertThatThrownBy(() -> healthProfileService.getStudentMedicalOverview("24520579", 2L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Chỉ Nhân viên y tế (Bác sĩ)");

        // 5. Bác sĩ không truyền studentId
        when(userRepository.findByUsername("NV001")).thenReturn(Optional.of(doctorUser));
        assertThatThrownBy(() -> healthProfileService.getStudentMedicalOverview("NV001", null))
                .isInstanceOf(BadRequestException.class);

        // 6. Không tìm thấy sinh viên
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        when(userRepository.findByUsername("999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> healthProfileService.getStudentMedicalOverview("NV001", 999L))
                .isInstanceOf(ResourceNotFoundException.class);

        // 7. ID truyền vào không phải tài khoản SINH_VIEN
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        assertThatThrownBy(() -> healthProfileService.getStudentMedicalOverview("NV001", 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không phải là sinh viên");
    }

    @Test
    @DisplayName("Kiểm tra các nhánh biên của MedicalCheckupSummaryResponse và StudentMedicalOverviewResponse")
    void medicalOverviewDtos_EdgeBranches() {
        assertThat(MedicalCheckupSummaryResponse.fromEntity(null)).isNull();

        MedicalStaff staffWithoutUser = MedicalStaff.builder().id(9L).staffCode("BS009").build();
        MedicalCheckup minimalCheckup = MedicalCheckup.builder()
                .id(51L)
                .checkupCode("PK001")
                .staff(staffWithoutUser)
                .build();
        MedicalCheckupSummaryResponse summaryWithStaffNoUser = MedicalCheckupSummaryResponse.fromEntity(minimalCheckup);
        assertThat(summaryWithStaffNoUser.getStaffId()).isEqualTo(9L);
        assertThat(summaryWithStaffNoUser.getDoctorName()).isNull();
        assertThat(summaryWithStaffNoUser.getDiseaseGroupId()).isNull();
        assertThat(summaryWithStaffNoUser.getAppointmentId()).isNull();

        MedicalCheckup checkupNoStaff = MedicalCheckup.builder().id(52L).build();
        MedicalCheckupSummaryResponse summaryNoStaff = MedicalCheckupSummaryResponse.fromEntity(checkupNoStaff);
        assertThat(summaryNoStaff.getStaffId()).isNull();

        StudentMedicalOverviewResponse emptyDto = new StudentMedicalOverviewResponse();
        emptyDto.setStudentInfo(new StudentPersonalInfoResponse());
        emptyDto.setHealthProfile(new HealthProfileResponse());
        emptyDto.setHealthInsurance(null);
        emptyDto.setPreviousCheckups(null);
        assertThat(emptyDto.getStudentInfo()).isNotNull();
        assertThat(emptyDto.getHealthProfile()).isNotNull();
        assertThat(emptyDto.getHealthInsurance()).isNull();
        assertThat(emptyDto.getPreviousCheckups()).isEmpty();

        StudentMedicalOverviewResponse nullListCtor = new StudentMedicalOverviewResponse(null, null, null, null);
        assertThat(nullListCtor.getPreviousCheckups()).isEmpty();
    }
}
