package com.unicare.application.service.impl;

import com.unicare.application.dto.request.CreateMedicalCheckupRequest;
import com.unicare.application.dto.request.PrescribedMedicineRequest;
import com.unicare.application.dto.request.UpdateMedicalCheckupRequest;
import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.service.MedicalCheckupService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.Appointment;
import com.unicare.domain.entity.DiseaseGroup;
import com.unicare.domain.entity.MedicalCheckup;
import com.unicare.domain.entity.MedicalStaff;
import com.unicare.domain.entity.PrescribedMedicine;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.repository.AppointmentRepository;
import com.unicare.domain.repository.DiseaseGroupRepository;
import com.unicare.domain.repository.MedicalCheckupRepository;
import com.unicare.domain.repository.MedicalStaffRepository;
import com.unicare.domain.repository.StudentRepository;
import com.unicare.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MedicalCheckupServiceImpl implements MedicalCheckupService {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String APPOINTMENT_STATUS_CONFIRMED = "CONFIRMED";
    public static final String APPOINTMENT_STATUS_COMPLETED = "COMPLETED";

    public static final String CHECKUP_TYPE_GENERAL = "GENERAL";
    public static final String CHECKUP_TYPE_PSYCHOLOGICAL = "PSYCHOLOGICAL";

    public static final String PERIODIC_RESULT_PASSED = "PASSED";
    public static final String PERIODIC_RESULT_FAILED = "FAILED";

    private static final DateTimeFormatter CODE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final MedicalCheckupRepository medicalCheckupRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final StudentRepository studentRepository;
    private final MedicalStaffRepository medicalStaffRepository;
    private final DiseaseGroupRepository diseaseGroupRepository;

    public MedicalCheckupServiceImpl(
            MedicalCheckupRepository medicalCheckupRepository,
            UserRepository userRepository) {
        this(medicalCheckupRepository, userRepository, null, null, null, null);
    }

    @Autowired
    public MedicalCheckupServiceImpl(
            MedicalCheckupRepository medicalCheckupRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            StudentRepository studentRepository,
            MedicalStaffRepository medicalStaffRepository,
            DiseaseGroupRepository diseaseGroupRepository) {
        this.medicalCheckupRepository = medicalCheckupRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.studentRepository = studentRepository;
        this.medicalStaffRepository = medicalStaffRepository;
        this.diseaseGroupRepository = diseaseGroupRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentPersonalInfoResponse> searchStudentsForIntake(String keyword, String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);

        if (currentUser.getRole() == Role.BAN_QUAN_LY) {
            throw new AccessDeniedException("Ban quản lý không được phép truy cập dữ liệu bệnh án cá nhân của sinh viên");
        }
        if (currentUser.getRole() != Role.NHAN_VIEN_Y_TE) {
            throw new AccessDeniedException("Chỉ tài khoản Bác sĩ / Nhân viên y tế mới có quyền tra cứu nhanh sinh viên khi tiếp nhận");
        }

        String cleanKeyword = keyword != null ? keyword.trim() : "";
        List<Student> students = studentRepository.searchStudentsByKeyword(cleanKeyword);
        if (students == null || students.isEmpty()) {
            return Collections.emptyList();
        }

        return students.stream()
                .limit(20)
                .map(s -> StudentPersonalInfoResponse.fromEntities(s.getUser(), s))
                .toList();
    }

    @Override
    @Transactional
    public MedicalCheckupDetailResponse createCheckup(CreateMedicalCheckupRequest request, String currentUsername) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu tiếp nhận và lập phiếu khám không được để trống");
        }

        User currentUser = resolveCurrentUser(currentUsername);
        validateDoctorRole(currentUser, "tiếp nhận và lập phiếu khám");
        MedicalStaff currentStaff = resolveMedicalStaff(currentUser);

        Appointment appointment = null;
        Student student;
        boolean isEmergency;
        String checkupType;
        String initialSymptoms = trimToNull(request.getSymptoms());

        if (request.getAppointmentId() != null) {
            // Luồng 1: Tiếp nhận sinh viên từ lịch hẹn CONFIRMED
            if (Boolean.TRUE.equals(request.getIsEmergency())) {
                throw new BadRequestException("Ca tiếp nhận từ lịch hẹn đặt trước không thể đánh dấu là ca cấp cứu khẩn cấp");
            }

            appointment = appointmentRepository.findByIdWithDetails(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + request.getAppointmentId()));

            if (appointment.getShift() == null
                    || appointment.getShift().getStaff() == null
                    || !appointment.getShift().getStaff().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Chỉ bác sĩ phụ trách ca khám mới có quyền tiếp nhận lịch hẹn này");
            }

            Optional<MedicalCheckup> existingCheckup = medicalCheckupRepository.findByAppointmentIdWithDetails(appointment.getId());
            if (existingCheckup.isPresent()) {
                throw new DuplicateResourceException(
                        "Lịch hẹn này đã được tiếp nhận và lập phiếu khám trước đó (Mã phiếu: "
                                + existingCheckup.get().getCheckupCode() + ")");
            }

            if (!APPOINTMENT_STATUS_CONFIRMED.equalsIgnoreCase(appointment.getStatus())) {
                throw new BadRequestException(
                        "Chỉ có thể tiếp nhận lịch hẹn đang ở trạng thái Đã xác nhận (CONFIRMED). Trạng thái hiện tại: "
                                + appointment.getStatus());
            }

            student = appointment.getStudent();
            if (student == null) {
                throw new BadRequestException("Lịch hẹn không có thông tin sinh viên hợp lệ");
            }

            if (request.getStudentId() != null && !request.getStudentId().equals(student.getId())) {
                throw new BadRequestException("Thông tin sinh viên (studentId) không khớp với lịch hẹn");
            }

            if (request.getStudentCode() != null && !request.getStudentCode().isBlank()
                    && student.getStudentCode() != null
                    && !request.getStudentCode().trim().equalsIgnoreCase(student.getStudentCode())) {
                throw new BadRequestException("Mã số sinh viên (studentCode) không khớp với lịch hẹn");
            }

            isEmergency = false;
            String expectedType = (appointment.getShift().getShiftType() == ServiceType.TU_VAN_TAM_LY)
                    ? CHECKUP_TYPE_PSYCHOLOGICAL
                    : CHECKUP_TYPE_GENERAL;

            if (request.getCheckupType() != null && !request.getCheckupType().isBlank()) {
                checkupType = normalizeCheckupType(request.getCheckupType());
            } else {
                checkupType = expectedType;
            }

            if (initialSymptoms == null && appointment.getReasonSymptoms() != null && !appointment.getReasonSymptoms().isBlank()) {
                initialSymptoms = appointment.getReasonSymptoms().trim();
            }
        } else {
            // Luồng 2: Tiếp nhận khẩn cấp / cấp cứu (không đặt lịch hẹn trước)
            student = resolveStudentForEmergencyIntake(request.getStudentId(), request.getStudentCode());
            isEmergency = (request.getIsEmergency() != null) ? request.getIsEmergency() : true;
            checkupType = (request.getCheckupType() != null && !request.getCheckupType().isBlank())
                    ? normalizeCheckupType(request.getCheckupType())
                    : CHECKUP_TYPE_GENERAL;
        }

        DiseaseGroup diseaseGroup = resolveDiseaseGroup(request.getDiseaseGroupId());
        String periodicResult = normalizePeriodicResult(request.getPeriodicResult());
        String targetStatus = normalizeStatus(request.getStatus());

        MedicalCheckup checkup = MedicalCheckup.builder()
                .checkupCode(generateUniqueCheckupCode())
                .student(student)
                .staff(currentStaff)
                .appointment(appointment)
                .checkupType(checkupType)
                .isEmergency(isEmergency)
                .periodicResult(periodicResult)
                .symptoms(initialSymptoms)
                .diagnosisNotes(trimToNull(request.getDiagnosisNotes()))
                .treatmentSuggestions(trimToNull(request.getTreatmentSuggestions()))
                .diseaseGroup(diseaseGroup)
                .status(STATUS_DRAFT)
                .createdAt(OffsetDateTime.now())
                .build();

        if (STATUS_COMPLETED.equals(targetStatus)) {
            applyCompletion(checkup);
        }

        MedicalCheckup savedCheckup = medicalCheckupRepository.save(checkup);
        return MedicalCheckupDetailResponse.fromEntity(savedCheckup);
    }

    @Override
    @Transactional
    public MedicalCheckupDetailResponse updateCheckup(Long checkupId, UpdateMedicalCheckupRequest request, String currentUsername) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu cập nhật phiếu khám không được để trống");
        }

        User currentUser = resolveCurrentUser(currentUsername);
        validateDoctorRole(currentUser, "cập nhật phiếu khám");

        MedicalCheckup checkup = medicalCheckupRepository.findByIdWithDetails(checkupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu khám với ID: " + checkupId));

        validateAssignedDoctor(checkup, currentUser, "cập nhật phiếu khám này");
        validateCheckupEditable(checkup, "chỉnh sửa");

        if (request.getCheckupType() != null && !request.getCheckupType().isBlank()) {
            checkup.setCheckupType(normalizeCheckupType(request.getCheckupType()));
        }

        if (request.getIsEmergency() != null && checkup.getAppointment() == null) {
            checkup.setIsEmergency(request.getIsEmergency());
        }

        checkup.setSymptoms(trimToNull(request.getSymptoms()));
        checkup.setDiagnosisNotes(trimToNull(request.getDiagnosisNotes()));
        checkup.setTreatmentSuggestions(trimToNull(request.getTreatmentSuggestions()));
        checkup.setDiseaseGroup(resolveDiseaseGroup(request.getDiseaseGroupId()));

        if (request.getPeriodicResult() != null) {
            checkup.setPeriodicResult(normalizePeriodicResult(request.getPeriodicResult()));
        }

        String targetStatus = normalizeStatus(request.getStatus());
        if (STATUS_COMPLETED.equals(targetStatus)) {
            applyCompletion(checkup);
        } else {
            checkup.setStatus(STATUS_DRAFT);
        }

        MedicalCheckup savedCheckup = medicalCheckupRepository.save(checkup);
        return MedicalCheckupDetailResponse.fromEntity(savedCheckup);
    }

    @Override
    @Transactional
    public MedicalCheckupDetailResponse completeCheckup(Long checkupId, String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);
        validateDoctorRole(currentUser, "hoàn tất phiếu khám");

        MedicalCheckup checkup = medicalCheckupRepository.findByIdWithDetails(checkupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu khám với ID: " + checkupId));

        validateAssignedDoctor(checkup, currentUser, "hoàn tất phiếu khám này");
        validateCheckupEditable(checkup, "hoàn tất lại hoặc chỉnh sửa");

        applyCompletion(checkup);

        MedicalCheckup savedCheckup = medicalCheckupRepository.save(checkup);
        return MedicalCheckupDetailResponse.fromEntity(savedCheckup);
    }

    @Override
    @Transactional
    public void deleteCheckup(Long checkupId, String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);
        validateDoctorRole(currentUser, "xóa phiếu khám");

        MedicalCheckup checkup = medicalCheckupRepository.findByIdWithDetails(checkupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu khám với ID: " + checkupId));

        validateAssignedDoctor(checkup, currentUser, "xóa phiếu khám này");
        validateCheckupEditable(checkup, "xóa");

        medicalCheckupRepository.delete(checkup);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalCheckupDetailResponse getCheckupByAppointment(Long appointmentId, String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);

        if (currentUser.getRole() == Role.BAN_QUAN_LY) {
            throw new AccessDeniedException("Ban quản lý không có quyền truy cập hồ sơ bệnh án chi tiết của sinh viên");
        }

        MedicalCheckup checkup = medicalCheckupRepository.findByAppointmentIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu khám cho lịch hẹn với ID: " + appointmentId));

        if (currentUser.getRole() == Role.SINH_VIEN) {
            if (checkup.getStudent() == null || !checkup.getStudent().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Sinh viên chỉ được phép xem thông tin phiếu khám của chính mình");
            }
        }

        return MedicalCheckupDetailResponse.fromEntity(checkup);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalCheckupDetailResponse getCheckupDetail(Long checkupId, String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);

        MedicalCheckup checkup = medicalCheckupRepository.findByIdWithDetails(checkupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu khám với ID: " + checkupId));

        if (currentUser.getRole() == Role.BAN_QUAN_LY) {
            throw new AccessDeniedException("Ban quản lý không có quyền truy cập hồ sơ bệnh án chi tiết của sinh viên");
        }

        if (currentUser.getRole() == Role.SINH_VIEN) {
            if (checkup.getStudent() == null || !checkup.getStudent().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Sinh viên chỉ được phép xem thông tin phiếu khám của chính mình");
            }
        }

        return MedicalCheckupDetailResponse.fromEntity(checkup);
    }

    @Override
    @Transactional
    public MedicalCheckupDetailResponse updatePrescription(Long checkupId, UpdatePrescriptionRequest request, String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);

        if (currentUser.getRole() != Role.NHAN_VIEN_Y_TE) {
            throw new AccessDeniedException("Chỉ tài khoản Bác sĩ / Nhân viên y tế mới có quyền kê đơn thuốc");
        }

        MedicalCheckup checkup = medicalCheckupRepository.findByIdWithDetails(checkupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu khám với ID: " + checkupId));

        if (checkup.getStaff() == null || !checkup.getStaff().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Chỉ bác sĩ phụ trách ca khám mới có quyền kê đơn thuốc cho phiếu khám này");
        }

        if (checkup.isCompleted()) {
            throw new BadRequestException("Phiếu khám đã hoàn tất (COMPLETED), đơn thuốc đã bị khóa vĩnh viễn và không thể chỉnh sửa");
        }

        checkup.clearPrescribedMedicines();

        // Nếu có thuốc được kê, tiến hành validate và thêm vào đơn
        if (request != null && request.getMedicines() != null && !request.getMedicines().isEmpty()) {
            Set<String> seenNames = new HashSet<>();
            for (PrescribedMedicineRequest item : request.getMedicines()) {
                String name = item.getMedicineName() != null ? item.getMedicineName().trim() : "";
                if (name.isEmpty()) {
                    throw new BadRequestException("Tên thuốc không được để trống");
                }
                if (name.length() > 100) {
                    throw new BadRequestException("Tên thuốc không được vượt quá 100 ký tự: " + name);
                }
                if (item.getQuantity() == null || item.getQuantity() < 1) {
                    throw new BadRequestException("Số lượng thuốc phải lớn hơn hoặc bằng 1 đối với thuốc: " + name);
                }
                String instruction = item.getDosageInstruction() != null ? item.getDosageInstruction().trim() : "";
                if (instruction.isEmpty()) {
                    throw new BadRequestException("Hướng dẫn liều dùng không được để trống đối với thuốc: " + name);
                }
                if (instruction.length() > 200) {
                    throw new BadRequestException("Hướng dẫn liều dùng không được vượt quá 200 ký tự đối với thuốc: " + name);
                }

                String lowerName = name.toLowerCase();
                if (!seenNames.add(lowerName)) {
                    throw new BadRequestException("Thuốc '" + name + "' bị trùng lặp trong đơn thuốc. Vui lòng gộp số lượng hoặc chỉnh sửa lại");
                }

                PrescribedMedicine medicine = PrescribedMedicine.builder()
                        .medicineName(name)
                        .quantity(item.getQuantity())
                        .dosageInstruction(instruction)
                        .build();

                checkup.addPrescribedMedicine(medicine);
            }
        }

        MedicalCheckup savedCheckup = medicalCheckupRepository.save(checkup);
        return MedicalCheckupDetailResponse.fromEntity(savedCheckup);
    }

    private void applyCompletion(MedicalCheckup checkup) {
        validateRequiredFieldsForCompletion(checkup);

        checkup.markCompleted(OffsetDateTime.now());

        Appointment appointment = checkup.getAppointment();
        if (appointment != null) {
            appointment.setStatus(APPOINTMENT_STATUS_COMPLETED);
            if (appointmentRepository != null) {
                appointmentRepository.save(appointment);
            }
        }
    }

    private void validateRequiredFieldsForCompletion(MedicalCheckup checkup) {
        String type = checkup.getCheckupType() != null ? checkup.getCheckupType() : CHECKUP_TYPE_GENERAL;
        if (CHECKUP_TYPE_PSYCHOLOGICAL.equalsIgnoreCase(type)) {
            if (checkup.getDiagnosisNotes() == null || checkup.getDiagnosisNotes().isBlank()) {
                throw new BadRequestException("Vui lòng nhập tóm tắt buổi tư vấn (diagnosisNotes) trước khi hoàn tất phiếu tư vấn tâm lý");
            }
        } else {
            if (checkup.getSymptoms() == null || checkup.getSymptoms().isBlank()) {
                throw new BadRequestException("Vui lòng nhập triệu chứng (symptoms) trước khi hoàn tất phiếu khám");
            }
            if (checkup.getDiagnosisNotes() == null || checkup.getDiagnosisNotes().isBlank()) {
                throw new BadRequestException("Vui lòng nhập kết luận chẩn đoán (diagnosisNotes) trước khi hoàn tất phiếu khám");
            }
        }
    }

    private void validateDoctorRole(User currentUser, String actionText) {
        if (currentUser.getRole() == Role.BAN_QUAN_LY) {
            throw new AccessDeniedException("Ban quản lý không có quyền truy cập hoặc " + actionText);
        }
        if (currentUser.getRole() != Role.NHAN_VIEN_Y_TE) {
            throw new AccessDeniedException("Chỉ tài khoản Bác sĩ / Nhân viên y tế mới có quyền " + actionText);
        }
    }

    private void validateAssignedDoctor(MedicalCheckup checkup, User currentUser, String actionText) {
        if (checkup.getStaff() == null || !checkup.getStaff().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Chỉ bác sĩ phụ trách ca khám mới có quyền " + actionText);
        }
    }

    private void validateCheckupEditable(MedicalCheckup checkup, String actionText) {
        if (!checkup.isEditable()) {
            throw new BadRequestException("Phiếu khám đã hoàn tất (COMPLETED), hồ sơ đã bị khóa Read-only vĩnh viễn và không thể " + actionText);
        }
    }

    private MedicalStaff resolveMedicalStaff(User currentUser) {
        if (currentUser.getMedicalStaff() != null) {
            return currentUser.getMedicalStaff();
        }
        if (medicalStaffRepository != null) {
            return medicalStaffRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy hồ sơ nhân viên y tế của tài khoản: " + currentUser.getUsername()));
        }
        throw new ResourceNotFoundException("Không tìm thấy hồ sơ nhân viên y tế của tài khoản: " + currentUser.getUsername());
    }

    private Student resolveStudentForEmergencyIntake(Long studentId, String studentCode) {
        if (studentId != null) {
            return studentRepository.findByIdWithDetails(studentId)
                    .or(() -> studentRepository.findById(studentId))
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với ID: " + studentId));
        }
        if (studentCode != null && !studentCode.isBlank()) {
            String cleanCode = studentCode.trim();
            return studentRepository.findByStudentCodeWithDetails(cleanCode)
                    .or(() -> studentRepository.findByStudentCode(cleanCode))
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với MSSV: " + cleanCode));
        }
        throw new BadRequestException("Vui lòng chọn lịch hẹn (appointmentId) hoặc truyền mã sinh viên (studentId / studentCode) để tiếp nhận");
    }

    private DiseaseGroup resolveDiseaseGroup(Integer diseaseGroupId) {
        if (diseaseGroupId == null) {
            return null;
        }
        if (diseaseGroupRepository == null) {
            throw new ResourceNotFoundException("Không tìm thấy nhóm bệnh dịch tễ với ID: " + diseaseGroupId);
        }
        return diseaseGroupRepository.findById(diseaseGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhóm bệnh dịch tễ với ID: " + diseaseGroupId));
    }

    private String normalizeCheckupType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return CHECKUP_TYPE_GENERAL;
        }
        String upper = rawType.trim().toUpperCase();
        if (CHECKUP_TYPE_GENERAL.equals(upper) || "KHAM_DA_KHOA".equals(upper) || "DA_KHOA".equals(upper)) {
            return CHECKUP_TYPE_GENERAL;
        }
        if (CHECKUP_TYPE_PSYCHOLOGICAL.equals(upper) || "TU_VAN_TAM_LY".equals(upper) || "TAM_LY".equals(upper)) {
            return CHECKUP_TYPE_PSYCHOLOGICAL;
        }
        throw new BadRequestException("Loại phiếu khám không hợp lệ: " + rawType + ". Chỉ chấp nhận GENERAL hoặc PSYCHOLOGICAL");
    }

    private String normalizePeriodicResult(String rawPeriodicResult) {
        if (rawPeriodicResult == null || rawPeriodicResult.isBlank()) {
            return null;
        }
        String upper = rawPeriodicResult.trim().toUpperCase();
        if (PERIODIC_RESULT_PASSED.equals(upper) || "DAT".equals(upper)) {
            return PERIODIC_RESULT_PASSED;
        }
        if (PERIODIC_RESULT_FAILED.equals(upper) || "KHONG_DAT".equals(upper)) {
            return PERIODIC_RESULT_FAILED;
        }
        throw new BadRequestException("Kết quả khám định kỳ không hợp lệ: " + rawPeriodicResult + ". Chỉ chấp nhận PASSED hoặc FAILED");
    }

    private String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return STATUS_DRAFT;
        }
        String upper = rawStatus.trim().toUpperCase();
        if (STATUS_DRAFT.equals(upper) || "NHAP".equals(upper)) {
            return STATUS_DRAFT;
        }
        if (STATUS_COMPLETED.equals(upper) || "DA_HOAN_TAT".equals(upper)) {
            return STATUS_COMPLETED;
        }
        throw new BadRequestException("Trạng thái phiếu khám không hợp lệ: " + rawStatus + ". Chỉ chấp nhận DRAFT hoặc COMPLETED");
    }

    private String generateUniqueCheckupCode() {
        String datePart = LocalDate.now().format(CODE_DATE_FORMAT);
        for (int attempt = 0; attempt < 20; attempt++) {
            int rand = SECURE_RANDOM.nextInt(10000);
            String candidate = String.format("PK%s%04d", datePart, rand);
            if (!medicalCheckupRepository.existsByCheckupCode(candidate)) {
                return candidate;
            }
        }
        long fallback = System.currentTimeMillis() % 10000L;
        return String.format("PK%s%04d", datePart, fallback);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private User resolveCurrentUser(String currentUsername) {
        if (currentUsername == null || currentUsername.isBlank()) {
            throw new AccessDeniedException("Yêu cầu đăng nhập để thực hiện thao tác này");
        }
        return userRepository.findByUsername(currentUsername.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + currentUsername));
    }
}
