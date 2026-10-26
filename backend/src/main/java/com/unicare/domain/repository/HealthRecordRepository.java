/**
 * Repository thao tác dữ liệu bảng health_records trong cơ sở dữ liệu.
 */
package com.unicare.domain.repository;

import com.unicare.domain.entity.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

    Optional<HealthRecord> findByStudentId(Long studentId);
}
