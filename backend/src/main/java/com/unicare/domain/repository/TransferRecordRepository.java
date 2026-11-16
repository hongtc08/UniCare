package com.unicare.domain.repository;

import com.unicare.domain.entity.TransferRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransferRecordRepository extends JpaRepository<TransferRecord, Long> {

    Optional<TransferRecord> findByMedicalCheckupId(Long checkupId);

    boolean existsByRecordCode(String recordCode);

    boolean existsByMedicalCheckupId(Long checkupId);
}
