package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho bảng disease_groups (Nhóm bệnh dịch tễ)
 */
@Entity
@Table(name = "disease_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "group_code", nullable = false, unique = true, length = 10)
    private String groupCode;

    @Column(name = "group_name", nullable = false, unique = true, length = 60)
    private String groupName;
}

