package com.unicare.application.dto.response;

import com.unicare.domain.entity.Appointment;
import com.unicare.domain.entity.MedicalStaff;
import com.unicare.domain.entity.Shift;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

/**
 * DTO trả về thông tin chi tiết của lịch hẹn khám (Appointment).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponse {

    private Long id;
    private String appointmentCode;

    // Thông tin sinh viên đặt lịch
    private Long studentId;
    private String studentCode;
    private String studentName;
    private String studentEmail;
    private String studentPhone;

    // Thông tin ca khám
    private Long shiftId;
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private ServiceType shiftType;
    private String room;
    private Integer maxSlots;
    private Integer bookedSlots;
    private ShiftStatus shiftStatus;

    // Thông tin bác sĩ phụ trách ca
    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    private String doctorSpecialty;

    // Nội dung và trạng thái cuộc hẹn
    private String reasonSymptoms;
    private String status;
    private String cancelRescheduleReason;
    private String canceledBy;
    private OffsetDateTime createdAt;
    private Boolean isReminded;

    public static AppointmentResponse fromEntity(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        AppointmentResponseBuilder builder = AppointmentResponse.builder()
                .id(appointment.getId())
                .appointmentCode(appointment.getAppointmentCode())
                .reasonSymptoms(appointment.getReasonSymptoms())
                .status(appointment.getStatus())
                .cancelRescheduleReason(appointment.getCancelRescheduleReason())
                .canceledBy(appointment.getCanceledBy())
                .createdAt(appointment.getCreatedAt())
                .isReminded(appointment.getIsReminded());

        // Sinh viên
        Student student = appointment.getStudent();
        if (student != null) {
            builder.studentId(student.getId());
            builder.studentCode(student.getStudentCode());
            User studentUser = student.getUser();
            if (studentUser != null) {
                builder.studentName(studentUser.getFullName());
                builder.studentEmail(studentUser.getEmail());
                builder.studentPhone(studentUser.getPhoneNumber());
            }
        }

        // Ca khám & Bác sĩ
        Shift shift = appointment.getShift();
        if (shift != null) {
            builder.shiftId(shift.getId())
                    .shiftDate(shift.getShiftDate())
                    .startTime(shift.getStartTime())
                    .endTime(shift.getEndTime())
                    .shiftType(shift.getShiftType())
                    .room(shift.getRoom())
                    .maxSlots(shift.getMaxSlots())
                    .bookedSlots(shift.getBookedSlots())
                    .shiftStatus(shift.getStatus());

            MedicalStaff staff = shift.getStaff();
            if (staff != null) {
                builder.doctorId(staff.getId());
                builder.doctorTitle(staff.getTitle());
                builder.doctorSpecialty(staff.getSpecialty());
                User staffUser = staff.getUser();
                if (staffUser != null) {
                    builder.doctorName(staffUser.getFullName());
                }
            }
        }

        return builder.build();
    }
}
