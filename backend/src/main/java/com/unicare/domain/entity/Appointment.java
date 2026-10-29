package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Entity đại diện cho bảng appointments (Lịch hẹn khám)
 */
@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_code", nullable = false, unique = true, length = 12)
    private String appointmentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "user_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(name = "reason_symptoms", columnDefinition = "TEXT")
    private String reasonSymptoms;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "CONFIRMED";

    @Column(name = "cancel_reschedule_reason", columnDefinition = "TEXT")
    private String cancelRescheduleReason;

    @Column(name = "canceled_by", length = 20)
    private String canceledBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "is_reminded", nullable = false)
    @Builder.Default
    private Boolean isReminded = false;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = "CONFIRMED";
        }
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        if (this.isReminded == null) {
            this.isReminded = false;
        }
    }
}

