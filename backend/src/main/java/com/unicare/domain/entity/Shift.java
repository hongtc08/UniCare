/**
 * Entity ánh xạ bảng shifts lưu trữ thông tin ca khám trong tuần theo Schema SQL chuẩn.
 */
package com.unicare.domain.entity;

import com.unicare.domain.enums.ServiceType;
import com.unicare.domain.enums.ShiftStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "shifts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", referencedColumnName = "user_id", nullable = false)
    private MedicalStaff staff;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false, length = 30)
    private ServiceType shiftType;

    @Column(name = "room", length = 100)
    private String room;

    @Column(name = "max_slots", nullable = false)
    private Integer maxSlots;

    @Column(name = "booked_slots", nullable = false)
    @Builder.Default
    private Integer bookedSlots = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ShiftStatus status = ShiftStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Shift(
            MedicalStaff staff,
            LocalDate shiftDate,
            LocalTime startTime,
            LocalTime endTime,
            ServiceType shiftType,
            String room,
            Integer maxSlots
    ) {
        this.staff = staff;
        this.shiftDate = shiftDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.shiftType = shiftType;
        this.room = room;
        this.maxSlots = maxSlots;
        this.bookedSlots = 0;
        this.status = ShiftStatus.OPEN;
    }

    @PrePersist
    public void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.bookedSlots == null) {
            this.bookedSlots = 0;
        }
        if (this.status == null) {
            this.status = ShiftStatus.OPEN;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // Helper compatibility methods
    public User getDoctor() {
        return staff != null ? staff.getUser() : null;
    }

    public ServiceType getServiceType() {
        return shiftType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.shiftType = serviceType;
    }

    public Integer getMaxQuantity() {
        return maxSlots;
    }

    public void setMaxQuantity(Integer maxQuantity) {
        this.maxSlots = maxQuantity;
    }

    public Integer getBookedQuantity() {
        return bookedSlots;
    }

    public void setBookedQuantity(Integer bookedQuantity) {
        this.bookedSlots = bookedQuantity;
    }
}
