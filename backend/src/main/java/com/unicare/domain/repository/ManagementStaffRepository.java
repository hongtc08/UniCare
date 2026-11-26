/**
 * Repository quản lý truy vấn dữ liệu cho thực thể ManagementStaff (Ban quản lý / Quản trị viên).
 */
package com.unicare.domain.repository;

import com.unicare.domain.entity.ManagementStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagementStaffRepository extends JpaRepository<ManagementStaff, Long> {

    Optional<ManagementStaff> findByStaffCodeIgnoreCase(String staffCode);

    boolean existsByStaffCodeIgnoreCase(String staffCode);

    boolean existsByStaffCodeIgnoreCaseAndIdNot(String staffCode, Long id);
}
