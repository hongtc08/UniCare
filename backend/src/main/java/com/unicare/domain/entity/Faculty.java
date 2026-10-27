package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho bảng faculties (Khoa đào tạo)
 */
@Entity
@Table(name = "faculties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "faculty_code", nullable = false, unique = true, length = 10)
    private String facultyCode;

    @Column(name = "faculty_name", nullable = false, unique = true, length = 100)
    private String facultyName;
}

