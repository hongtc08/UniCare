package com.unicare.domain.repository;

import com.unicare.domain.entity.DiseaseGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiseaseGroupRepository extends JpaRepository<DiseaseGroup, Integer> {

    Optional<DiseaseGroup> findByGroupCode(String groupCode);

    Optional<DiseaseGroup> findByGroupCodeIgnoreCase(String groupCode);

    boolean existsByGroupCodeIgnoreCase(String groupCode);

    boolean existsByGroupNameIgnoreCase(String groupName);

    boolean existsByGroupCodeIgnoreCaseAndIdNot(String groupCode, Integer id);

    boolean existsByGroupNameIgnoreCaseAndIdNot(String groupName, Integer id);

    @Query("SELECT dg FROM DiseaseGroup dg WHERE " +
           "(:search = '' OR " +
           "LOWER(dg.groupCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(dg.groupName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY dg.id ASC")
    List<DiseaseGroup> searchDiseaseGroups(@Param("search") String search);
}

