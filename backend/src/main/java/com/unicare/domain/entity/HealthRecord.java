package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entity đại diện cho bảng health_records (Hồ sơ sức khỏe sinh viên)
 */
@Entity
@Table(name = "health_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private Student student;

    @Column(name = "height", nullable = false, precision = 5, scale = 1)
    private BigDecimal height;

    @Column(name = "weight", nullable = false, precision = 5, scale = 1)
    private BigDecimal weight;

    @Column(name = "blood_type", length = 5)
    private String bloodType;

    @Column(name = "bmi", precision = 4, scale = 1)
    private BigDecimal bmi;

    @Column(name = "bmi_classification", length = 20)
    private String bmiClassification;

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "underlying_diseases", columnDefinition = "TEXT")
    private String underlyingDiseases;

    @Column(name = "vaccination_history", columnDefinition = "TEXT")
    private String vaccinationHistory;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setStudent(User user) {
        if (user != null) {
            this.student = Student.builder()
                    .id(user.getId())
                    .user(user)
                    .studentCode(user.getUsername())
                    .build();
        } else {
            this.student = null;
        }
    }
}
