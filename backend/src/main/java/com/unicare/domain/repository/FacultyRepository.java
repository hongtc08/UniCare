package com.unicare.domain.repository;

import com.unicare.domain.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Integer> {

    boolean existsByFacultyCodeIgnoreCase(String facultyCode);

    boolean existsByFacultyNameIgnoreCase(String facultyName);

    boolean existsByFacultyCodeIgnoreCaseAndIdNot(String facultyCode, Integer id);

    boolean existsByFacultyNameIgnoreCaseAndIdNot(String facultyName, Integer id);

    @Query("SELECT f FROM Faculty f WHERE " +
           "(:search = '' OR " +
           "LOWER(f.facultyCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(f.facultyName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY f.id ASC")
    List<Faculty> searchFaculties(@Param("search") String search);
}
