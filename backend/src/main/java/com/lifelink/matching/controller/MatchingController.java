package com.lifelink.matching.controller;

import com.lifelink.common.dto.ApiResponse;
import com.lifelink.matching.dto.FindMatchesRequest;
import com.lifelink.matching.dto.MatchResultDto;
import com.lifelink.matching.service.BloodMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matching")
@Tag(name = "Matching Engine", description = "Endpoints for deterministic multi-bank blood compatibility matching")
public class MatchingController {

    private final BloodMatchingService matchingService;

    public MatchingController(BloodMatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping("/find-matches")
    @Operation(summary = "Find Blood Matches", description = "Executes the multi-bank matching algorithm and returns ranked facilities and fulfillment plans")
    public ResponseEntity<ApiResponse<MatchResultDto>> findMatches(@Valid @RequestBody FindMatchesRequest request) {
        MatchResultDto result = matchingService.findMatches(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
