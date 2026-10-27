package com.lifelink.request.repository;

import com.lifelink.request.model.BloodRequestResponse;
import com.lifelink.request.model.enums.BankResponseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BloodRequestResponseRepository extends JpaRepository<BloodRequestResponse, Long> {
    List<BloodRequestResponse> findByRequestId(Long requestId);
    Optional<BloodRequestResponse> findByRequestIdAndBloodBankId(Long requestId, Long bloodBankId);
    Page<BloodRequestResponse> findByBloodBankIdAndStatus(Long bloodBankId, BankResponseStatus status, Pageable pageable);
    Page<BloodRequestResponse> findByBloodBankIdOrderByCreatedAtDesc(Long bloodBankId, Pageable pageable);
    long countByBloodBankIdAndStatus(Long bloodBankId, BankResponseStatus status);
}
