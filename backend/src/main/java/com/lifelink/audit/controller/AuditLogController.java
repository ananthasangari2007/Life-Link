package com.lifelink.audit.controller;

import com.lifelink.audit.dto.AuditLogDto;
import com.lifelink.audit.service.AuditService;
import com.lifelink.common.dto.ApiResponse;
import com.lifelink.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit Logs", description = "Endpoints for inspecting system and security audit trails")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('NETWORK_ADMIN', 'AUDITOR')")
    @Operation(summary = "Get Audit Logs", description = "Returns chronological audit records (Admin and Auditor only)")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogDto>>> getAuditLogs(
            @RequestParam(required = false) Long facilityId,
            Pageable pageable
    ) {
        Page<AuditLogDto> page = facilityId != null
                ? auditService.getFacilityLogs(facilityId, pageable)
                : auditService.getAllLogs(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements())));
    }
}
