package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho bảng medical_checkups (Phiếu khám bệnh: Đa khoa, Tâm lý, Khám định kỳ)
 */
@Entity
@Table(name = "medical_checkups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalCheckup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "checkup_code", nullable = false, unique = true, length = 12)
    private String checkupCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "user_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", referencedColumnName = "user_id", nullable = false)
    private MedicalStaff staff;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;

    @Column(name = "checkup_type", nullable = false, length = 30)
    private String checkupType;

    @Column(name = "is_emergency", nullable = false)
    @Builder.Default
    private Boolean isEmergency = false;

    @Column(name = "periodic_result", length = 20)
    private String periodicResult;

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "diagnosis_notes", columnDefinition = "TEXT")
    private String diagnosisNotes;

    @Column(name = "treatment_suggestions", columnDefinition = "TEXT")
    private String treatmentSuggestions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_group_id")
    private DiseaseGroup diseaseGroup;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @OneToMany(mappedBy = "medicalCheckup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PrescribedMedicine> prescribedMedicines = new ArrayList<>();

    @OneToOne(mappedBy = "medicalCheckup", cascade = CascadeType.ALL, orphanRemoval = true)
    private TransferRecord transferRecord;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
        if (this.status == null) {
            this.status = "DRAFT";
        }
        if (this.isEmergency == null) {
            this.isEmergency = false;
        }
    }
}

