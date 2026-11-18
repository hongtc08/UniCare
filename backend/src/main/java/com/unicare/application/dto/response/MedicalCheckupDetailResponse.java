package com.unicare.application.dto.response;

import com.unicare.domain.entity.AcademicClass;
import com.unicare.domain.entity.Appointment;
import com.unicare.domain.entity.MedicalCheckup;
import com.unicare.domain.entity.MedicalStaff;
import com.unicare.domain.entity.PrescribedMedicine;
import com.unicare.domain.entity.Shift;
import com.unicare.domain.entity.Student;
import com.unicare.domain.enums.ServiceType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Chi tiết Phiếu khám bệnh kèm Đơn thuốc và Hồ sơ chuyển viện (nếu có).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalCheckupDetailResponse {

    private Long id;
    private String checkupCode;
    private String checkupType;
    private Boolean isEmergency;
    private String emergencyLevel;
    private String emergencyType;
    private String reporterContact;
    private String currentStep;
    private String currentStepLabel;
    private String periodicResult;
    private Boolean isPeriodic;
    private String timelineCategory;

    private String symptoms;
    private String diagnosisNotes;
    private String treatmentSuggestions;

    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime completedAt;

    // Sinh viên
    private Long studentId;
    private String studentCode;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private String className;
    private String facultyName;

    // Bác sĩ
    private Long staffId;
    private String staffCode;
    private String doctorName;
    private String doctorTitle;
    private String doctorSpecialty;

    // Dịch tễ & Lịch hẹn / Ca khám
    private Integer diseaseGroupId;
    private String diseaseGroupName;
    private Long appointmentId;
    private String appointmentCode;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private ServiceType shiftType;

    // Đơn thuốc (rỗng nếu không kê thuốc)
    @Builder.Default
    private List<PrescribedMedicineResponse> prescribedMedicines = Collections.emptyList();

    // Hồ sơ chuyển viện (null nếu không có chuyển viện)
    private TransferRecordResponse transferRecord;

    public static MedicalCheckupDetailResponse fromEntity(MedicalCheckup entity) {
        if (entity == null) {
            return null;
        }

        Student student = entity.getStudent();
        MedicalStaff staff = entity.getStaff();
        AcademicClass academicClass = (student != null) ? student.getAcademicClass() : null;
        Appointment appointment = entity.getAppointment();
        Shift shift = (appointment != null) ? appointment.getShift() : null;

        boolean periodic = isPeriodicCheckup(entity.getCheckupType(), entity.getPeriodicResult());
        String category = resolveTimelineCategory(entity.getCheckupType(), periodic);
        String resolvedStep = resolveEffectiveStep(entity);

        List<PrescribedMedicineResponse> medicines = entity.getPrescribedMedicines() != null
                ? entity.getPrescribedMedicines().stream()
                        .sorted(Comparator.comparing(PrescribedMedicine::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(PrescribedMedicineResponse::fromEntity)
                        .toList()
                : Collections.emptyList();

        return MedicalCheckupDetailResponse.builder()
                .id(entity.getId())
                .checkupCode(entity.getCheckupCode())
                .checkupType(entity.getCheckupType())
                .isEmergency(entity.getIsEmergency())
                .emergencyLevel(entity.getEmergencyLevel())
                .emergencyType(entity.getEmergencyType())
                .reporterContact(entity.getReporterContact())
                .currentStep(resolvedStep)
                .currentStepLabel(resolveStepLabel(resolvedStep))
                .periodicResult(entity.getPeriodicResult())
                .isPeriodic(periodic)
                .timelineCategory(category)
                .symptoms(entity.getSymptoms())
                .diagnosisNotes(entity.getDiagnosisNotes())
                .treatmentSuggestions(entity.getTreatmentSuggestions())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .completedAt(entity.getCompletedAt())
                .studentId(student != null ? student.getId() : null)
                .studentCode(student != null ? student.getStudentCode() : null)
                .studentName(student != null && student.getUser() != null ? student.getUser().getFullName() : null)
                .studentEmail(student != null && student.getUser() != null ? student.getUser().getEmail() : null)
                .studentPhone(student != null && student.getUser() != null ? student.getUser().getPhoneNumber() : null)
                .className(academicClass != null ? academicClass.getClassName() : null)
                .facultyName(academicClass != null && academicClass.getFaculty() != null ? academicClass.getFaculty().getFacultyName() : null)
                .staffId(staff != null ? staff.getId() : null)
                .staffCode(staff != null ? staff.getStaffCode() : null)
                .doctorName(staff != null && staff.getUser() != null ? staff.getUser().getFullName() : null)
                .doctorTitle(staff != null ? staff.getTitle() : null)
                .doctorSpecialty(staff != null ? staff.getSpecialty() : null)
                .diseaseGroupId(entity.getDiseaseGroup() != null ? entity.getDiseaseGroup().getId() : null)
                .diseaseGroupName(entity.getDiseaseGroup() != null ? entity.getDiseaseGroup().getGroupName() : null)
                .appointmentId(appointment != null ? appointment.getId() : null)
                .appointmentCode(appointment != null ? appointment.getAppointmentCode() : null)
                .shiftDate(shift != null ? shift.getShiftDate() : null)
                .startTime(shift != null ? shift.getStartTime() : null)
                .endTime(shift != null ? shift.getEndTime() : null)
                .room(shift != null ? shift.getRoom() : null)
                .shiftType(shift != null ? shift.getShiftType() : null)
                .prescribedMedicines(medicines)
                .transferRecord(TransferRecordResponse.fromEntity(entity.getTransferRecord()))
                .build();
    }

    // Khám định kỳ trong schema gộp chung vào medical_checkups và đánh dấu qua periodic_result
    private static boolean isPeriodicCheckup(String checkupType, String periodicResult) {
        if (periodicResult != null && !periodicResult.isBlank()) {
            return true;
        }
        return checkupType != null
                && ("PERIODIC".equalsIgnoreCase(checkupType.trim()) || "KHAM_DINH_KY".equalsIgnoreCase(checkupType.trim()));
    }

    private static String resolveTimelineCategory(String checkupType, boolean periodic) {
        if (periodic) {
            return "PERIODIC";
        }
        if (checkupType != null) {
            String upper = checkupType.trim().toUpperCase();
            if ("PSYCHOLOGICAL".equals(upper) || "TU_VAN_TAM_LY".equals(upper) || "TAM_LY".equals(upper)) {
                return "PSYCHOLOGICAL";
            }
        }
        return "GENERAL";
    }

    public static String resolveEffectiveStep(MedicalCheckup entity) {
        if (entity.getCurrentStep() != null && !entity.getCurrentStep().isBlank()) {
            return entity.getCurrentStep();
        }
        if (!Boolean.TRUE.equals(entity.getIsEmergency())) {
            return null;
        }
        if (entity.getTransferRecord() != null) {
            return "CHUYEN_VIEN";
        }
        if (entity.isCompleted()) {
            return "KET_THUC";
        }
        return "TIEP_NHAN";
    }

    public static String resolveStepLabel(String step) {
        if (step == null) {
            return null;
        }
        return switch (step.toUpperCase()) {
            case "TIEP_NHAN" -> "Đã tiếp nhận";
            case "SO_CUU" -> "Sơ cứu ban đầu";
            case "CHUYEN_VIEN" -> "Chuyển viện ngoài";
            case "KET_THUC" -> "Kết thúc ca";
            default -> step;
        };
    }
}
