package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Entity đại diện cho bảng mood_logs (Nhật ký tâm trạng hằng ngày của sinh viên)
 */
@Entity
@Table(name = "mood_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "user_id", nullable = false)
    private Student student;

    @Column(name = "log_time", nullable = false)
    @Builder.Default
    private OffsetDateTime logTime = OffsetDateTime.now();

    @Column(name = "mood_level", nullable = false)
    private Integer moodLevel;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    public void prePersist() {
        if (this.logTime == null) {
            this.logTime = OffsetDateTime.now();
        }
    }
}

