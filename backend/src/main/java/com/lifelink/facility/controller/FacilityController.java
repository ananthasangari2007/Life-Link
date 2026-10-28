package com.lifelink.facility.controller;

import com.lifelink.common.dto.ApiResponse;
import com.lifelink.common.dto.PageResponse;
import com.lifelink.facility.dto.FacilityApprovalRequest;
import com.lifelink.facility.dto.FacilityDto;
import com.lifelink.facility.model.enums.FacilityStatus;
import com.lifelink.facility.model.enums.FacilityType;
import com.lifelink.facility.service.FacilityService;
import com.lifelink.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/facilities")
@Tag(name = "Facilities", description = "Endpoints for managing hospitals and blood banks")
public class FacilityController {

    private final FacilityService facilityService;
    private final SecurityUtils securityUtils;

    public FacilityController(FacilityService facilityService, SecurityUtils securityUtils) {
        this.facilityService = facilityService;
        this.securityUtils = securityUtils;
    }

    @GetMapping({"/public", "/public/approved"})
    @Operation(summary = "Public Approved Facilities", description = "Returns active verified hospitals and blood banks for public viewing")
    public ResponseEntity<ApiResponse<List<FacilityDto>>> getPublicApprovedFacilities(
            @RequestParam(required = false) FacilityType type
    ) {
        List<FacilityDto> list = type != null
                ? facilityService.getApprovedFacilitiesByType(type)
                : facilityService.getAllApprovedFacilities();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Facility by ID")
    public ResponseEntity<ApiResponse<FacilityDto>> getFacilityById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(facilityService.getFacilityById(id)));
    }

    @GetMapping("/my-facility")
    @Operation(summary = "Get Authenticated User's Facility")
    public ResponseEntity<ApiResponse<FacilityDto>> getMyFacility() {
        Long facilityId = securityUtils.getCurrentFacilityId()
                .orElse(null);
        if (facilityId == null) {
            return ResponseEntity.ok(ApiResponse.ok(null));
        }
        return ResponseEntity.ok(ApiResponse.ok(facilityService.getFacilityById(facilityId)));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('NETWORK_ADMIN')")
    @Operation(summary = "List Facilities by Status (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<FacilityDto>>> getAllFacilities(
            @RequestParam(required = false) FacilityStatus status,
            Pageable pageable
    ) {
        Page<FacilityDto> page = status != null
                ? facilityService.getFacilitiesByStatus(status, pageable)
                : facilityService.getFacilitiesByStatus(FacilityStatus.PENDING, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements())));
    }

    @PatchMapping("/admin/{id}/approval")
    @PreAuthorize("hasRole('NETWORK_ADMIN')")
    @Operation(summary = "Approve or Reject Facility Registration (Admin)")
    public ResponseEntity<ApiResponse<FacilityDto>> updateApproval(
            @PathVariable Long id,
            @Valid @RequestBody FacilityApprovalRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Facility status updated", facilityService.updateApprovalStatus(id, req)));
    }

    @PatchMapping("/admin/{id}/suspend")
    @PreAuthorize("hasRole('NETWORK_ADMIN')")
    @Operation(summary = "Suspend Facility Access (Admin)")
    public ResponseEntity<ApiResponse<FacilityDto>> suspendFacility(
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Facility suspended", facilityService.suspendFacility(id, reason)));
    }
}
