package com.lifelink.inventory.repository;

import com.lifelink.inventory.model.BloodInventoryBatch;
import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import com.lifelink.inventory.model.enums.InventoryStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BloodInventoryBatchRepository extends JpaRepository<BloodInventoryBatch, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BloodInventoryBatch b WHERE b.id = :id")
    Optional<BloodInventoryBatch> findByIdForUpdate(@Param("id") Long id);

    List<BloodInventoryBatch> findByFacilityId(Long facilityId);
    Page<BloodInventoryBatch> findByFacilityId(Long facilityId, Pageable pageable);

    Optional<BloodInventoryBatch> findByFacilityIdAndBatchNumber(Long facilityId, String batchNumber);

    @Query("""
        SELECT b FROM BloodInventoryBatch b
        WHERE b.facility.id = :facilityId
          AND b.status = :status
          AND b.expiryDate > :now
        ORDER BY b.expiryDate ASC
    """)
    List<BloodInventoryBatch> findActiveBatchesByFacility(
            @Param("facilityId") Long facilityId,
            @Param("status") InventoryStatus status,
            @Param("now") Instant now
    );

    @Query("""
        SELECT b FROM BloodInventoryBatch b
        JOIN FETCH b.facility f
        WHERE f.status = 'APPROVED'
          AND b.status = 'AVAILABLE'
          AND b.componentType = :componentType
          AND b.bloodGroup IN :compatibleGroups
          AND b.expiryDate > :now
          AND (b.quantity - (b.reservedQuantity + b.dispatchedQuantity)) > 0
        ORDER BY b.expiryDate ASC
    """)
    List<BloodInventoryBatch> findAvailableBatchesForMatching(
            @Param("componentType") ComponentType componentType,
            @Param("compatibleGroups") Collection<BloodGroup> compatibleGroups,
            @Param("now") Instant now
    );

    @Query("""
        SELECT b FROM BloodInventoryBatch b
        WHERE b.facility.id = :facilityId
          AND b.status = 'AVAILABLE'
          AND b.expiryDate BETWEEN :now AND :threshold
        ORDER BY b.expiryDate ASC
    """)
    List<BloodInventoryBatch> findNearExpiryBatches(
            @Param("facilityId") Long facilityId,
            @Param("now") Instant now,
            @Param("threshold") Instant threshold
    );

    @Query("""
        SELECT b.bloodGroup, b.componentType, SUM(b.quantity - (b.reservedQuantity + b.dispatchedQuantity))
        FROM BloodInventoryBatch b
        WHERE b.status = 'AVAILABLE' AND b.expiryDate > :now
        GROUP BY b.bloodGroup, b.componentType
    """)
    List<Object[]> getNetworkAggregateStock(@Param("now") Instant now);
}
