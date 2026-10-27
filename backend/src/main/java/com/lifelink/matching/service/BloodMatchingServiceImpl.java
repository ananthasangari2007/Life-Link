package com.lifelink.matching.service;

import com.lifelink.facility.model.Facility;
import com.lifelink.inventory.dto.InventoryBatchDto;
import com.lifelink.inventory.model.BloodInventoryBatch;
import com.lifelink.inventory.model.enums.BloodGroup;
import com.lifelink.inventory.model.enums.ComponentType;
import com.lifelink.inventory.repository.BloodInventoryBatchRepository;
import com.lifelink.inventory.strategy.InventoryAllocationStrategy;
import com.lifelink.matching.dto.*;
import com.lifelink.matching.policy.BloodCompatibilityPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Deterministic multi-bank blood matching implementation.
 * Ranks facilities using explainable criteria (exact ABO match, available units, expiry buffer)
 * and generates single-bank and multi-bank split fulfillment plans.
 */
@Service
public class BloodMatchingServiceImpl implements BloodMatchingService {

    private final BloodInventoryBatchRepository batchRepository;
    private final BloodCompatibilityPolicy compatibilityPolicy;
    private final InventoryAllocationStrategy allocationStrategy;

    public BloodMatchingServiceImpl(BloodInventoryBatchRepository batchRepository,
                                   BloodCompatibilityPolicy compatibilityPolicy,
                                   InventoryAllocationStrategy allocationStrategy) {
        this.batchRepository = batchRepository;
        this.compatibilityPolicy = compatibilityPolicy;
        this.allocationStrategy = allocationStrategy;
    }

    @Override
    @Transactional(readOnly = true)
    public MatchResultDto findMatches(FindMatchesRequest request) {
        BloodGroup targetGroup = request.bloodGroup();
        ComponentType component = request.componentType();
        int requestedUnits = request.unitsRequested();
        Instant now = Instant.now();

        // 1. Determine compatible donor groups via the polymorphic compatibility policy
        List<BloodGroup> compatibleGroups = compatibilityPolicy.getCompatibleDonorGroups(targetGroup, component);

        // 2. Query available, approved, non-expired batches for the component & compatible groups
        List<BloodInventoryBatch> candidateBatches = batchRepository.findAvailableBatchesForMatching(
                component, compatibleGroups, now
        );

        // 3. Filter out expired or zero-availability batches
        List<BloodInventoryBatch> validBatches = candidateBatches.stream()
                .filter(b -> b.getAvailableQuantity() > 0 && !b.isExpired())
                .toList();

        // 4. Group batches by Facility
        Map<Facility, List<BloodInventoryBatch>> batchesByFacility = validBatches.stream()
                .collect(Collectors.groupingBy(BloodInventoryBatch::getFacility));

        List<CandidateFacilityMatchDto> candidateFacilities = new ArrayList<>();
        int totalNetworkAvailable = 0;

        for (Map.Entry<Facility, List<BloodInventoryBatch>> entry : batchesByFacility.entrySet()) {
            Facility facility = entry.getKey();
            List<BloodInventoryBatch> facilityBatches = allocationStrategy.sortForAllocation(entry.getValue());

            int facilityAvailableUnits = facilityBatches.stream()
                    .mapToInt(BloodInventoryBatch::getAvailableQuantity)
                    .sum();

            totalNetworkAvailable += facilityAvailableUnits;

            // Determine dominant or best compatibility among facility's batches
            BloodGroup bestGroup = facilityBatches.stream()
                    .map(BloodInventoryBatch::getBloodGroup)
                    .max(Comparator.comparingInt(bg -> compatibilityPolicy.getCompatibilityScore(targetGroup, bg, component)))
                    .orElse(targetGroup);

            int score = compatibilityPolicy.getCompatibilityScore(targetGroup, bestGroup, component);
            String explanation = compatibilityPolicy.getCompatibilityExplanation(targetGroup, bestGroup, component);

            boolean canFulfillEntirely = facilityAvailableUnits >= requestedUnits;

            List<InventoryBatchDto> batchDtos = facilityBatches.stream()
                    .map(InventoryBatchDto::fromEntity)
                    .toList();

            candidateFacilities.add(new CandidateFacilityMatchDto(
                    facility.getId(),
                    facility.getName(),
                    facility.getAddress(),
                    facility.getCity(),
                    facility.getContactPhone(),
                    facility.getContactEmail(),
                    bestGroup,
                    bestGroup.getDisplayName(),
                    facilityAvailableUnits,
                    score,
                    explanation,
                    canFulfillEntirely,
                    batchDtos
            ));
        }

        // 5. Deterministic Ranking:
        // - Compatibility score desc (exact match 100 first)
        // - Can fulfill entirely (true first)
        // - Total available units desc
        candidateFacilities.sort(Comparator
                .comparingInt(CandidateFacilityMatchDto::compatibilityScore).reversed()
                .thenComparing((CandidateFacilityMatchDto c) -> c.canFulfillEntirely() ? 0 : 1)
                .thenComparing(Comparator.comparingInt(CandidateFacilityMatchDto::availableUnits).reversed())
        );

        // 6. Generate Suggested Fulfillment Plans
        List<FulfillmentPlanDto> suggestedPlans = generateFulfillmentPlans(candidateFacilities, requestedUnits, targetGroup);

        boolean fullMatchFound = candidateFacilities.stream().anyMatch(CandidateFacilityMatchDto::canFulfillEntirely);

        String medicalNotice = "LifeLink is a coordination platform and does not replace certified laboratory crossmatching, antibody screening, or authorized medical release. ABO/Rh compatibility must be clinically verified prior to transfusion.";

        return new MatchResultDto(
                targetGroup,
                targetGroup.getDisplayName(),
                component,
                component.getDisplayName(),
                requestedUnits,
                totalNetworkAvailable,
                fullMatchFound,
                candidateFacilities,
                suggestedPlans,
                medicalNotice
        );
    }

    private List<FulfillmentPlanDto> generateFulfillmentPlans(
            List<CandidateFacilityMatchDto> candidates, int requestedUnits, BloodGroup targetGroup) {

        List<FulfillmentPlanDto> plans = new ArrayList<>();

        // Plan A: Single facility plans that can satisfy the full request
        for (CandidateFacilityMatchDto c : candidates) {
            if (c.canFulfillEntirely()) {
                PlanSegmentDto segment = new PlanSegmentDto(
                        c.facilityId(), c.facilityName(), c.city(),
                        c.bloodGroup(), c.bloodGroupDisplay(), requestedUnits,
                        "Single-facility full fulfillment (" + c.availableUnits() + " units in stock)"
                );
                plans.add(new FulfillmentPlanDto(
                        "SINGLE_BANK",
                        "Direct Fulfillment from " + c.facilityName(),
                        "Facility has sufficient units to satisfy your entire request without multi-facility coordination.",
                        requestedUnits,
                        true,
                        List.of(segment)
                ));
            }
        }

        // Plan B: If no single facility satisfies or if alternatives are useful, generate a split multi-bank plan
        if (plans.isEmpty() && !candidates.isEmpty()) {
            int allocated = 0;
            List<PlanSegmentDto> segments = new ArrayList<>();

            for (CandidateFacilityMatchDto c : candidates) {
                if (allocated >= requestedUnits) break;
                int take = Math.min(c.availableUnits(), requestedUnits - allocated);
                if (take > 0) {
                    segments.add(new PlanSegmentDto(
                            c.facilityId(), c.facilityName(), c.city(),
                            c.bloodGroup(), c.bloodGroupDisplay(), take,
                            "Partial allocation of " + take + " units"
                    ));
                    allocated += take;
                }
            }

            if (!segments.isEmpty()) {
                boolean completes = allocated >= requestedUnits;
                plans.add(new FulfillmentPlanDto(
                        "MULTI_BANK_SPLIT",
                        "Coordinated Multi-Bank Plan (" + segments.size() + " Facilities)",
                        completes
                                ? "Combines stock across " + segments.size() + " blood banks to fulfill all " + requestedUnits + " requested units."
                                : "Partial coordinated fulfillment: " + allocated + " of " + requestedUnits + " units available across the network.",
                        allocated,
                        completes,
                        segments
                ));
            }
        }

        return plans;
    }
}
