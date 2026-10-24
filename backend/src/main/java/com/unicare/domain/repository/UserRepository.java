package com.unicare.domain.repository;

import com.unicare.domain.entity.User;
import com.unicare.domain.enums.Role;
import com.unicare.domain.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    @Query("SELECT u FROM User u WHERE " +
       "(:search = '' OR " +
       "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
       "(:role IS NULL OR u.role = :role) AND " +
       "(:status IS NULL OR u.status = :status) " +
       "ORDER BY u.id DESC")
    List<User> searchUsers(@Param("search") String search,
                           @Param("role") Role role,
                           @Param("status") UserStatus status);
}
