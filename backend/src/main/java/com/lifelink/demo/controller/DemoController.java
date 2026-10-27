package com.lifelink.demo.controller;

import com.lifelink.common.dto.ApiResponse;
import com.lifelink.demo.service.DemoDataSeederService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/demo")
@Tag(name = "Demo Data", description = "Endpoints for simulated demonstration setup and credentials reference")
public class DemoController {

    private final DemoDataSeederService demoService;

    public DemoController(DemoDataSeederService demoService) {
        this.demoService = demoService;
    }

    @PostMapping("/seed")
    @Operation(summary = "Seed Demo Healthcare Network", description = "Opt-in generation of simulated hospital, blood banks, inventory batches, and sample requests")
    public ResponseEntity<ApiResponse<String>> seedDemoNetwork() {
        String result = demoService.seedSimulatedDemoNetwork();
        return ResponseEntity.ok(ApiResponse.ok(result, result));
    }

    @GetMapping("/accounts")
    @Operation(summary = "Get Demo Credentials Reference", description = "Returns the pre-configured simulated credentials for evaluation and demonstration")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDemoAccounts() {
        Map<String, Object> accounts = Map.of(
                "disclaimer", "These credentials operate on simulated demo data for evaluation and testing purposes only.",
                "administrator", Map.of(
                        "email", "admin@lifelink.network",
                        "password", "Admin@LifeLink2026!",
                        "role", "NETWORK_ADMIN"
                ),
                "hospital", Map.of(
                        "email", "hospital@lifelink.demo",
                        "password", "Demo@Hospital2026!",
                        "facility", "Metro City General Hospital (DEMO)",
                        "role", "HOSPITAL_USER"
                ),
                "bloodBank", Map.of(
                        "email", "bloodbank@lifelink.demo",
                        "password", "Demo@BloodBank2026!",
                        "facility", "Red Cross Central Blood Center (DEMO)",
                        "role", "BLOOD_BANK_USER"
                )
        );
        return ResponseEntity.ok(ApiResponse.ok(accounts));
    }
}
