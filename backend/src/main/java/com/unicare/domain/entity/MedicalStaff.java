package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho bảng medical_staff (Nhân viên y tế / Bác sĩ - Mở rộng từ users)
 */
@Entity
@Table(name = "medical_staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalStaff {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "staff_code", nullable = false, unique = true, length = 10)
    private String staffCode;

    @Column(name = "title", nullable = false, length = 30)
    private String title;

    @Column(name = "specialty", nullable = false, length = 30)
    private String specialty;

    public Long getUserId() {
        return id;
    }

    public void setUserId(Long userId) {
        this.id = userId;
    }
}

