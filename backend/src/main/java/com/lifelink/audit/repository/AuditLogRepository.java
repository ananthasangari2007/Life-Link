package com.lifelink.audit.repository;

import com.lifelink.audit.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByFacilityIdOrderByTimestampDesc(Long facilityId, Pageable pageable);
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId, Pageable pageable);
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
