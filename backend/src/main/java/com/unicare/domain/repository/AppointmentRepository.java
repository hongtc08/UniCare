package com.unicare.domain.repository;

import com.unicare.domain.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.shift s " +
           "JOIN FETCH s.staff st " +
           "JOIN FETCH st.user u " +
           "JOIN FETCH a.student std " +
           "JOIN FETCH std.user su " +
           "WHERE a.id = :id")
    Optional<Appointment> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.shift s " +
           "JOIN FETCH s.staff st " +
           "JOIN FETCH st.user u " +
           "JOIN FETCH a.student std " +
           "JOIN FETCH std.user su " +
           "WHERE std.id = :studentId " +
           "ORDER BY s.shiftDate DESC, s.startTime DESC, a.id DESC")
    List<Appointment> findByStudentIdWithDetails(@Param("studentId") Long studentId);

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.shift s " +
           "JOIN FETCH s.staff st " +
           "JOIN FETCH st.user u " +
           "JOIN FETCH a.student std " +
           "JOIN FETCH std.user su " +
           "WHERE std.id = :studentId AND UPPER(a.status) = UPPER(:status) " +
           "ORDER BY s.shiftDate DESC, s.startTime DESC, a.id DESC")
    List<Appointment> findByStudentIdAndStatusWithDetails(
            @Param("studentId") Long studentId,
            @Param("status") String status
    );

    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
           "WHERE a.student.id = :studentId AND a.shift.id = :shiftId AND UPPER(a.status) = UPPER(:status)")
    boolean existsByStudentIdAndShiftIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("shiftId") Long shiftId,
            @Param("status") String status
    );

    Optional<Appointment> findByAppointmentCode(String appointmentCode);
}

