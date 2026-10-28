package com.lifelink.facility.repository;

import com.lifelink.facility.model.Facility;
import com.lifelink.facility.model.enums.FacilityStatus;
import com.lifelink.facility.model.enums.FacilityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    Optional<Facility> findByLicenseNumber(String licenseNumber);
    boolean existsByLicenseNumber(String licenseNumber);
    List<Facility> findByTypeAndStatus(FacilityType type, FacilityStatus status);
    List<Facility> findByStatus(FacilityStatus status);
    Page<Facility> findByStatus(FacilityStatus status, Pageable pageable);
    Page<Facility> findByType(FacilityType type, Pageable pageable);
    long countByStatus(FacilityStatus status);
}
