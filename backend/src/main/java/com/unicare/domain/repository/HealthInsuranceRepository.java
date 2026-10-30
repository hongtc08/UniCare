package com.unicare.domain.repository;

import com.unicare.domain.entity.HealthInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthInsuranceRepository extends JpaRepository<HealthInsurance, Long> {

    Optional<HealthInsurance> findByCardNumberIgnoreCase(String cardNumber);

    Optional<HealthInsurance> findByStudentId(Long studentId);

    boolean existsByCardNumberIgnoreCase(String cardNumber);

    boolean existsByCardNumberIgnoreCaseAndIdNot(String cardNumber, Long id);

    boolean existsByStudentId(Long studentId);

    boolean existsByStudentIdAndIdNot(Long studentId, Long id);

    @Query("SELECT h FROM HealthInsurance h " +
           "JOIN FETCH h.student s " +
           "JOIN FETCH s.user u " +
           "WHERE (:search = '' OR " +
           "LOWER(h.cardNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(h.initialHospital) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY h.id DESC")
    List<HealthInsurance> searchHealthInsurances(@Param("search") String search);

    @Query("SELECT h FROM HealthInsurance h " +
           "JOIN FETCH h.student s " +
           "JOIN FETCH s.user u " +
           "WHERE h.id = :id")
    Optional<HealthInsurance> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT h FROM HealthInsurance h " +
           "JOIN FETCH h.student s " +
           "JOIN FETCH s.user u " +
           "WHERE s.id = :studentId")
    Optional<HealthInsurance> findByStudentIdWithDetails(@Param("studentId") Long studentId);
}

