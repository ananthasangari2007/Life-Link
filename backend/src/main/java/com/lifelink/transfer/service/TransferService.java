package com.lifelink.transfer.service;

import com.lifelink.transfer.dto.ConfirmReceiptRequest;
import com.lifelink.transfer.dto.DispatchTransferRequest;
import com.lifelink.transfer.dto.TransferDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service abstraction for managing blood custody transfers between facilities.
 * Demonstrates Abstraction in the transfer workflow.
 */
public interface TransferService {

    TransferDto dispatchTransfer(DispatchTransferRequest request);

    TransferDto confirmReceipt(Long transferId, ConfirmReceiptRequest request);

    TransferDto getTransferById(Long transferId);

    Page<TransferDto> getSourceTransfers(Long facilityId, Pageable pageable);

    Page<TransferDto> getDestinationTransfers(Long facilityId, Pageable pageable);
}
