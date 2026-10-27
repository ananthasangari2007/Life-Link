package com.lifelink.transfer.controller;

import com.lifelink.common.dto.ApiResponse;
import com.lifelink.common.dto.PageResponse;
import com.lifelink.transfer.dto.ConfirmReceiptRequest;
import com.lifelink.transfer.dto.DispatchTransferRequest;
import com.lifelink.transfer.dto.TransferDto;
import com.lifelink.transfer.service.TransferService;
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
@RequestMapping("/api/v1/transfers")
@Tag(name = "Blood Transfers", description = "Endpoints for blood transfer dispatch and custody verification")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/dispatch")
    @PreAuthorize("hasAnyRole('BLOOD_BANK_USER', 'NETWORK_ADMIN')")
    @Operation(summary = "Dispatch Transfer", description = "Authorizes dispatch of reserved blood units from blood bank to hospital")
    public ResponseEntity<ApiResponse<TransferDto>> dispatchTransfer(@Valid @RequestBody DispatchTransferRequest req) {
        TransferDto dto = transferService.dispatchTransfer(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Transfer dispatched successfully", dto));
    }

    @PostMapping("/{id}/confirm-receipt")
    @PreAuthorize("hasAnyRole('HOSPITAL_USER', 'NETWORK_ADMIN')")
    @Operation(summary = "Confirm Transfer Receipt", description = "Hospital authorizes receipt confirmation, concluding the transfer and fulfilling the request")
    public ResponseEntity<ApiResponse<TransferDto>> confirmReceipt(
            @PathVariable Long id,
            @RequestBody(required = false) ConfirmReceiptRequest req
    ) {
        ConfirmReceiptRequest request = req != null ? req : new ConfirmReceiptRequest("Receipt verified by hospital blood bank");
        TransferDto dto = transferService.confirmReceipt(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Transfer receipt confirmed successfully", dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Transfer by ID")
    public ResponseEntity<ApiResponse<TransferDto>> getTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(transferService.getTransferById(id)));
    }

    @GetMapping("/source/{facilityId}")
    @Operation(summary = "Get Dispatched Transfers from Source Facility")
    public ResponseEntity<ApiResponse<PageResponse<TransferDto>>> getSourceTransfers(
            @PathVariable Long facilityId,
            Pageable pageable
    ) {
        Page<TransferDto> page = transferService.getSourceTransfers(facilityId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements())));
    }

    @GetMapping("/destination/{facilityId}")
    @Operation(summary = "Get Incoming Transfers for Destination Hospital")
    public ResponseEntity<ApiResponse<PageResponse<TransferDto>>> getDestinationTransfers(
            @PathVariable Long facilityId,
            Pageable pageable
    ) {
        Page<TransferDto> page = transferService.getDestinationTransfers(facilityId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements())));
    }
}
