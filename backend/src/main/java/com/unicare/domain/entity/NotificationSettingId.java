package com.unicare.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

/**
 * Composite Primary Key cho bảng notification_settings
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class NotificationSettingId implements Serializable {

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "notification_type", length = 30)
    private String notificationType;
}

