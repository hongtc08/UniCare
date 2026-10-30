package com.unicare.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho bảng classes (Lớp học)
 */
@Entity
@Table(name = "classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "class_code", nullable = false, unique = true, length = 10)
    private String classCode;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    public AcademicClass(String classCode, String className, String academicYear, Faculty faculty) {
        this.classCode = classCode;
        this.className = className;
        this.academicYear = academicYear;
        this.faculty = faculty;
    }
}

