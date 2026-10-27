package com.lifelink.user.controller;

import com.lifelink.common.dto.ApiResponse;
import com.lifelink.user.dto.*;
import com.lifelink.user.service.AuthService;
import com.lifelink.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and facility registration")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates user and returns JWT access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/register-hospital")
    @Operation(summary = "Register Hospital", description = "Registers a new hospital and initial administrative account")
    public ResponseEntity<ApiResponse<AuthResponse>> registerHospital(@Valid @RequestBody RegisterHospitalRequest request) {
        AuthResponse response = authService.registerHospital(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Hospital registration submitted successfully (pending administrator verification)", response));
    }

    @PostMapping("/register-blood-bank")
    @Operation(summary = "Register Blood Bank", description = "Registers a new blood bank facility and initial administrative account")
    public ResponseEntity<ApiResponse<AuthResponse>> registerBloodBank(@Valid @RequestBody RegisterBloodBankRequest request) {
        AuthResponse response = authService.registerBloodBank(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Blood bank registration submitted successfully (pending administrator verification)", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Current User Profile", description = "Retrieves profile details for the currently authenticated user")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
