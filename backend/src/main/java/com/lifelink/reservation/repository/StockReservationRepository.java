package com.lifelink.reservation.repository;

import com.lifelink.reservation.model.StockReservation;
import com.lifelink.reservation.model.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
    Optional<StockReservation> findByReservationReference(String reference);
    List<StockReservation> findByRequestId(Long requestId);
    List<StockReservation> findByBatchId(Long batchId);
    Page<StockReservation> findByBloodBankIdOrderByCreatedAtDesc(Long bloodBankId, Pageable pageable);

    @Query("""
        SELECT sr FROM StockReservation sr
        WHERE sr.status = 'ACTIVE' AND sr.expiresAt < :now
    """)
    List<StockReservation> findExpiredActiveReservations(@Param("now") Instant now);

    long countByBloodBankIdAndStatus(Long bloodBankId, ReservationStatus status);
}
