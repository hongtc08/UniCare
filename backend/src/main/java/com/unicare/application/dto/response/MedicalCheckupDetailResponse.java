package com.unicare.application.dto.response;

import com.unicare.domain.entity.AcademicClass;
import com.unicare.domain.entity.MedicalCheckup;
import com.unicare.domain.entity.MedicalStaff;
import com.unicare.domain.entity.Student;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Chi tiết Phiếu khám bệnh và Đơn thuốc (nếu có).
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
    private String periodicResult;

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

    // Dịch tễ & Lịch hẹn
    private Integer diseaseGroupId;
    private String diseaseGroupName;
    private Long appointmentId;
    private String appointmentCode;

    // Đơn thuốc (rỗng nếu không kê thuốc)
    @Builder.Default
    private List<PrescribedMedicineResponse> prescribedMedicines = Collections.emptyList();

    public static MedicalCheckupDetailResponse fromEntity(MedicalCheckup entity) {
        if (entity == null) {
            return null;
        }

        Student student = entity.getStudent();
        MedicalStaff staff = entity.getStaff();
        AcademicClass academicClass = (student != null) ? student.getAcademicClass() : null;

        return MedicalCheckupDetailResponse.builder()
                .id(entity.getId())
                .checkupCode(entity.getCheckupCode())
                .checkupType(entity.getCheckupType())
                .isEmergency(entity.getIsEmergency())
                .periodicResult(entity.getPeriodicResult())
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
                .appointmentId(entity.getAppointment() != null ? entity.getAppointment().getId() : null)
                .appointmentCode(entity.getAppointment() != null ? entity.getAppointment().getAppointmentCode() : null)
                .prescribedMedicines(entity.getPrescribedMedicines() != null
                        ? entity.getPrescribedMedicines().stream().map(PrescribedMedicineResponse::fromEntity).toList()
                        : Collections.emptyList())
                .build();
    }
}
