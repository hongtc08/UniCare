package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho bảng management_staff (Ban quản lý / Quản trị viên - Mở rộng từ users)
 */
@Entity
@Table(name = "management_staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagementStaff {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "staff_code", nullable = false, unique = true, length = 10)
    private String staffCode;

    public Long getUserId() {
        return id;
    }

    public void setUserId(Long userId) {
        this.id = userId;
    }
}

