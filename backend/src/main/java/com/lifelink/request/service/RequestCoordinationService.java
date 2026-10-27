package com.lifelink.request.service;

import com.lifelink.request.dto.BankResponseActionDto;
import com.lifelink.request.dto.BloodRequestDto;
import com.lifelink.request.dto.CreateBloodRequestDto;
import com.lifelink.request.model.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service abstraction for hospital blood request coordination and multi-bank responses.
 * Demonstrates Abstraction in the coordination layer.
 */
public interface RequestCoordinationService {

    BloodRequestDto createRequest(CreateBloodRequestDto dto);

    BloodRequestDto getRequestById(Long requestId);

    BloodRequestDto getRequestByReference(String reference);

    Page<BloodRequestDto> getHospitalRequests(Long hospitalId, Pageable pageable);

    Page<BloodRequestDto> getBankIncomingRequests(Long bloodBankId, Pageable pageable);

    Page<BloodRequestDto> getAllRequests(RequestStatus status, Pageable pageable);

    BloodRequestDto respondToRequest(Long requestId, Long bloodBankId, BankResponseActionDto actionDto);

    BloodRequestDto cancelRequest(Long requestId, String reason);
}
