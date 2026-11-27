package com.unicare.application.service.impl;

import com.unicare.application.dto.request.CreateUserRequest;
import com.unicare.application.dto.request.UpdateUserRequest;
import com.unicare.application.dto.request.UpdateUserStatusRequest;
import com.unicare.application.dto.response.UserResponse;
import com.unicare.application.service.UserService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.AcademicClass;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    public static final String DEFAULT_PASSWORD = "UniCare@123";
    public static final String DEFAULT_STAFF_TITLE = "BAC_SI";
    public static final String DEFAULT_STAFF_SPECIALTY = "DA_KHOA";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Set<String> DOCTOR_TITLES = Set.of("BAC_SI", "BÁC SĨ");
    private static final Set<String> PSYCHOLOGIST_TITLES = Set.of("CHUYEN_GIA_TAM_LY", "CHUYÊN GIA TÂM LÝ");
    private static final Set<String> NURSE_TITLES = Set.of("DIEU_DUONG", "ĐIỀU DƯỠNG");
    private static final Set<String> GENERAL_SPECIALTIES = Set.of("DA_KHOA", "KHAM_DA_KHOA", "ĐA KHOA", "KHÁM ĐA KHOA");
    private static final Set<String> PSYCHOLOGY_SPECIALTIES = Set.of("TAM_LY", "TU_VAN_TAM_LY", "TÂM LÝ", "TƯ VẤN TÂM LÝ");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final MedicalStaffRepository medicalStaffRepository;
    private final ManagementStaffRepository managementStaffRepository;
    private final AcademicClassRepository academicClassRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this(userRepository, passwordEncoder, null, null, null, null);
    }

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            StudentRepository studentRepository,
            MedicalStaffRepository medicalStaffRepository,
            ManagementStaffRepository managementStaffRepository,
            AcademicClassRepository academicClassRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.studentRepository = studentRepository;
        this.medicalStaffRepository = medicalStaffRepository;
        this.managementStaffRepository = managementStaffRepository;
        this.academicClassRepository = academicClassRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(String search, Role role, UserStatus status) {
        String cleanSearch = search == null ? "" : search.trim();
        List<User> users = userRepository.searchUsers(cleanSearch, role, status);
        return users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với mã ID: " + id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String cleanUsername = request.getUsername().trim();
        if (userRepository.existsByUsernameIgnoreCase(cleanUsername)) {
            throw new DuplicateResourceException("Tên đăng nhập '" + cleanUsername + "' đã tồn tại trên hệ thống.");
        }

        String cleanEmail = normalizeAndValidateEmail(request.getEmail());
        if (cleanEmail != null && userRepository.existsByEmailIgnoreCase(cleanEmail)) {
            throw new DuplicateResourceException("Email '" + cleanEmail + "' đã được sử dụng bởi tài khoản khác.");
        }

        String cleanPhone = normalizePhoneNumber(request.getPhoneNumber());

        String rawPassword = (request.getPassword() != null && !request.getPassword().trim().isEmpty())
                ? request.getPassword().trim()
                : DEFAULT_PASSWORD;

        User user = new User(
                cleanUsername,
                passwordEncoder.encode(rawPassword),
                request.getFullName().trim(),
                request.getRole(),
                UserStatus.ACTIVE
        );
        user.setEmail(cleanEmail);
        user.setPhoneNumber(cleanPhone);

        User savedUser = userRepository.save(user);
        syncRoleProfile(
                savedUser,
                request.getStudentCode(),
                request.getClassId(),
                request.getClassCode(),
                request.getStaffCode(),
                request.getTitle(),
                request.getSpecialty()
        );
        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với mã ID: " + id));

        // Ràng buộc bảo mật: Chặn tự thay đổi vai trò của chính tài khoản đang đăng nhập
        if (currentUsername != null && !currentUsername.isBlank()
                && user.getUsername().equalsIgnoreCase(currentUsername)
                && user.getRole() != request.getRole()) {
            throw new BadRequestException("Bạn không thể tự thay đổi vai trò của chính mình.");
        }

        if (request.getEmail() != null) {
            String cleanEmail = normalizeAndValidateEmail(request.getEmail());
            if (cleanEmail != null && userRepository.existsByEmailIgnoreCaseAndIdNot(cleanEmail, user.getId())) {
                throw new DuplicateResourceException("Email '" + cleanEmail + "' đã được sử dụng bởi tài khoản khác.");
            }
            user.setEmail(cleanEmail);
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(normalizePhoneNumber(request.getPhoneNumber()));
        }

        user.setFullName(request.getFullName().trim());
        user.setRole(request.getRole());

        User updatedUser = userRepository.save(user);
        syncRoleProfile(
                updatedUser,
                request.getStudentCode(),
                request.getClassId(),
                request.getClassCode(),
                request.getStaffCode(),
                request.getTitle(),
                request.getSpecialty()
        );
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long id, UpdateUserStatusRequest request, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với mã ID: " + id));

        // Ràng buộc bảo mật: Chặn tự khóa tài khoản của chính mình (Self-lock prevention)
        if (currentUsername != null && !currentUsername.isBlank()
                && user.getUsername().equalsIgnoreCase(currentUsername)
                && request.getStatus() == UserStatus.LOCKED) {
            throw new BadRequestException("Bạn không thể tự khóa tài khoản của chính mình.");
        }

        user.setStatus(request.getStatus());
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với mã ID: " + id));

        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        userRepository.save(user);
    }

    private void syncRoleProfile(
            User user,
            String explicitStudentCode,
            Integer classId,
            String classCode,
            String explicitStaffCode,
            String rawTitle,
            String rawSpecialty
    ) {
        if (user.getRole() == Role.SINH_VIEN) {
            syncStudentProfile(user, explicitStudentCode, classId, classCode);
        } else if (user.getRole() == Role.NHAN_VIEN_Y_TE) {
            syncMedicalStaffProfile(user, explicitStaffCode, rawTitle, rawSpecialty);
        } else if (user.getRole() == Role.BAN_QUAN_LY) {
            syncManagementStaffProfile(user, explicitStaffCode);
        }
    }

    private void syncStudentProfile(User user, String explicitStudentCode, Integer classId, String classCode) {
        if (studentRepository == null) {
            return;
        }
        AcademicClass resolvedClass = resolveAcademicClass(classId, classCode);
        Student student = studentRepository.findById(user.getId()).orElse(user.getStudent());

        if (student == null) {
            String studentCode = resolveNewCode(
                    explicitStudentCode,
                    "Mã sinh viên",
                    user.getUsername(),
                    "SV",
                    user.getId(),
                    studentRepository::existsByStudentCodeIgnoreCase
            );
            student = new Student(user.getId(), user, studentCode, resolvedClass);
        } else {
            String updatedCode = resolveUpdatedCode(
                    explicitStudentCode,
                    "Mã sinh viên",
                    user.getId(),
                    studentRepository::existsByStudentCodeIgnoreCaseAndIdNot
            );
            if (updatedCode != null) {
                student.setStudentCode(updatedCode);
            }
            if (resolvedClass != null) {
                student.setAcademicClass(resolvedClass);
            }
        }

        studentRepository.save(student);
        user.setStudent(student);
    }

    private void syncMedicalStaffProfile(User user, String explicitStaffCode, String rawTitle, String rawSpecialty) {
        if (medicalStaffRepository == null) {
            return;
        }
        MedicalStaff staff = medicalStaffRepository.findById(user.getId()).orElse(user.getMedicalStaff());

        if (staff == null) {
            String staffCode = resolveNewCode(
                    explicitStaffCode,
                    "Mã nhân viên",
                    user.getUsername(),
                    "NV",
                    user.getId(),
                    medicalStaffRepository::existsByStaffCodeIgnoreCase
            );
            staff = MedicalStaff.builder()
                    .id(user.getId())
                    .user(user)
                    .staffCode(staffCode)
                    .title(normalizeStaffTitle(rawTitle))
                    .specialty(normalizeSpecialty(rawSpecialty))
                    .build();
        } else {
            String updatedCode = resolveUpdatedCode(
                    explicitStaffCode,
                    "Mã nhân viên",
                    user.getId(),
                    medicalStaffRepository::existsByStaffCodeIgnoreCaseAndIdNot
            );
            if (updatedCode != null) {
                staff.setStaffCode(updatedCode);
            }
            if (rawTitle != null && !rawTitle.isBlank()) {
                staff.setTitle(normalizeStaffTitle(rawTitle));
            } else if (staff.getTitle() == null || staff.getTitle().isBlank()) {
                staff.setTitle(DEFAULT_STAFF_TITLE);
            }
            if (rawSpecialty != null && !rawSpecialty.isBlank()) {
                staff.setSpecialty(normalizeSpecialty(rawSpecialty));
            } else if (staff.getSpecialty() == null || staff.getSpecialty().isBlank()) {
                staff.setSpecialty(DEFAULT_STAFF_SPECIALTY);
            }
        }

        medicalStaffRepository.save(staff);
        user.setMedicalStaff(staff);
    }

    private void syncManagementStaffProfile(User user, String explicitStaffCode) {
        if (managementStaffRepository == null) {
            return;
        }
        ManagementStaff managementStaff = managementStaffRepository.findById(user.getId()).orElse(user.getManagementStaff());

        if (managementStaff == null) {
            String staffCode = resolveNewCode(
                    explicitStaffCode,
                    "Mã quản lý",
                    user.getUsername(),
                    "QL",
                    user.getId(),
                    managementStaffRepository::existsByStaffCodeIgnoreCase
            );
            managementStaff = ManagementStaff.builder()
                    .id(user.getId())
                    .user(user)
                    .staffCode(staffCode)
                    .build();
        } else {
            String updatedCode = resolveUpdatedCode(
                    explicitStaffCode,
                    "Mã quản lý",
                    user.getId(),
                    managementStaffRepository::existsByStaffCodeIgnoreCaseAndIdNot
            );
            if (updatedCode != null) {
                managementStaff.setStaffCode(updatedCode);
            }
        }

        managementStaffRepository.save(managementStaff);
        user.setManagementStaff(managementStaff);
    }

    private AcademicClass resolveAcademicClass(Integer classId, String classCode) {
        if (academicClassRepository == null) {
            return null;
        }
        if (classId != null) {
            return academicClassRepository.findById(classId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với mã ID: " + classId));
        }
        if (classCode != null && !classCode.isBlank()) {
            String cleanClassCode = classCode.trim();
            return academicClassRepository.findByClassCodeIgnoreCase(cleanClassCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp học với mã lớp: " + cleanClassCode));
        }
        return null;
    }

    private String resolveNewCode(
            String explicitCode,
            String label,
            String username,
            String prefix,
            Long userId,
            Predicate<String> existsByCode
    ) {
        if (explicitCode != null && !explicitCode.isBlank()) {
            String code = validateExplicitCode(explicitCode, label);
            if (existsByCode.test(code)) {
                throw new DuplicateResourceException(label + " '" + code + "' đã tồn tại trên hệ thống.");
            }
            return code;
        }
        String candidate = username.length() <= 10 ? username : buildFallbackCode(prefix, userId);
        if (existsByCode.test(candidate)) {
            return buildFallbackCode(prefix, userId);
        }
        return candidate;
    }

    private String resolveUpdatedCode(
            String explicitCode,
            String label,
            Long userId,
            BiPredicate<String, Long> existsByCodeAndIdNot
    ) {
        if (explicitCode == null || explicitCode.isBlank()) {
            return null;
        }
        String code = validateExplicitCode(explicitCode, label);
        if (existsByCodeAndIdNot.test(code, userId)) {
            throw new DuplicateResourceException(label + " '" + code + "' đã tồn tại trên hệ thống.");
        }
        return code;
    }

    private String validateExplicitCode(String rawCode, String label) {
        String clean = rawCode.trim();
        if (clean.length() > 10) {
            throw new BadRequestException(label + " không được vượt quá 10 ký tự.");
        }
        return clean;
    }

    private String buildFallbackCode(String prefix, Long userId) {
        long safeId = userId != null ? userId : 0L;
        return prefix + String.format("%08d", safeId % 100_000_000L);
    }

    private String normalizeAndValidateEmail(String rawEmail) {
        if (rawEmail == null || rawEmail.trim().isEmpty()) {
            return null;
        }
        String cleanEmail = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (cleanEmail.length() > 100 || !EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new BadRequestException("Định dạng email không hợp lệ.");
        }
        return cleanEmail;
    }

    private String normalizePhoneNumber(String rawPhone) {
        if (rawPhone == null || rawPhone.trim().isEmpty()) {
            return null;
        }
        String cleanPhone = rawPhone.trim();
        if (cleanPhone.length() > 15) {
            throw new BadRequestException("Số điện thoại không được vượt quá 15 ký tự.");
        }
        return cleanPhone;
    }

    private String normalizeStaffTitle(String rawTitle) {
        if (rawTitle == null || rawTitle.isBlank()) {
            return DEFAULT_STAFF_TITLE;
        }
        String clean = rawTitle.trim().toUpperCase(Locale.ROOT);
        if (DOCTOR_TITLES.contains(clean)) {
            return "BAC_SI";
        }
        if (PSYCHOLOGIST_TITLES.contains(clean)) {
            return "CHUYEN_GIA_TAM_LY";
        }
        if (NURSE_TITLES.contains(clean)) {
            return "DIEU_DUONG";
        }
        if (clean.length() > 30) {
            throw new BadRequestException("Chức danh không được vượt quá 30 ký tự.");
        }
        return clean;
    }

    private String normalizeSpecialty(String rawSpecialty) {
        if (rawSpecialty == null || rawSpecialty.isBlank()) {
            return DEFAULT_STAFF_SPECIALTY;
        }
        String clean = rawSpecialty.trim().toUpperCase(Locale.ROOT);
        if (GENERAL_SPECIALTIES.contains(clean)) {
            return "DA_KHOA";
        }
        if (PSYCHOLOGY_SPECIALTIES.contains(clean)) {
            return "TAM_LY";
        }
        if (clean.length() > 30) {
            throw new BadRequestException("Chuyên môn không được vượt quá 30 ký tự.");
        }
        return clean;
    }
}
