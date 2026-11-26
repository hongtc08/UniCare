package com.unicare.domain.repository;

import com.unicare.domain.entity.Dass21Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repository thao tác dữ liệu bảng dass21_results (Kết quả bài đánh giá tâm lý DASS-21 của sinh viên).
 */
@Repository
public interface Dass21ResultRepository extends JpaRepository<Dass21Result, Long> {

    List<Dass21Result> findByStudentIdOrderByTestDateDesc(Long studentId);

    @Query("SELECT d FROM Dass21Result d " +
           "JOIN FETCH d.student s " +
           "JOIN FETCH s.user u " +
           "WHERE s.id = :studentId " +
           "ORDER BY d.testDate DESC, d.id DESC")
    List<Dass21Result> findByStudentIdWithStudentOrderByTestDateDesc(@Param("studentId") Long studentId);

    @Query("SELECT d FROM Dass21Result d " +
           "JOIN FETCH d.student s " +
           "JOIN FETCH s.user u " +
           "WHERE s.id = :studentId " +
           "AND d.testDate >= :startTime " +
           "AND d.testDate < :endTime " +
           "ORDER BY d.testDate DESC, d.id DESC")
    List<Dass21Result> findByStudentIdAndTestDateRange(
            @Param("studentId") Long studentId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime
    );
}
