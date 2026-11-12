package com.unicare.domain.repository;

import com.unicare.domain.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query("SELECT s FROM Student s " +
           "LEFT JOIN FETCH s.user u " +
           "LEFT JOIN FETCH s.academicClass c " +
           "LEFT JOIN FETCH c.faculty f " +
           "WHERE LOWER(s.studentCode) = LOWER(:studentCode)")
    Optional<Student> findByStudentCodeWithDetails(@Param("studentCode") String studentCode);

    @Query("SELECT s FROM Student s " +
           "JOIN FETCH s.user u " +
           "LEFT JOIN FETCH s.academicClass c " +
           "LEFT JOIN FETCH c.faculty f " +
           "WHERE (:keyword = '' OR " +
           "LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.classCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.className) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY s.studentCode ASC")
    List<Student> searchStudentsByKeyword(@Param("keyword") String keyword);
}
