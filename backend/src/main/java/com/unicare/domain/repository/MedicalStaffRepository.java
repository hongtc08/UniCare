/**
 * Repository quản lý truy vấn dữ liệu cho thực thể MedicalStaff (Nhân viên y tế / Bác sĩ).
 */
package com.unicare.domain.repository;

import com.unicare.domain.entity.MedicalStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalStaffRepository extends JpaRepository<MedicalStaff, Long> {

    Optional<MedicalStaff> findByStaffCode(String staffCode);
}
