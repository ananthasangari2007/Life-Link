package com.lifelink.request.controller;

import com.lifelink.common.dto.ApiResponse;
import com.lifelink.common.dto.PageResponse;
import com.lifelink.request.dto.BankResponseActionDto;
import com.lifelink.request.dto.BloodRequestDto;
import com.lifelink.request.dto.CreateBloodRequestDto;
import com.lifelink.request.model.enums.RequestStatus;
import com.lifelink.request.service.RequestCoordinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/requests")
@Tag(name = "Blood Requests", description = "Endpoints for creating and coordinating hospital blood requests")
public class BloodRequestController {

    private final RequestCoordinationService requestService;

    public BloodRequestController(RequestCoordinationService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HOSPITAL_USER', 'NETWORK_ADMIN')")
    @Operation(summary = "Submit Blood Request", description = "Submits a new blood request from a hospital to matching blood banks")
    public ResponseEntity<ApiResponse<BloodRequestDto>> createRequest(@Valid @RequestBody CreateBloodRequestDto dto) {
        BloodRequestDto created = requestService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Blood request submitted successfully", created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Request Details by ID")
    public ResponseEntity<ApiResponse<BloodRequestDto>> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(requestService.getRequestById(id)));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get Request Details by Reference Code")
    public ResponseEntity<ApiResponse<BloodRequestDto>> getRequestByReference(@PathVariable String reference) {
        return ResponseEntity.ok(ApiResponse.ok(requestService.getRequestByReference(reference)));
    }

    @GetMapping("/hospital/{hospitalId}")
    @Operation(summary = "List Requests by Hospital")
    public ResponseEntity<ApiResponse<PageResponse<BloodRequestDto>>> getHospitalRequests(
            @PathVariable Long hospitalId,
            Pageable pageable
    ) {
        Page<BloodRequestDto> page = requestService.getHospitalRequests(hospitalId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements())));
    }

    @GetMapping("/incoming/{bloodBankId}")
    @PreAuthorize("hasAnyRole('BLOOD_BANK_USER', 'NETWORK_ADMIN')")
    @Operation(summary = "List Incoming Requests for Blood Bank")
    public ResponseEntity<ApiResponse<PageResponse<BloodRequestDto>>> getIncomingRequests(
            @PathVariable Long bloodBankId,
            Pageable pageable
    ) {
        Page<BloodRequestDto> page = requestService.getBankIncomingRequests(bloodBankId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements())));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('NETWORK_ADMIN', 'AUDITOR')")
    @Operation(summary = "List All Network Requests (Admin/Auditor)")
    public ResponseEntity<ApiResponse<PageResponse<BloodRequestDto>>> getAllRequests(
            @RequestParam(required = false) RequestStatus status,
            Pageable pageable
    ) {
        Page<BloodRequestDto> page = requestService.getAllRequests(status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements())));
    }

    @PostMapping("/{id}/respond/{bloodBankId}")
    @PreAuthorize("hasAnyRole('BLOOD_BANK_USER', 'NETWORK_ADMIN')")
    @Operation(summary = "Blood Bank Response", description = "Accepts, partially accepts, or rejects a hospital blood request")
    public ResponseEntity<ApiResponse<BloodRequestDto>> respondToRequest(
            @PathVariable Long id,
            @PathVariable Long bloodBankId,
            @Valid @RequestBody BankResponseActionDto actionDto
    ) {
        BloodRequestDto updated = requestService.respondToRequest(id, bloodBankId, actionDto);
        return ResponseEntity.ok(ApiResponse.ok("Response recorded successfully", updated));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel Blood Request")
    public ResponseEntity<ApiResponse<BloodRequestDto>> cancelRequest(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Cancelled by requester") String reason
    ) {
        BloodRequestDto cancelled = requestService.cancelRequest(id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Request cancelled successfully", cancelled));
    }
}
