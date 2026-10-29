package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Entity đại diện cho bảng notifications (Thông báo hệ thống / Thông báo y tế)
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "topic", length = 50)
    private String topic;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "target_scope", nullable = false, length = 20)
    @Builder.Default
    private String targetScope = "ALL";

    @Column(name = "scope_value", length = 50)
    private String scopeValue;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "SENT";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.targetScope == null) {
            this.targetScope = "ALL";
        }
        if (this.status == null) {
            this.status = "SENT";
        }
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }
}

