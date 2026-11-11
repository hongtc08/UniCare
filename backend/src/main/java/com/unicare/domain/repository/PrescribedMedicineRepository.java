package com.unicare.domain.repository;

import com.unicare.domain.entity.PrescribedMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescribedMedicineRepository extends JpaRepository<PrescribedMedicine, Long> {

    List<PrescribedMedicine> findByMedicalCheckupId(Long checkupId);

    void deleteByMedicalCheckupId(Long checkupId);
}
