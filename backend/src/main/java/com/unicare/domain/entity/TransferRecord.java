package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Entity đại diện cho bảng transfer_records (Hồ sơ chuyển viện)
 */
@Entity
@Table(name = "transfer_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_code", nullable = false, unique = true, length = 12)
    private String recordCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkup_id", nullable = false, unique = true)
    private MedicalCheckup medicalCheckup;

    @Column(name = "target_hospital", nullable = false, length = 150)
    private String targetHospital;

    @Column(name = "reason_category", nullable = false, length = 30)
    private String reasonCategory;

    @Column(name = "transfer_reason", columnDefinition = "TEXT", nullable = false)
    private String transferReason;

    @Column(name = "current_condition", columnDefinition = "TEXT", nullable = false)
    private String currentCondition;

    @Column(name = "first_aid_notes", columnDefinition = "TEXT", nullable = false)
    private String firstAidNotes;

    @Column(name = "refusal_notes", columnDefinition = "TEXT")
    private String refusalNotes;

    @Column(name = "transferred_at", nullable = false)
    @Builder.Default
    private OffsetDateTime transferredAt = OffsetDateTime.now();

    @PrePersist
    public void prePersist() {
        if (this.transferredAt == null) {
            this.transferredAt = OffsetDateTime.now();
        }
    }
}

