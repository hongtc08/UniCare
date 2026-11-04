package com.unicare.domain.repository;

import com.unicare.domain.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentCode(String studentCode);

    boolean existsByAcademicClassId(Integer classId);

    @Query("SELECT s FROM Student s " +
           "LEFT JOIN FETCH s.user u " +
           "LEFT JOIN FETCH s.academicClass c " +
           "LEFT JOIN FETCH c.faculty f " +
           "WHERE s.id = :id")
    Optional<Student> findByIdWithDetails(@Param("id") Long id);
}
