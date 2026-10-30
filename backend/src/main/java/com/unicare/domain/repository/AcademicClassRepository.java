package com.unicare.domain.repository;

import com.unicare.domain.entity.AcademicClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademicClassRepository extends JpaRepository<AcademicClass, Integer> {

    boolean existsByClassCodeIgnoreCase(String classCode);

    boolean existsByClassCodeIgnoreCaseAndIdNot(String classCode, Integer id);

    boolean existsByFacultyId(Integer facultyId);

    @Query("SELECT c FROM AcademicClass c " +
           "JOIN FETCH c.faculty f WHERE " +
           "(:facultyId IS NULL OR f.id = :facultyId) AND " +
           "(:search = '' OR " +
           "LOWER(c.classCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.className) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.academicYear) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.facultyName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY c.id ASC")
    List<AcademicClass> searchClasses(@Param("search") String search, @Param("facultyId") Integer facultyId);
}
