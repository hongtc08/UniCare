package com.unicare.application.service.impl;

import com.unicare.application.dto.request.CreateMedicalCheckupRequest;
import com.unicare.application.dto.request.CreateTransferRecordRequest;
import com.unicare.application.dto.request.PrescribedMedicineRequest;
import com.unicare.application.dto.request.UpdateEmergencyProgressRequest;
import com.unicare.application.dto.request.UpdateMedicalCheckupRequest;
import com.unicare.application.dto.request.UpdatePrescriptionRequest;
import com.unicare.application.dto.response.EmergencyCasePageResponse;
import com.unicare.application.dto.response.EmergencyStatsResponse;
import com.unicare.application.dto.response.MedicalCheckupDetailResponse;
import com.unicare.application.dto.response.StudentPersonalInfoResponse;
import com.unicare.application.dto.response.TransferReferralPrintResponse;
import com.unicare.application.service.MedicalCheckupService;
import com.unicare.common.exception.BadRequestException;
import com.unicare.common.exception.DuplicateResourceException;
import com.unicare.common.exception.ResourceNotFoundException;
import com.unicare.domain.entity.Appointment;
import com.unicare.domain.entity.DiseaseGroup;
import com.unicare.domain.entity.HealthInsurance;
import com.unicare.domain.entity.HealthRecord;
import com.unicare.domain.entity.MedicalCheckup;
import com.unicare.domain.entity.MedicalStaff;
import com.unicare.domain.entity.PrescribedMedicine;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.TransferRecord;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.repository.AppointmentRepository;
import com.unicare.domain.repository.DiseaseGroupRepository;
import com.unicare.domain.repository.HealthInsuranceRepository;
import com.unicare.domain.repository.HealthRecordRepository;
import com.unicare.domain.repository.MedicalCheckupRepository;
import com.unicare.domain.repository.MedicalStaffRepository;
import com.unicare.domain.repository.StudentRepository;
import com.unicare.domain.repository.TransferRecordRepository;
import com.unicare.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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

    public static final String STEP_TIEP_NHAN = "TIEP_NHAN";
    public static final String STEP_SO_CUU = "SO_CUU";
    public static final String STEP_CHUYEN_VIEN = "CHUYEN_VIEN";
    public static final String STEP_KET_THUC = "KET_THUC";

    public static final String REASON_QUA_TAI = "QUA_TAI";
    public static final String REASON_THIEU_THIET_BI = "THIEU_THIET_BI";
    public static final String REASON_CAP_CUU_NANG = "CAP_CUU_NANG";

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter CODE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Comparator<MedicalCheckup> TIMELINE_COMPARATOR = Comparator
            .comparing(
                    (MedicalCheckup mc) -> mc.getCompletedAt() != null ? mc.getCompletedAt() : mc.getCreatedAt(),
                    Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(MedicalCheckup::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(MedicalCheckup::getId, Comparator.nullsLast(Comparator.reverseOrder()));

    private final MedicalCheckupRepository medicalCheckupRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final StudentRepository studentRepository;
    private final MedicalStaffRepository medicalStaffRepository;
    private final DiseaseGroupRepository diseaseGroupRepository;
    private final TransferRecordRepository transferRecordRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final HealthInsuranceRepository healthInsuranceRepository;

    public MedicalCheckupServiceImpl(
            MedicalCheckupRepository medicalCheckupRepository,
            UserRepository userRepository) {
        this(medicalCheckupRepository, userRepository, null, null, null, null, null, null, null);
    }

    public MedicalCheckupServiceImpl(
            MedicalCheckupRepository medicalCheckupRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            StudentRepository studentRepository,
            MedicalStaffRepository medicalStaffRepository,
            DiseaseGroupRepository diseaseGroupRepository) {
        this(
                medicalCheckupRepository,
                userRepository,
                appointmentRepository,
                studentRepository,
                medicalStaffRepository,
                diseaseGroupRepository,
                null,
                null,
                null
        );
    }

    @Autowired
    public MedicalCheckupServiceImpl(
            MedicalCheckupRepository medicalCheckupRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            StudentRepository studentRepository,
            MedicalStaffRepository medicalStaffRepository,
            DiseaseGroupRepository diseaseGroupRepository,
            TransferRecordRepository transferRecordRepository,
            HealthRecordRepository healthRecordRepository,
            HealthInsuranceRepository healthInsuranceRepository) {
        this.medicalCheckupRepository = medicalCheckupRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.studentRepository = studentRepository;
        this.medicalStaffRepository = medicalStaffRepository;
        this.diseaseGroupRepository = diseaseGroupRepository;
        this.transferRecordRepository = transferRecordRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.healthInsuranceRepository = healthInsuranceRepository;
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

        String emergencyLevel = isEmergency ? normalizeEmergencyLevel(request.getEmergencyLevel()) : null;
        String emergencyType = isEmergency ? normalizeEmergencyType(request.getEmergencyType()) : null;
        String reporterContact = isEmergency ? normalizeReporterContact(request.getReporterContact()) : null;
        String currentStep = isEmergency
                ? (request.getCurrentStep() != null && !request.getCurrentStep().isBlank()
                        ? normalizeEmergencyStep(request.getCurrentStep())
                        : STEP_TIEP_NHAN)
                : null;

        MedicalCheckup checkup = MedicalCheckup.builder()
                .checkupCode(generateUniqueCheckupCode())
                .student(student)
                .staff(currentStaff)
                .appointment(appointment)
                .checkupType(checkupType)
                .isEmergency(isEmergency)
                .emergencyLevel(emergencyLevel)
                .emergencyType(emergencyType)
                .reporterContact(reporterContact)
                .currentStep(currentStep)
                .periodicResult(periodicResult)
                .symptoms(initialSymptoms)
                .diagnosisNotes(trimToNull(request.getDiagnosisNotes()))
                .treatmentSuggestions(trimToNull(request.getTreatmentSuggestions()))
                .diseaseGroup(diseaseGroup)
                .status(STATUS_DRAFT)
                .createdAt(OffsetDateTime.now())
                .build();

        if (STATUS_COMPLETED.equals(targetStatus) || STEP_KET_THUC.equals(currentStep)) {
            if (isEmergency) {
                checkup.setCurrentStep(STEP_KET_THUC);
            }
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

        if (Boolean.TRUE.equals(checkup.getIsEmergency())) {
            if (request.getEmergencyLevel() != null) {
                checkup.setEmergencyLevel(normalizeEmergencyLevel(request.getEmergencyLevel()));
            }
            if (request.getEmergencyType() != null) {
                checkup.setEmergencyType(normalizeEmergencyType(request.getEmergencyType()));
            }
            if (request.getReporterContact() != null) {
                checkup.setReporterContact(normalizeReporterContact(request.getReporterContact()));
            }
            if (request.getCurrentStep() != null && !request.getCurrentStep().isBlank()) {
                checkup.setCurrentStep(normalizeEmergencyStep(request.getCurrentStep()));
            } else if (checkup.getCurrentStep() == null) {
                checkup.setCurrentStep(STEP_TIEP_NHAN);
            }
        }

        checkup.setSymptoms(trimToNull(request.getSymptoms()));
        checkup.setDiagnosisNotes(trimToNull(request.getDiagnosisNotes()));
        checkup.setTreatmentSuggestions(trimToNull(request.getTreatmentSuggestions()));
        checkup.setDiseaseGroup(resolveDiseaseGroup(request.getDiseaseGroupId()));

        if (request.getPeriodicResult() != null) {
            checkup.setPeriodicResult(normalizePeriodicResult(request.getPeriodicResult()));
        }

        String targetStatus = normalizeStatus(request.getStatus());
        if (STATUS_COMPLETED.equals(targetStatus) || STEP_KET_THUC.equals(checkup.getCurrentStep())) {
            if (Boolean.TRUE.equals(checkup.getIsEmergency()) && !STEP_CHUYEN_VIEN.equals(checkup.getCurrentStep())) {
                checkup.setCurrentStep(STEP_KET_THUC);
            }
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

        if (Boolean.TRUE.equals(checkup.getIsEmergency()) && !STEP_CHUYEN_VIEN.equals(checkup.getCurrentStep())) {
            checkup.setCurrentStep(STEP_KET_THUC);
        }
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

    @Override
    @Transactional(readOnly = true)
    public List<MedicalCheckupDetailResponse> getStudentMedicalHistory(String currentUsername, Long requestedStudentId) {
        User currentUser = resolveCurrentUser(currentUsername);

        // Chỉ tài khoản Sinh viên mới được truy cập endpoint lịch sử khám cá nhân
        if (currentUser.getRole() != Role.SINH_VIEN) {
            throw new AccessDeniedException("Chỉ tài khoản sinh viên mới có quyền xem lịch sử khám cá nhân");
        }

        // Chặn IDOR nếu request cố tình truyền studentId khác với sinh viên đang đăng nhập
        if (requestedStudentId != null && !requestedStudentId.equals(currentUser.getId())) {
            throw new AccessDeniedException("Sinh viên tuyệt đối không được phép xem lịch sử khám của sinh viên khác");
        }

        List<MedicalCheckup> checkups = medicalCheckupRepository
                .findCompletedTimelineByStudentIdWithDetails(currentUser.getId());
        if (checkups == null || checkups.isEmpty()) {
            return Collections.emptyList();
        }

        // Lọc bảo vệ kép đảm bảo chỉ trả về phiếu đã COMPLETED của chính sinh viên đang đăng nhập
        return checkups.stream()
                .filter(mc -> mc.isCompleted()
                        && mc.getStudent() != null
                        && currentUser.getId().equals(mc.getStudent().getId()))
                .sorted(TIMELINE_COMPARATOR)
                .map(MedicalCheckupDetailResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public MedicalCheckupDetailResponse createTransferRecord(
            Long checkupId,
            CreateTransferRecordRequest request,
            String currentUsername
    ) {
        if (request == null) {
            throw new BadRequestException("Dữ liệu lập hồ sơ chuyển viện không được để trống");
        }

        String cleanHospital = trimToNull(request.getTargetHospital());
        if (cleanHospital == null) {
            throw new BadRequestException("Bệnh viện tuyến trên tiếp nhận không được để trống");
        }
        if (cleanHospital.length() > 150) {
            throw new BadRequestException("Tên bệnh viện tuyến trên không được vượt quá 150 ký tự");
        }

        String normalizedCategory = normalizeTransferReasonCategory(request.getReasonCategory());

        String cleanTransferReason = trimToNull(request.getTransferReason());
        if (cleanTransferReason == null) {
            throw new BadRequestException("Lý do chuyển viện chi tiết không được để trống");
        }

        String cleanCondition = trimToNull(request.getCurrentCondition());
        if (cleanCondition == null) {
            throw new BadRequestException("Tình trạng hiện tại của sinh viên không được để trống");
        }

        String cleanFirstAid = trimToNull(request.getFirstAidNotes());
        if (cleanFirstAid == null) {
            throw new BadRequestException("Biện pháp sơ cứu đã thực hiện không được để trống");
        }

        String cleanRefusal = trimToNull(request.getRefusalNotes());
        OffsetDateTime transferredAt = request.getTransferredAt() != null
                ? request.getTransferredAt()
                : OffsetDateTime.now();

        User currentUser = resolveCurrentUser(currentUsername);
        validateDoctorRole(currentUser, "lập hồ sơ chuyển viện");

        MedicalCheckup checkup = medicalCheckupRepository.findByIdWithDetails(checkupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu khám với ID: " + checkupId));

        validateAssignedDoctor(checkup, currentUser, "lập hồ sơ chuyển viện cho phiếu khám này");

        boolean alreadyHasTransfer = checkup.getTransferRecord() != null
                || (transferRecordRepository != null && transferRecordRepository.existsByMedicalCheckupId(checkupId));
        if (alreadyHasTransfer) {
            throw new DuplicateResourceException(
                    "Hồ sơ chuyển viện đã được xác nhận và khóa chỉ đọc, không thể lập lại hoặc chỉnh sửa");
        }

        validateCheckupEditable(checkup, "lập hồ sơ chuyển viện");

        if (checkup.getSymptoms() == null || checkup.getSymptoms().isBlank()) {
            checkup.setSymptoms(cleanCondition);
        }
        if (checkup.getDiagnosisNotes() == null || checkup.getDiagnosisNotes().isBlank()) {
            checkup.setDiagnosisNotes(cleanTransferReason);
        }
        if (checkup.getTreatmentSuggestions() == null || checkup.getTreatmentSuggestions().isBlank()) {
            checkup.setTreatmentSuggestions(cleanFirstAid);
        }

        TransferRecord transferRecord = TransferRecord.builder()
                .recordCode(generateUniqueTransferRecordCode())
                .medicalCheckup(checkup)
                .targetHospital(cleanHospital)
                .reasonCategory(normalizedCategory)
                .transferReason(cleanTransferReason)
                .currentCondition(cleanCondition)
                .firstAidNotes(cleanFirstAid)
                .refusalNotes(cleanRefusal)
                .transferredAt(transferredAt)
                .build();

        if (transferRecordRepository != null) {
            transferRecord = transferRecordRepository.save(transferRecord);
        }
        checkup.setTransferRecord(transferRecord);

        if (Boolean.TRUE.equals(checkup.getIsEmergency())) {
            checkup.setCurrentStep(STEP_CHUYEN_VIEN);
        }

        // Tự động kết thúc ca khám tại trường và khóa Read-only hồ sơ
        checkup.markCompleted(transferredAt);
        Appointment appointment = checkup.getAppointment();
        if (appointment != null) {
            appointment.setStatus(APPOINTMENT_STATUS_COMPLETED);
            if (appointmentRepository != null) {
                appointmentRepository.save(appointment);
            }
        }

        MedicalCheckup savedCheckup = medicalCheckupRepository.save(checkup);
        return MedicalCheckupDetailResponse.fromEntity(savedCheckup);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferReferralPrintResponse getTransferReferralPaper(Long checkupId, String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);

        if (currentUser.getRole() == Role.BAN_QUAN_LY) {
            throw new AccessDeniedException("Ban quản lý không có quyền truy cập hồ sơ chuyển viện cá nhân của sinh viên");
        }

        MedicalCheckup checkup = medicalCheckupRepository.findByIdWithDetails(checkupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu khám với ID: " + checkupId));

        if (currentUser.getRole() == Role.SINH_VIEN) {
            if (checkup.getStudent() == null || !checkup.getStudent().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Sinh viên chỉ được phép xem hoặc xuất giấy giới thiệu chuyển viện của chính mình");
            }
        }

        TransferRecord transferRecord = checkup.getTransferRecord();
        if (transferRecord == null && transferRecordRepository != null) {
            transferRecord = transferRecordRepository.findByMedicalCheckupId(checkupId).orElse(null);
        }
        if (transferRecord == null) {
            throw new ResourceNotFoundException("Phiếu khám (ID: " + checkupId + ") chưa có hồ sơ chuyển viện");
        }

        Long studentId = checkup.getStudent() != null ? checkup.getStudent().getId() : null;
        HealthRecord healthRecord = (studentId != null && healthRecordRepository != null)
                ? healthRecordRepository.findByStudentId(studentId).orElse(null)
                : null;
        HealthInsurance insurance = (studentId != null && healthInsuranceRepository != null)
                ? healthInsuranceRepository.findByStudentIdWithDetails(studentId)
                        .or(() -> healthInsuranceRepository.findByStudentId(studentId))
                        .orElse(null)
                : null;

        return TransferReferralPrintResponse.fromEntities(checkup, transferRecord, healthRecord, insurance);
    }

    @Override
    @Transactional(readOnly = true)
    public EmergencyCasePageResponse getEmergencyCases(
            String keyword,
            String emergencyLevel,
            String emergencyType,
            String currentStep,
            String status,
            LocalDate date,
            Integer page,
            Integer size,
            String currentUsername
    ) {
        User currentUser = resolveCurrentUser(currentUsername);
        validateDoctorRole(currentUser, "tra cứu danh sách ca cấp cứu");

        String cleanKeyword = trimFilter(keyword);
        String cleanLevel = trimFilter(emergencyLevel);
        String cleanType = trimFilter(emergencyType);
        String cleanStep = trimFilter(currentStep);
        String normalizedFilterStep = cleanStep != null ? normalizeEmergencyStep(cleanStep) : null;
        String cleanStatus = trimFilter(status);

        List<MedicalCheckup> allEmergencies = medicalCheckupRepository.findEmergencyCasesWithDetails();
        List<MedicalCheckup> safeList = allEmergencies != null ? allEmergencies : Collections.emptyList();

        List<MedicalCheckupDetailResponse> filtered = safeList.stream()
                .filter(mc -> Boolean.TRUE.equals(mc.getIsEmergency()))
                .filter(mc -> matchesEmergencyKeyword(mc, cleanKeyword))
                .filter(mc -> cleanLevel == null || (mc.getEmergencyLevel() != null && mc.getEmergencyLevel().equalsIgnoreCase(cleanLevel)))
                .filter(mc -> cleanType == null || (mc.getEmergencyType() != null && mc.getEmergencyType().equalsIgnoreCase(cleanType)))
                .filter(mc -> normalizedFilterStep == null || normalizedFilterStep.equalsIgnoreCase(MedicalCheckupDetailResponse.resolveEffectiveStep(mc)))
                .filter(mc -> cleanStatus == null || (mc.getStatus() != null && mc.getStatus().equalsIgnoreCase(cleanStatus)))
                .filter(mc -> date == null || (mc.getCreatedAt() != null && date.equals(mc.getCreatedAt().atZoneSameInstant(VIETNAM_ZONE).toLocalDate())))
                .map(MedicalCheckupDetailResponse::fromEntity)
                .toList();

        int pageIndex = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 10;
        int fromIndex = Math.min(pageIndex * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        List<MedicalCheckupDetailResponse> pagedItems = filtered.subList(fromIndex, toIndex);

        return new EmergencyCasePageResponse(pagedItems, pageIndex, pageSize, filtered.size());
    }

    @Override
    @Transactional
    public MedicalCheckupDetailResponse updateEmergencyProgress(
            Long checkupId,
            UpdateEmergencyProgressRequest request,
            String currentUsername
    ) {
        if (request == null || request.getCurrentStep() == null || request.getCurrentStep().isBlank()) {
            throw new BadRequestException("Bước xử lý hiện tại (currentStep) không được để trống");
        }

        User currentUser = resolveCurrentUser(currentUsername);
        validateDoctorRole(currentUser, "cập nhật tiến độ ca cấp cứu");

        MedicalCheckup checkup = medicalCheckupRepository.findByIdWithDetails(checkupId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu khám với ID: " + checkupId));

        validateAssignedDoctor(checkup, currentUser, "cập nhật tiến độ ca cấp cứu này");
        validateCheckupEditable(checkup, "cập nhật tiến độ ca cấp cứu");

        if (!Boolean.TRUE.equals(checkup.getIsEmergency())) {
            throw new BadRequestException("Chỉ có thể cập nhật tiến độ bước xử lý cho các ca tiếp nhận khẩn cấp / cấp cứu");
        }

        String normalizedStep = normalizeEmergencyStep(request.getCurrentStep());
        checkup.setCurrentStep(normalizedStep);

        if (request.getTreatmentSuggestions() != null && !request.getTreatmentSuggestions().isBlank()) {
            checkup.setTreatmentSuggestions(request.getTreatmentSuggestions().trim());
        }
        if (request.getDiagnosisNotes() != null && !request.getDiagnosisNotes().isBlank()) {
            checkup.setDiagnosisNotes(request.getDiagnosisNotes().trim());
        }

        if (STEP_KET_THUC.equals(normalizedStep)) {
            checkup.markCompleted(OffsetDateTime.now());
        }

        MedicalCheckup savedCheckup = medicalCheckupRepository.save(checkup);
        return MedicalCheckupDetailResponse.fromEntity(savedCheckup);
    }

    @Override
    @Transactional(readOnly = true)
    public EmergencyStatsResponse getEmergencyStats(String currentUsername) {
        User currentUser = resolveCurrentUser(currentUsername);
        validateDoctorRole(currentUser, "xem thống kê ca cấp cứu");

        List<MedicalCheckup> allEmergencies = medicalCheckupRepository.findEmergencyCasesWithDetails();
        List<MedicalCheckup> safeList = allEmergencies != null ? allEmergencies : Collections.emptyList();
        LocalDate today = LocalDate.now(VIETNAM_ZONE);

        long inProgressCount = 0;
        long firstAidCount = 0;
        long transferringCount = 0;
        long completedTodayCount = 0;

        for (MedicalCheckup mc : safeList) {
            if (!Boolean.TRUE.equals(mc.getIsEmergency())) {
                continue;
            }
            String step = MedicalCheckupDetailResponse.resolveEffectiveStep(mc);
            if (!mc.isCompleted()) {
                inProgressCount++;
                if (STEP_SO_CUU.equalsIgnoreCase(step)) {
                    firstAidCount++;
                }
            }
            if (STEP_CHUYEN_VIEN.equalsIgnoreCase(step) || mc.getTransferRecord() != null) {
                transferringCount++;
            }
            if (mc.isCompleted()) {
                OffsetDateTime refTime = mc.getCompletedAt() != null ? mc.getCompletedAt() : mc.getCreatedAt();
                if (refTime != null && today.equals(refTime.atZoneSameInstant(VIETNAM_ZONE).toLocalDate())) {
                    completedTodayCount++;
                }
            }
        }

        return new EmergencyStatsResponse(inProgressCount, firstAidCount, transferringCount, completedTodayCount);
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

    private String generateUniqueTransferRecordCode() {
        String datePart = LocalDate.now().format(CODE_DATE_FORMAT);
        for (int attempt = 0; attempt < 20; attempt++) {
            int rand = SECURE_RANDOM.nextInt(10000);
            String candidate = String.format("CV%s%04d", datePart, rand);
            if (transferRecordRepository == null || !transferRecordRepository.existsByRecordCode(candidate)) {
                return candidate;
            }
        }
        long fallback = System.currentTimeMillis() % 10000L;
        return String.format("CV%s%04d", datePart, fallback);
    }

    private String normalizeTransferReasonCategory(String rawCategory) {
        if (rawCategory == null || rawCategory.isBlank()) {
            throw new BadRequestException("Phân loại lý do chuyển viện không được để trống");
        }
        String upper = rawCategory.trim().toUpperCase(Locale.ROOT);
        if (REASON_QUA_TAI.equals(upper) || "OVERLOAD".equals(upper)) {
            return REASON_QUA_TAI;
        }
        if (REASON_THIEU_THIET_BI.equals(upper) || "LACK_OF_EQUIPMENT".equals(upper)) {
            return REASON_THIEU_THIET_BI;
        }
        if (REASON_CAP_CUU_NANG.equals(upper) || "SEVERE_EMERGENCY".equals(upper)) {
            return REASON_CAP_CUU_NANG;
        }
        throw new BadRequestException(
                "Phân loại lý do chuyển viện không hợp lệ: " + rawCategory
                        + ". Chỉ chấp nhận QUA_TAI, THIEU_THIET_BI hoặc CAP_CUU_NANG");
    }

    private String normalizeEmergencyLevel(String rawLevel) {
        String cleaned = trimToNull(rawLevel);
        if (cleaned == null) {
            return null;
        }
        if (cleaned.length() > 20) {
            throw new BadRequestException("Mức độ khẩn cấp không được vượt quá 20 ký tự");
        }
        return cleaned.toUpperCase(Locale.ROOT);
    }

    private String normalizeEmergencyType(String rawType) {
        String cleaned = trimToNull(rawType);
        if (cleaned == null) {
            return null;
        }
        if (cleaned.length() > 50) {
            throw new BadRequestException("Loại cấp cứu không được vượt quá 50 ký tự");
        }
        return cleaned;
    }

    private String normalizeReporterContact(String rawContact) {
        String cleaned = trimToNull(rawContact);
        if (cleaned == null) {
            return null;
        }
        if (cleaned.length() > 150) {
            throw new BadRequestException("Thông tin người báo cáo / liên hệ không được vượt quá 150 ký tự");
        }
        return cleaned;
    }

    private String normalizeEmergencyStep(String rawStep) {
        String cleaned = trimToNull(rawStep);
        if (cleaned == null) {
            throw new BadRequestException("Bước xử lý hiện tại không được để trống");
        }
        String upper = cleaned.toUpperCase(Locale.ROOT);
        return switch (upper) {
            case STEP_TIEP_NHAN, "INTAKE", "DA_TIEP_NHAN", "RECEIVED" -> STEP_TIEP_NHAN;
            case STEP_SO_CUU, "FIRST_AID", "DANG_SO_CUU", "INITIAL_FIRST_AID" -> STEP_SO_CUU;
            case STEP_CHUYEN_VIEN, "TRANSFER", "TRANSFERRING", "DANG_CHUYEN_VIEN", "EXTERNAL_TRANSFER" -> STEP_CHUYEN_VIEN;
            case STEP_KET_THUC, "COMPLETED", "DONE", "FINISHED", "DA_KET_THUC", "END" -> STEP_KET_THUC;
            default -> throw new BadRequestException(
                    "Bước xử lý cấp cứu không hợp lệ: " + rawStep
                            + ". Chỉ chấp nhận TIEP_NHAN, SO_CUU, CHUYEN_VIEN hoặc KET_THUC");
        };
    }

    private boolean matchesEmergencyKeyword(MedicalCheckup mc, String cleanKeyword) {
        if (cleanKeyword == null) {
            return true;
        }
        String q = cleanKeyword.toLowerCase(Locale.ROOT);
        if (mc.getCheckupCode() != null && mc.getCheckupCode().toLowerCase(Locale.ROOT).contains(q)) {
            return true;
        }
        Student student = mc.getStudent();
        if (student != null) {
            if (student.getStudentCode() != null && student.getStudentCode().toLowerCase(Locale.ROOT).contains(q)) {
                return true;
            }
            if (student.getUser() != null && student.getUser().getFullName() != null
                    && student.getUser().getFullName().toLowerCase(Locale.ROOT).contains(q)) {
                return true;
            }
        }
        return false;
    }

    private String trimFilter(String value) {
        String cleaned = trimToNull(value);
        if (cleaned == null || "ALL".equalsIgnoreCase(cleaned)) {
            return null;
        }
        return cleaned;
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
