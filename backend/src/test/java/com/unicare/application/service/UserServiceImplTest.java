package com.unicare.application.service;

import com.unicare.application.dto.request.CreateUserRequest;
import com.unicare.application.dto.request.UpdateUserRequest;
import com.unicare.application.dto.request.UpdateUserStatusRequest;
import com.unicare.application.dto.response.UserResponse;
import com.unicare.application.service.impl.UserServiceImpl;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.AcademicClass;
import com.unicare.domain.entity.Faculty;
import com.unicare.domain.entity.ManagementStaff;
import com.unicare.domain.entity.MedicalStaff;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;
import com.unicare.domain.repository.AcademicClassRepository;
import com.unicare.domain.repository.ManagementStaffRepository;
import com.unicare.domain.repository.MedicalStaffRepository;
import com.unicare.domain.repository.StudentRepository;
import com.unicare.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private MedicalStaffRepository medicalStaffRepository;

    @Mock
    private ManagementStaffRepository managementStaffRepository;

    @Mock
    private AcademicClassRepository academicClassRepository;

    private UserServiceImpl userService;

    private User sampleUser;
    private AcademicClass sampleClass;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                passwordEncoder,
                studentRepository,
                medicalStaffRepository,
                managementStaffRepository,
                academicClassRepository
        );

        sampleUser = new User("admin01", "hashed_pwd", "Quản Trị Viên", Role.BAN_QUAN_LY, UserStatus.ACTIVE);
        sampleUser.setId(1L);

        Faculty faculty = new Faculty("CNPM", "Công nghệ Phần mềm");
        faculty.setId(1);
        sampleClass = new AcademicClass("KTPM2024", "Kỹ thuật phần mềm 2024", "2024-2028", faculty);
        sampleClass.setId(5);
    }

    @Test
    @DisplayName("Lấy danh sách tài khoản thành công và tương thích constructor 2 tham số")
    void getUsers_Success() {
        UserServiceImpl legacyService = new UserServiceImpl(userRepository, passwordEncoder);
        when(userRepository.searchUsers("admin", null, null)).thenReturn(List.of(sampleUser));
        when(userRepository.existsByUsernameIgnoreCase("sv_legacy")).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("bs_legacy")).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("ql_legacy")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        List<UserResponse> responses = legacyService.getUsers(" admin ", null, null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUsername()).isEqualTo("admin01");
        assertThat(responses.get(0).getRole()).isEqualTo(Role.BAN_QUAN_LY);
        assertThat(UserResponse.fromEntity(null)).isNull();

        // Đảm bảo không lỗi NullPointerException khi các repository con là null
        assertThat(legacyService.createUser(new CreateUserRequest("sv_legacy", "SV", Role.SINH_VIEN, null))).isNotNull();
        assertThat(legacyService.createUser(new CreateUserRequest("bs_legacy", "BS", Role.NHAN_VIEN_Y_TE, null))).isNotNull();
        assertThat(legacyService.createUser(new CreateUserRequest("ql_legacy", "QL", Role.BAN_QUAN_LY, null))).isNotNull();
    }

    @Test
    @DisplayName("Tạo tài khoản SINH_VIEN mới thành công kèm bản ghi Student và Lớp học")
    void createUser_StudentWithClass_Success() {
        CreateUserRequest request = new CreateUserRequest("24520001", "Nguyễn Văn A", Role.SINH_VIEN, null);
        request.setEmail("  SV01@Gm.Uit.Edu.Vn ");
        request.setPhoneNumber(" 0901234567 ");
        request.setClassId(5);

        when(userRepository.existsByUsernameIgnoreCase("24520001")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("sv01@gm.uit.edu.vn")).thenReturn(false);
        when(passwordEncoder.encode(UserServiceImpl.DEFAULT_PASSWORD)).thenReturn("encoded_default_pwd");
        when(academicClassRepository.findById(5)).thenReturn(Optional.of(sampleClass));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(studentRepository.findById(2L)).thenReturn(Optional.empty());
        when(studentRepository.existsByStudentCodeIgnoreCase("24520001")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.createUser(request);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getUsername()).isEqualTo("24520001");
        assertThat(response.getEmail()).isEqualTo("sv01@gm.uit.edu.vn");
        assertThat(response.getPhoneNumber()).isEqualTo("0901234567");
        assertThat(response.getStudentCode()).isEqualTo("24520001");
        assertThat(response.getClassId()).isEqualTo(5);
        assertThat(response.getClassCode()).isEqualTo("KTPM2024");
        assertThat(response.getClassName()).isEqualTo("Kỹ thuật phần mềm 2024");
        assertThat(response.getFacultyName()).isEqualTo("Công nghệ Phần mềm");
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("Tạo tài khoản NHAN_VIEN_Y_TE tự chuẩn hóa title & specialty và tạo MedicalStaff")
    void createUser_MedicalStaff_NormalizesTitleAndSpecialty() {
        CreateUserRequest psychReq = new CreateUserRequest("cgtl_long_username", "ThS. Tâm Lý", Role.NHAN_VIEN_Y_TE, "Custom@123");
        psychReq.setTitle("Chuyên gia tâm lý");
        psychReq.setSpecialty("Tư vấn tâm lý");

        when(userRepository.existsByUsernameIgnoreCase("cgtl_long_username")).thenReturn(false);
        when(passwordEncoder.encode("Custom@123")).thenReturn("encoded_custom");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(12L);
            return u;
        });
        when(medicalStaffRepository.findById(12L)).thenReturn(Optional.empty());
        when(medicalStaffRepository.existsByStaffCodeIgnoreCase("NV00000012")).thenReturn(false);
        when(medicalStaffRepository.save(any(MedicalStaff.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.createUser(psychReq);

        assertThat(response.getStaffCode()).isEqualTo("NV00000012");
        assertThat(response.getTitle()).isEqualTo("CHUYEN_GIA_TAM_LY");
        assertThat(response.getSpecialty()).isEqualTo("TAM_LY");

        // Kiểm tra giá trị mặc định BAC_SI & DA_KHOA khi không truyền title/specialty
        CreateUserRequest defaultDocReq = new CreateUserRequest("bs001", "BS. Đa Khoa", Role.NHAN_VIEN_Y_TE, "   ");
        when(userRepository.existsByUsernameIgnoreCase("bs001")).thenReturn(false);
        when(passwordEncoder.encode(UserServiceImpl.DEFAULT_PASSWORD)).thenReturn("encoded_default");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(13L);
            return u;
        });
        when(medicalStaffRepository.findById(13L)).thenReturn(Optional.empty());
        when(medicalStaffRepository.existsByStaffCodeIgnoreCase("bs001")).thenReturn(true);
        when(medicalStaffRepository.save(any(MedicalStaff.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse defaultResp = userService.createUser(defaultDocReq);
        assertThat(defaultResp.getStaffCode()).isEqualTo("NV00000013");
        assertThat(defaultResp.getTitle()).isEqualTo("BAC_SI");
        assertThat(defaultResp.getSpecialty()).isEqualTo("DA_KHOA");
    }

    @Test
    @DisplayName("Tạo tài khoản BAN_QUAN_LY tự tạo bản ghi ManagementStaff phục vụ khóa ngoại handbooks")
    void createUser_ManagementStaff_Success() {
        CreateUserRequest request = new CreateUserRequest("bql01", "Quản Lý Viên", Role.BAN_QUAN_LY, null);
        request.setStaffCode("QL2026");

        when(userRepository.existsByUsernameIgnoreCase("bql01")).thenReturn(false);
        when(passwordEncoder.encode(UserServiceImpl.DEFAULT_PASSWORD)).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(3L);
            return u;
        });
        when(managementStaffRepository.findById(3L)).thenReturn(Optional.empty());
        when(managementStaffRepository.existsByStaffCodeIgnoreCase("QL2026")).thenReturn(false);
        when(managementStaffRepository.save(any(ManagementStaff.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.createUser(request);

        assertThat(response.getStaffCode()).isEqualTo("QL2026");
        verify(managementStaffRepository).save(any(ManagementStaff.class));
    }

    @Test
    @DisplayName("Tạo tài khoản thất bại do trùng username, trùng email, hoặc định dạng không hợp lệ")
    void createUser_ValidationAndDuplicates_ThrowsException() {
        CreateUserRequest dupUser = new CreateUserRequest("admin01", "Nguyễn Văn A", Role.SINH_VIEN, null);
        when(userRepository.existsByUsernameIgnoreCase("admin01")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dupUser))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("đã tồn tại");

        CreateUserRequest invalidEmail = new CreateUserRequest("sv_new", "Nguyễn Văn A", Role.SINH_VIEN, null);
        invalidEmail.setEmail("invalid-email");
        when(userRepository.existsByUsernameIgnoreCase("sv_new")).thenReturn(false);

        assertThatThrownBy(() -> userService.createUser(invalidEmail))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Định dạng email không hợp lệ");

        CreateUserRequest dupEmail = new CreateUserRequest("sv_new", "Nguyễn Văn A", Role.SINH_VIEN, null);
        dupEmail.setEmail("dup@uit.edu.vn");
        when(userRepository.existsByEmailIgnoreCase("dup@uit.edu.vn")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(dupEmail))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email 'dup@uit.edu.vn' đã được sử dụng");

        CreateUserRequest longPhone = new CreateUserRequest("sv_new", "Nguyễn Văn A", Role.SINH_VIEN, null);
        longPhone.setPhoneNumber("01234567890123456");

        assertThatThrownBy(() -> userService.createUser(longPhone))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Số điện thoại không được vượt quá 15 ký tự");
    }

    @Test
    @DisplayName("Kiểm tra lỗi trùng mã con (studentCode / staffCode) và lớp học không tồn tại")
    void createAndUpdateUser_ChildCodeAndClassErrors() {
        when(userRepository.existsByUsernameIgnoreCase("u_test")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getId() == null) {
                u.setId(50L);
            }
            return u;
        });

        // 1. classId không tồn tại
        CreateUserRequest badClassId = new CreateUserRequest("u_test", "SV Test", Role.SINH_VIEN, null);
        badClassId.setClassId(999);
        when(academicClassRepository.findById(999)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.createUser(badClassId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy lớp học với mã ID: 999");

        // 2. classCode không tồn tại
        CreateUserRequest badClassCode = new CreateUserRequest("u_test", "SV Test", Role.SINH_VIEN, null);
        badClassCode.setClassCode("UNKNOWN");
        when(academicClassRepository.findByClassCodeIgnoreCase("UNKNOWN")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.createUser(badClassCode))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy lớp học với mã lớp: UNKNOWN");

        // 3. studentCode dài quá 10 ký tự
        CreateUserRequest tooLongCode = new CreateUserRequest("u_test", "SV Test", Role.SINH_VIEN, null);
        tooLongCode.setStudentCode("CODE_TOO_LONG_123");
        assertThatThrownBy(() -> userService.createUser(tooLongCode))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không được vượt quá 10 ký tự");

        // 4. Trùng explicit studentCode khi tạo mới
        CreateUserRequest dupStudentCode = new CreateUserRequest("u_test", "SV Test", Role.SINH_VIEN, null);
        dupStudentCode.setStudentCode("24520001");
        when(studentRepository.existsByStudentCodeIgnoreCase("24520001")).thenReturn(true);
        assertThatThrownBy(() -> userService.createUser(dupStudentCode))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Mã sinh viên '24520001' đã tồn tại");

        // 5. Trùng explicit staffCode của MedicalStaff và ManagementStaff
        CreateUserRequest dupDocCode = new CreateUserRequest("u_test", "BS Test", Role.NHAN_VIEN_Y_TE, null);
        dupDocCode.setStaffCode("BS01");
        when(medicalStaffRepository.existsByStaffCodeIgnoreCase("BS01")).thenReturn(true);
        assertThatThrownBy(() -> userService.createUser(dupDocCode))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Mã nhân viên 'BS01' đã tồn tại");

        CreateUserRequest dupMgtCode = new CreateUserRequest("u_test", "QL Test", Role.BAN_QUAN_LY, null);
        dupMgtCode.setStaffCode("QL01");
        when(managementStaffRepository.existsByStaffCodeIgnoreCase("QL01")).thenReturn(true);
        assertThatThrownBy(() -> userService.createUser(dupMgtCode))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Mã quản lý 'QL01' đã tồn tại");
    }

    @Test
    @DisplayName("Cập nhật thông tin tài khoản và đồng bộ bản ghi con theo từng Role thành công")
    void updateUser_SyncsExistingProfiles_Success() {
        // 1. Cập nhật BAN_QUAN_LY đã có ManagementStaff
        ManagementStaff existingMgt = ManagementStaff.builder().id(1L).user(sampleUser).staffCode("QL0001").build();
        sampleUser.setManagementStaff(existingMgt);
        UpdateUserRequest mgtReq = new UpdateUserRequest("Quản Trị Viên Cập Nhật", Role.BAN_QUAN_LY);
        mgtReq.setEmail("admin01@uit.edu.vn");
        mgtReq.setPhoneNumber("0912345678");
        mgtReq.setStaffCode("QL0009");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("admin01@uit.edu.vn", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(managementStaffRepository.findById(1L)).thenReturn(Optional.of(existingMgt));
        when(managementStaffRepository.existsByStaffCodeIgnoreCaseAndIdNot("QL0009", 1L)).thenReturn(false);
        when(managementStaffRepository.save(any(ManagementStaff.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse mgtResp = userService.updateUser(1L, mgtReq, "admin01");
        assertThat(mgtResp.getFullName()).isEqualTo("Quản Trị Viên Cập Nhật");
        assertThat(mgtResp.getEmail()).isEqualTo("admin01@uit.edu.vn");
        assertThat(mgtResp.getStaffCode()).isEqualTo("QL0009");

        // 2. Cập nhật SINH_VIEN đã có Student qua classCode và xóa email/phone rỗng
        User studentUser = new User("24520002", "hash", "SV B", Role.SINH_VIEN, UserStatus.ACTIVE);
        studentUser.setId(20L);
        Student existingStudent = new Student(20L, studentUser, "24520002", null);
        studentUser.setStudent(existingStudent);

        UpdateUserRequest stuReq = new UpdateUserRequest("SV B Mới", Role.SINH_VIEN);
        stuReq.setEmail("   ");
        stuReq.setPhoneNumber("   ");
        stuReq.setStudentCode("24529999");
        stuReq.setClassCode("KTPM2024");

        when(userRepository.findById(20L)).thenReturn(Optional.of(studentUser));
        when(userRepository.save(any(User.class))).thenReturn(studentUser);
        when(academicClassRepository.findByClassCodeIgnoreCase("KTPM2024")).thenReturn(Optional.of(sampleClass));
        when(studentRepository.findById(20L)).thenReturn(Optional.of(existingStudent));
        when(studentRepository.existsByStudentCodeIgnoreCaseAndIdNot("24529999", 20L)).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse stuResp = userService.updateUser(20L, stuReq, "admin01");
        assertThat(stuResp.getEmail()).isNull();
        assertThat(stuResp.getPhoneNumber()).isNull();
        assertThat(stuResp.getStudentCode()).isEqualTo("24529999");
        assertThat(stuResp.getClassCode()).isEqualTo("KTPM2024");

        // 3. Cập nhật NHAN_VIEN_Y_TE đã có MedicalStaff (đổi sang Điều dưỡng / Khám đa khoa)
        User staffUser = new User("dd01", "hash", "ĐD. C", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        staffUser.setId(30L);
        MedicalStaff existingStaff = MedicalStaff.builder().id(30L).user(staffUser).staffCode("DD01").title("").specialty("").build();
        staffUser.setMedicalStaff(existingStaff);

        UpdateUserRequest staffReq = new UpdateUserRequest("Điều dưỡng C", Role.NHAN_VIEN_Y_TE);
        staffReq.setStaffCode("DD0030");
        staffReq.setTitle("Điều dưỡng");
        staffReq.setSpecialty("KHAM_DA_KHOA");

        when(userRepository.findById(30L)).thenReturn(Optional.of(staffUser));
        when(userRepository.save(any(User.class))).thenReturn(staffUser);
        when(medicalStaffRepository.findById(30L)).thenReturn(Optional.of(existingStaff));
        when(medicalStaffRepository.existsByStaffCodeIgnoreCaseAndIdNot("DD0030", 30L)).thenReturn(false);
        when(medicalStaffRepository.save(any(MedicalStaff.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse staffResp = userService.updateUser(30L, staffReq, "admin01");
        assertThat(staffResp.getStaffCode()).isEqualTo("DD0030");
        assertThat(staffResp.getTitle()).isEqualTo("DIEU_DUONG");
        assertThat(staffResp.getSpecialty()).isEqualTo("DA_KHOA");
    }

    @Test
    @DisplayName("Chặn trùng email hoặc mã nhân sự khi cập nhật và kiểm tra giới hạn độ dài title/specialty")
    void updateUser_DuplicateEmailAndInvalidStaffMeta_ThrowsException() {
        User otherUser = new User("bs01", "hash", "BS B", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        otherUser.setId(2L);
        MedicalStaff existingStaff = MedicalStaff.builder().id(2L).user(otherUser).staffCode("BS01").title("BAC_SI").specialty("DA_KHOA").build();
        otherUser.setMedicalStaff(existingStaff);

        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("taken@uit.edu.vn", 2L)).thenReturn(true);

        UpdateUserRequest dupEmailReq = new UpdateUserRequest("BS B", Role.NHAN_VIEN_Y_TE);
        dupEmailReq.setEmail("taken@uit.edu.vn");

        assertThatThrownBy(() -> userService.updateUser(2L, dupEmailReq, "admin01"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email 'taken@uit.edu.vn' đã được sử dụng");

        when(userRepository.save(any(User.class))).thenReturn(otherUser);
        when(medicalStaffRepository.findById(2L)).thenReturn(Optional.of(existingStaff));

        UpdateUserRequest longTitleReq = new UpdateUserRequest("BS B", Role.NHAN_VIEN_Y_TE);
        longTitleReq.setTitle("CHUC_DANH_QUA_DAI_VUOT_QUA_30_KY_TU_CHO_PHEP");
        assertThatThrownBy(() -> userService.updateUser(2L, longTitleReq, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Chức danh không được vượt quá 30 ký tự");

        UpdateUserRequest longSpecReq = new UpdateUserRequest("BS B", Role.NHAN_VIEN_Y_TE);
        longSpecReq.setSpecialty("CHUYEN_MON_QUA_DAI_VUOT_QUA_30_KY_TU_CHO_PHEP");
        assertThatThrownBy(() -> userService.updateUser(2L, longSpecReq, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Chuyên môn không được vượt quá 30 ký tự");
    }

    @Test
    @DisplayName("Chặn tự hạ vai trò của chính tài khoản đang đăng nhập")
    void updateUser_SelfDemote_ThrowsException() {
        UpdateUserRequest request = new UpdateUserRequest("Quản Trị Viên", Role.SINH_VIEN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> userService.updateUser(1L, request, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không thể tự thay đổi vai trò");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Khóa tài khoản người dùng khác thành công")
    void updateUserStatus_LockOtherUser_Success() {
        User otherUser = new User("bs01", "hash", "Bác sĩ B", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        otherUser.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(userRepository.save(any(User.class))).thenReturn(otherUser);

        UpdateUserStatusRequest request = new UpdateUserStatusRequest(UserStatus.LOCKED);
        UserResponse response = userService.updateUserStatus(2L, request, "admin01");

        assertThat(response.getStatus()).isEqualTo(UserStatus.LOCKED);
        verify(userRepository).save(otherUser);
    }

    @Test
    @DisplayName("Chặn tự khóa tài khoản của chính mình (Self-lock prevention)")
    void updateUserStatus_SelfLock_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UpdateUserStatusRequest request = new UpdateUserStatusRequest(UserStatus.LOCKED);
        assertThatThrownBy(() -> userService.updateUserStatus(1L, request, "admin01"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không thể tự khóa tài khoản của chính mình");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Cấp lại mật khẩu mặc định thành công")
    void resetPassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode(UserServiceImpl.DEFAULT_PASSWORD)).thenReturn("new_hash");

        userService.resetPassword(1L);

        assertThat(sampleUser.getPassword()).isEqualTo("new_hash");
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Xem chi tiết theo ID thành công và báo 404 khi không tồn tại")
    void getUserById_Success_And_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserResponse found = userService.getUserById(1L);
        assertThat(found.getId()).isEqualTo(1L);

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy người dùng");
    }

    @Test
    @DisplayName("Bao phủ các nhánh cập nhật không đổi mã, trùng mã khi sửa, và getter/setter DTO")
    void updateUser_PreserveOptionalFields_And_DtoAccessors() {
        User staffUser = new User("bs99", "hash", "BS 99", Role.NHAN_VIEN_Y_TE, UserStatus.ACTIVE);
        staffUser.setId(99L);
        MedicalStaff existingStaff = MedicalStaff.builder().id(99L).user(staffUser).staffCode("BS99").title("").specialty("").build();
        staffUser.setMedicalStaff(existingStaff);

        when(userRepository.findById(99L)).thenReturn(Optional.of(staffUser));
        when(userRepository.save(any(User.class))).thenReturn(staffUser);
        when(medicalStaffRepository.findById(99L)).thenReturn(Optional.of(existingStaff));
        when(medicalStaffRepository.save(any(MedicalStaff.class))).thenAnswer(inv -> inv.getArgument(0));

        // Cập nhật không truyền email/phone/staffCode/title/specialty -> tự gán mặc định BAC_SI / DA_KHOA khi đang rỗng
        UpdateUserRequest minimalUpdate = new UpdateUserRequest("BS 99 Mới", Role.NHAN_VIEN_Y_TE);
        UserResponse resp = userService.updateUser(99L, minimalUpdate, null);
        assertThat(resp.getTitle()).isEqualTo("BAC_SI");
        assertThat(resp.getSpecialty()).isEqualTo("DA_KHOA");

        // Chấp nhận custom title / specialty hợp lệ <= 30 ký tự
        UpdateUserRequest customStaff = new UpdateUserRequest("BS Chuyên Khoa", Role.NHAN_VIEN_Y_TE);
        customStaff.setTitle("Y_SI");
        customStaff.setSpecialty("NOI_TONG_QUAT");
        UserResponse customResp = userService.updateUser(99L, customStaff, "  ");
        assertThat(customResp.getTitle()).isEqualTo("Y_SI");
        assertThat(customResp.getSpecialty()).isEqualTo("NOI_TONG_QUAT");

        // Trùng mã khi cập nhật
        UpdateUserRequest dupStaffCode = new UpdateUserRequest("BS Chuyên Khoa", Role.NHAN_VIEN_Y_TE);
        dupStaffCode.setStaffCode("BS_DUP");
        when(medicalStaffRepository.existsByStaffCodeIgnoreCaseAndIdNot("BS_DUP", 99L)).thenReturn(true);
        assertThatThrownBy(() -> userService.updateUser(99L, dupStaffCode, "admin01"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Mã nhân viên 'BS_DUP' đã tồn tại");

        // Kiểm tra DTO constructors & setters
        CreateUserRequest createDto = new CreateUserRequest();
        createDto.setUsername("u1");
        createDto.setFullName("F1");
        createDto.setRole(Role.SINH_VIEN);
        createDto.setPassword("p1");
        assertThat(createDto.getUsername()).isEqualTo("u1");

        UpdateUserRequest updateDto = new UpdateUserRequest();
        updateDto.setFullName("F2");
        updateDto.setRole(Role.BAN_QUAN_LY);
        assertThat(updateDto.getFullName()).isEqualTo("F2");

        UserResponse resDto = new UserResponse();
        resDto.setId(10L);
        resDto.setUsername("u10");
        resDto.setFullName("Full 10");
        resDto.setRole(Role.SINH_VIEN);
        resDto.setStatus(UserStatus.ACTIVE);
        assertThat(resDto.getId()).isEqualTo(10L);
    }
}
