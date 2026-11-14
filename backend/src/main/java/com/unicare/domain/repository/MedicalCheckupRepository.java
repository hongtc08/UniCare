package com.unicare.domain.repository;

import com.unicare.domain.entity.MedicalCheckup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalCheckupRepository extends JpaRepository<MedicalCheckup, Long> {

    @Query("SELECT mc FROM MedicalCheckup mc " +
           "LEFT JOIN FETCH mc.student s " +
           "LEFT JOIN FETCH s.user su " +
           "LEFT JOIN FETCH s.academicClass ac " +
           "LEFT JOIN FETCH ac.faculty f " +
           "LEFT JOIN FETCH mc.staff st " +
           "LEFT JOIN FETCH st.user tu " +
           "LEFT JOIN FETCH mc.appointment a " +
           "LEFT JOIN FETCH mc.diseaseGroup dg " +
           "LEFT JOIN FETCH mc.prescribedMedicines pm " +
           "WHERE mc.id = :id")
    Optional<MedicalCheckup> findByIdWithDetails(@Param("id") Long id);

    Optional<MedicalCheckup> findByCheckupCode(String checkupCode);

    boolean existsByCheckupCode(String checkupCode);

    boolean existsByAppointmentId(Long appointmentId);

    @Query("SELECT mc FROM MedicalCheckup mc " +
           "LEFT JOIN FETCH mc.student s " +
           "LEFT JOIN FETCH s.user su " +
           "LEFT JOIN FETCH s.academicClass ac " +
           "LEFT JOIN FETCH ac.faculty f " +
           "LEFT JOIN FETCH mc.staff st " +
           "LEFT JOIN FETCH st.user tu " +
           "LEFT JOIN FETCH mc.appointment a " +
           "LEFT JOIN FETCH mc.diseaseGroup dg " +
           "LEFT JOIN FETCH mc.prescribedMedicines pm " +
           "WHERE mc.appointment.id = :appointmentId")
    Optional<MedicalCheckup> findByAppointmentIdWithDetails(@Param("appointmentId") Long appointmentId);

    List<MedicalCheckup> findByStudentId(Long studentId);

    List<MedicalCheckup> findByStaffId(Long staffId);

    @Query("SELECT mc FROM MedicalCheckup mc " +
           "LEFT JOIN FETCH mc.staff st " +
           "LEFT JOIN FETCH st.user tu " +
           "LEFT JOIN FETCH mc.diseaseGroup dg " +
           "LEFT JOIN FETCH mc.appointment a " +
           "WHERE mc.student.id = :studentId " +
           "ORDER BY mc.createdAt DESC")
    List<MedicalCheckup> findByStudentIdWithDetailsOrderByCreatedAtDesc(@Param("studentId") Long studentId);
}
