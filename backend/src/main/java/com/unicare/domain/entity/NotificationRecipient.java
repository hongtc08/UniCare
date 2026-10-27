package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Entity đại diện cho bảng notification_recipients (Chi tiết người nhận thông báo)
 */
@Entity
@Table(
    name = "notification_recipients",
    uniqueConstraints = @UniqueConstraint(name = "uq_notification_user", columnNames = {"notification_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @Column(name = "is_pushed", nullable = false)
    @Builder.Default
    private Boolean isPushed = false;

    @PrePersist
    public void prePersist() {
        if (this.isRead == null) {
            this.isRead = false;
        }
        if (this.isPushed == null) {
            this.isPushed = false;
        }
    }
}

