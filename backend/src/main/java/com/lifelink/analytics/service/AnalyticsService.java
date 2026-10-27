package com.lifelink.analytics.service;

import com.lifelink.analytics.dto.NetworkAnalyticsDto;
import com.lifelink.analytics.dto.ShortageMatrixDto;
import com.lifelink.facility.model.enums.FacilityStatus;
import com.lifelink.facility.model.enums.FacilityType;
import com.lifelink.facility.repository.FacilityRepository;
import com.lifelink.inventory.model.BloodInventoryBatch;
import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import com.lifelink.inventory.repository.BloodInventoryBatchRepository;
import com.lifelink.request.model.enums.RequestStatus;
import com.lifelink.request.repository.BloodRequestRepository;
import com.lifelink.transfer.model.enums.TransferStatus;
import com.lifelink.transfer.repository.BloodTransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class AnalyticsService {

    private final FacilityRepository facilityRepository;
    private final BloodInventoryBatchRepository batchRepository;
    private final BloodRequestRepository requestRepository;
    private final BloodTransferRepository transferRepository;

    public AnalyticsService(FacilityRepository facilityRepository,
                            BloodInventoryBatchRepository batchRepository,
                            BloodRequestRepository requestRepository,
                            BloodTransferRepository transferRepository) {
        this.facilityRepository = facilityRepository;
        this.batchRepository = batchRepository;
        this.requestRepository = requestRepository;
        this.transferRepository = transferRepository;
    }

    @Transactional(readOnly = true)
    public NetworkAnalyticsDto getNetworkAnalytics() {
        int approvedHospitals = facilityRepository.findByTypeAndStatus(FacilityType.HOSPITAL, FacilityStatus.APPROVED).size();
        int approvedBloodBanks = facilityRepository.findByTypeAndStatus(FacilityType.BLOOD_BANK, FacilityStatus.APPROVED).size();

        List<BloodInventoryBatch> allBatches = batchRepository.findAll();
        int totalAvailable = 0;
        int totalReserved = 0;

        Map<String, Integer> stockByBloodGroup = new HashMap<>();
        Map<String, Integer> stockByComponent = new HashMap<>();

        for (BloodInventoryBatch b : allBatches) {
            totalReserved += b.getReservedQuantity();
            int avail = b.getAvailableQuantity();
            totalAvailable += avail;

            if (avail > 0) {
                String bg = b.getBloodGroup().getDisplayName();
                stockByBloodGroup.put(bg, stockByBloodGroup.getOrDefault(bg, 0) + avail);

                String comp = b.getComponentType().getDisplayName();
                stockByComponent.put(comp, stockByComponent.getOrDefault(comp, 0) + avail);
            }
        }

        int totalRequests = (int) requestRepository.count();
        int fulfilledRequests = (int) requestRepository.countByStatus(RequestStatus.FULFILLED);
        int activeTransfers = (int) transferRepository.countByStatus(TransferStatus.IN_TRANSIT);

        double fulfillmentRate = totalRequests > 0
                ? Math.round(((double) fulfilledRequests / (double) totalRequests) * 1000.0) / 10.0
                : 100.0;

        return new NetworkAnalyticsDto(
                approvedHospitals,
                approvedBloodBanks,
                totalAvailable,
                totalReserved,
                totalRequests,
                fulfilledRequests,
                activeTransfers,
                fulfillmentRate,
                stockByBloodGroup,
                stockByComponent
        );
    }

    @Transactional(readOnly = true)
    public List<ShortageMatrixDto> getShortageMatrix() {
        List<BloodInventoryBatch> allBatches = batchRepository.findAll();
        Map<BloodGroup, Integer> availableByGroup = new EnumMap<>(BloodGroup.class);

        for (BloodInventoryBatch b : allBatches) {
            int avail = b.getAvailableQuantity();
            if (avail > 0) {
                availableByGroup.put(b.getBloodGroup(), availableByGroup.getOrDefault(b.getBloodGroup(), 0) + avail);
            }
        }

        List<ShortageMatrixDto> matrix = new ArrayList<>();
        int minimumSafetyThreshold = 10;

        for (BloodGroup bg : BloodGroup.values()) {
            int currentAvail = availableByGroup.getOrDefault(bg, 0);
            String severity;
            if (currentAvail == 0 || currentAvail <= 3) {
                severity = "CRITICAL";
            } else if (currentAvail < minimumSafetyThreshold) {
                severity = "LOW";
            } else {
                severity = "NORMAL";
            }

            matrix.add(new ShortageMatrixDto(
                    bg.getDisplayName(),
                    "Red Blood Cells / Platelets",
                    currentAvail,
                    minimumSafetyThreshold,
                    severity
            ));
        }

        return matrix;
    }
}
