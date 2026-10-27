package com.lifelink.analytics.controller;

import com.lifelink.analytics.dto.NetworkAnalyticsDto;
import com.lifelink.analytics.dto.ShortageMatrixDto;
import com.lifelink.analytics.service.AnalyticsService;
import com.lifelink.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics & Reports", description = "Endpoints for network-wide inventory analytics and shortage indicators")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/network")
    @Operation(summary = "Network Analytics Overview", description = "Aggregates verified live statistics across all facilities")
    public ResponseEntity<ApiResponse<NetworkAnalyticsDto>> getNetworkAnalytics() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getNetworkAnalytics()));
    }

    @GetMapping("/shortages")
    @Operation(summary = "Network Blood Shortage Matrix", description = "Identifies critical and low blood groups based on threshold levels")
    public ResponseEntity<ApiResponse<List<ShortageMatrixDto>>> getShortageMatrix() {
        return ResponseEntity.ok(ApiResponse.ok(analyticsService.getShortageMatrix()));
    }
}
