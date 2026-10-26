/**
 * Repository quản lý truy vấn dữ liệu cho thực thể Shift (Ca khám).
 */
package com.unicare.domain.repository;

import com.unicare.domain.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    @Query("SELECT s FROM Shift s JOIN FETCH s.doctor ORDER BY s.shiftDate ASC, s.startTime ASC")
    List<Shift> findAllWithDoctor();

    @Query("SELECT s FROM Shift s JOIN FETCH s.doctor WHERE s.id = :id")
    Optional<Shift> findByIdWithDoctor(@Param("id") Long id);

    @Query("SELECT COUNT(s) > 0 FROM Shift s WHERE " +
           "s.doctor.id = :doctorId AND " +
           "s.shiftDate = :shiftDate AND " +
           "s.status <> com.unicare.domain.enums.ShiftStatus.DA_HUY AND " +
           "s.startTime < :endTime AND s.endTime > :startTime")
    boolean existsOverlappingShift(
            @Param("doctorId") Long doctorId,
            @Param("shiftDate") LocalDate shiftDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("SELECT COUNT(s) > 0 FROM Shift s WHERE " +
           "s.id <> :excludeId AND " +
           "s.doctor.id = :doctorId AND " +
           "s.shiftDate = :shiftDate AND " +
           "s.status <> com.unicare.domain.enums.ShiftStatus.DA_HUY AND " +
           "s.startTime < :endTime AND s.endTime > :startTime")
    boolean existsOverlappingShiftExcludingId(
            @Param("excludeId") Long excludeId,
            @Param("doctorId") Long doctorId,
            @Param("shiftDate") LocalDate shiftDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}
