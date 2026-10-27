package com.lifelink.inventory.repository;

import com.lifelink.inventory.model.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByBatchIdOrderByCreatedAtDesc(Long batchId);
    Page<InventoryTransaction> findByFacilityIdOrderByCreatedAtDesc(Long facilityId, Pageable pageable);
}
