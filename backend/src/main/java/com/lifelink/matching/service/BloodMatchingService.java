package com.lifelink.matching.service;

import com.lifelink.matching.dto.FindMatchesRequest;
import com.lifelink.matching.dto.MatchResultDto;

/**
 * Service abstraction for the deterministic multi-bank blood matching engine.
 * Demonstrates Abstraction and Strategy composition.
 */
public interface BloodMatchingService {

    /**
     * Executes the multi-facility matching algorithm and returns explainable candidates and fulfillment plans.
     */
    MatchResultDto findMatches(FindMatchesRequest request);
}
