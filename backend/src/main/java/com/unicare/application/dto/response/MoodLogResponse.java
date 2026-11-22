package com.unicare.application.dto.response;

import com.unicare.domain.entity.MoodLog;
import com.unicare.domain.entity.Student;
import com.unicare.domain.entity.User;
import com.unicare.domain.enums.MoodLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * DTO trả về thông tin chi tiết một bản ghi nhật ký tâm trạng.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodLogResponse {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private Long id;
    private Long studentId;
    private String studentCode;
    private String studentName;
    private Integer moodLevel;
    private String moodLabel;
    private String notes;
    private OffsetDateTime logTime;
    private LocalDate logDate;

    public static MoodLogResponse fromEntity(MoodLog entity) {
        if (entity == null) {
            return null;
        }

        Student student = entity.getStudent();
        User user = student != null ? student.getUser() : null;

        Long studentId = student != null ? student.getId() : null;
        String studentCode = null;
        if (student != null && student.getStudentCode() != null && !student.getStudentCode().isBlank()) {
            studentCode = student.getStudentCode();
        } else if (user != null) {
            studentCode = user.getUsername();
        }
        String studentName = user != null ? user.getFullName() : null;

        OffsetDateTime logTime = entity.getLogTime();
        LocalDate logDate = logTime != null
                ? logTime.atZoneSameInstant(VIETNAM_ZONE).toLocalDate()
                : null;

        return MoodLogResponse.builder()
                .id(entity.getId())
                .studentId(studentId)
                .studentCode(studentCode)
                .studentName(studentName)
                .moodLevel(entity.getMoodLevel())
                .moodLabel(MoodLevel.labelOf(entity.getMoodLevel()))
                .notes(entity.getNotes())
                .logTime(logTime)
                .logDate(logDate)
                .build();
    }
}
