package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Entity đại diện cho bảng dass21_results (Kết quả test tâm lý DASS-21)
 */
@Entity
@Table(name = "dass21_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dass21Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "user_id", nullable = false)
    private Student student;

    @Column(name = "test_date", nullable = false)
    @Builder.Default
    private OffsetDateTime testDate = OffsetDateTime.now();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "answers", columnDefinition = "integer[]", nullable = false)
    private List<Integer> answers;

    @Column(name = "depression_score", nullable = false)
    private Integer depressionScore;

    @Column(name = "anxiety_score", nullable = false)
    private Integer anxietyScore;

    @Column(name = "stress_score", nullable = false)
    private Integer stressScore;

    @Column(name = "depression_level", nullable = false, length = 20)
    private String depressionLevel;

    @Column(name = "anxiety_level", nullable = false, length = 20)
    private String anxietyLevel;

    @Column(name = "stress_level", nullable = false, length = 20)
    private String stressLevel;

    @PrePersist
    public void prePersist() {
        if (this.testDate == null) {
            this.testDate = OffsetDateTime.now();
        }
    }
}

