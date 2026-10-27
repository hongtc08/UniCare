package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Entity đại diện cho bảng handbooks (Cẩm nang y tế & Sơ cứu)
 */
@Entity
@Table(name = "handbooks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Handbook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "topic", nullable = false, length = 30)
    private String topic;

    @Column(name = "status", nullable = false, length = 10)
    @Builder.Default
    private String status = "PUBLISHED";

    @Column(name = "published_at", nullable = false)
    @Builder.Default
    private OffsetDateTime publishedAt = OffsetDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", referencedColumnName = "user_id", nullable = false)
    private ManagementStaff publisher;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = "PUBLISHED";
        }
        if (this.publishedAt == null) {
            this.publishedAt = OffsetDateTime.now();
        }
    }
}

