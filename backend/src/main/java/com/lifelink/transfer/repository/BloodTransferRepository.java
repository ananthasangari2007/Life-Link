package com.lifelink.transfer.repository;

import com.lifelink.transfer.model.BloodTransfer;
import com.lifelink.transfer.model.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodTransferRepository extends JpaRepository<BloodTransfer, Long> {
    Optional<BloodTransfer> findByTransferReference(String reference);
    List<BloodTransfer> findByRequestId(Long requestId);
    Page<BloodTransfer> findBySourceFacilityIdOrderByCreatedAtDesc(Long facilityId, Pageable pageable);
    Page<BloodTransfer> findByDestinationFacilityIdOrderByCreatedAtDesc(Long facilityId, Pageable pageable);
    List<BloodTransfer> findByStatus(TransferStatus status);
    long countByStatus(TransferStatus status);
}
