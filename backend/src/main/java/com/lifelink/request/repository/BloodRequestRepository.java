package com.lifelink.request.repository;

import com.lifelink.request.model.BloodRequest;
import com.lifelink.request.model.enums.RequestStatus;
import com.lifelink.request.model.enums.RequestUrgency;
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
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    Optional<BloodRequest> findByRequestReference(String requestReference);

    Page<BloodRequest> findByHospitalIdOrderByCreatedAtDesc(Long hospitalFacilityId, Pageable pageable);

    Page<BloodRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status, Pageable pageable);

    @Query("""
        SELECT r FROM BloodRequest r
        WHERE r.status NOT IN ('FULFILLED', 'CANCELLED', 'REJECTED')
        ORDER BY
            CASE r.urgency
                WHEN 'CRITICAL' THEN 1
                WHEN 'URGENT' THEN 2
                ELSE 3
            END,
            r.createdAt ASC
    """)
    List<BloodRequest> findOpenRequestsPrioritized();

    @Query("""
        SELECT r FROM BloodRequest r
        WHERE r.status = 'AWAITING_BANK_RESPONSES'
          AND r.urgency = :urgency
          AND r.createdAt < :cutoff
    """)
    List<BloodRequest> findUnrespondedRequestsOlderThan(
            @Param("urgency") RequestUrgency urgency,
            @Param("cutoff") Instant cutoff
    );

    long countByStatus(RequestStatus status);
}
