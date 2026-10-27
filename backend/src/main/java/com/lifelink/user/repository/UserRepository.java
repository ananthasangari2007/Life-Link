package com.lifelink.user.repository;

import com.lifelink.user.model.User;
import com.lifelink.user.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByFacilityId(Long facilityId);
    List<User> findByRole(UserRole role);
    Page<User> findByFacilityId(Long facilityId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.facility.id = :facilityId AND u.active = true")
    List<User> findActiveUsersInFacility(@Param("facilityId") Long facilityId);
}
