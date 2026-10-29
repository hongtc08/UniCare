package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho bảng notification_settings (Cài đặt nhận thông báo của
 * sinh viên)
 */
@Entity
@Table(name = "notification_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSetting {

    @EmbeddedId
    private NotificationSettingId id;

    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "user_id", nullable = false)
    private Student student;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @PrePersist
    public void prePersist() {
        if (this.isEnabled == null) {
            this.isEnabled = true;
        }
    }

    public Long getStudentId() {
        return id != null ? id.getStudentId() : null;
    }

    public String getNotificationType() {
        return id != null ? id.getNotificationType() : null;
    }
}
