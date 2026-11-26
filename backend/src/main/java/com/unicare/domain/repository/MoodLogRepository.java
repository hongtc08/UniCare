package com.unicare.domain.repository;

import com.unicare.domain.entity.MoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository thao tác dữ liệu bảng mood_logs (Nhật ký tâm trạng hằng ngày của sinh viên).
 */
@Repository
public interface MoodLogRepository extends JpaRepository<MoodLog, Long> {

    @Query("SELECT m FROM MoodLog m " +
           "JOIN FETCH m.student s " +
           "JOIN FETCH s.user u " +
           "WHERE m.id = :id")
    Optional<MoodLog> findByIdWithStudent(@Param("id") Long id);

    @Query("SELECT m FROM MoodLog m " +
           "JOIN FETCH m.student s " +
           "JOIN FETCH s.user u " +
           "WHERE m.student.id = :studentId " +
           "AND m.logTime >= :startTime " +
           "AND m.logTime < :endTime " +
           "ORDER BY m.logTime DESC, m.id DESC")
    List<MoodLog> findByStudentIdAndLogTimeRange(
            @Param("studentId") Long studentId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime
    );

    @Query("SELECT m FROM MoodLog m " +
           "JOIN FETCH m.student s " +
           "JOIN FETCH s.user u " +
           "WHERE m.student.id = :studentId " +
           "ORDER BY m.logTime DESC, m.id DESC")
    List<MoodLog> findAllByStudentIdWithStudentOrderByLogTimeDesc(@Param("studentId") Long studentId);

    boolean existsByStudentIdAndLogTimeGreaterThanEqualAndLogTimeLessThan(
            Long studentId,
            OffsetDateTime startTime,
            OffsetDateTime endTime
    );

    boolean existsByStudentIdAndLogTimeGreaterThanEqualAndLogTimeLessThanAndIdNot(
            Long studentId,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            Long id
    );
}
