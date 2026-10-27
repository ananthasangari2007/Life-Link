package com.lifelink.user.service;

import com.lifelink.audit.service.AuditService;
import com.lifelink.common.exception.BusinessRuleException;
import com.lifelink.common.exception.ResourceNotFoundException;
import com.lifelink.facility.model.Facility;
import com.lifelink.facility.model.enums.FacilityStatus;
import com.lifelink.facility.model.enums.FacilityType;
import com.lifelink.facility.repository.FacilityRepository;
import com.lifelink.security.JwtService;
import com.lifelink.security.SecurityUser;
import com.lifelink.user.dto.*;
import com.lifelink.user.model.RefreshToken;
import com.lifelink.user.model.User;
import com.lifelink.user.model.enums.UserRole;
import com.lifelink.user.repository.RefreshTokenRepository;
import com.lifelink.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository,
                       FacilityRepository facilityRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.facilityRepository = facilityRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.email()));

        if (!user.isActive()) {
            throw new BusinessRuleException("User account is inactive. Please contact support.", "ACCOUNT_INACTIVE");
        }

        // If user is tied to a facility, check approval status
        if (user.getFacility() != null && user.getFacility().getStatus() == FacilityStatus.REJECTED) {
            throw new BusinessRuleException("Your facility registration was rejected. Reason: " + user.getFacility().getApprovalNotes(), "FACILITY_REJECTED");
        }

        SecurityUser securityUser = new SecurityUser(user);
        String accessToken = jwtService.generateToken(securityUser);
        String refreshTokenStr = jwtService.generateRefreshToken(user.getEmail());

        RefreshToken refreshToken = new RefreshToken(
                user,
                refreshTokenStr,
                Instant.now().plus(7, ChronoUnit.DAYS)
        );
        refreshTokenRepository.save(refreshToken);

        auditService.record(user.getId(), user.getEmail(), user.getRole().name(),
                user.getFacility() != null ? user.getFacility().getId() : null,
                "USER_LOGIN", "User", user.getId(), "Successful login", null);

        return AuthResponse.of(accessToken, refreshTokenStr, toUserResponse(user));
    }

    @Transactional
    public AuthResponse registerHospital(RegisterHospitalRequest req) {
        validateEmailAndLicense(req.adminEmail(), req.licenseNumber());

        Facility facility = new Facility(
                req.hospitalName().trim(),
                req.licenseNumber().trim().toUpperCase(),
                FacilityType.HOSPITAL,
                req.address().trim(),
                req.city().trim(),
                req.state().trim(),
                req.pincode().trim(),
                req.contactPhone().trim(),
                req.contactEmail().trim().toLowerCase()
        );
        facility.setOperatingHours(req.operatingHours());
        facility.setEmergencyContact(req.emergencyContact());
        Facility savedFacility = facilityRepository.save(facility);

        User adminUser = new User(
                req.adminEmail().trim().toLowerCase(),
                passwordEncoder.encode(req.adminPassword()),
                req.adminFullName().trim(),
                req.contactPhone().trim(),
                UserRole.HOSPITAL_USER,
                savedFacility,
                req.designation() != null ? req.designation() : "Hospital Administrator"
        );
        User savedUser = userRepository.save(adminUser);

        SecurityUser securityUser = new SecurityUser(savedUser);
        String accessToken = jwtService.generateToken(securityUser);
        String refreshTokenStr = jwtService.generateRefreshToken(savedUser.getEmail());

        auditService.record(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name(),
                savedFacility.getId(), "REGISTER_HOSPITAL", "Facility", savedFacility.getId(),
                "New hospital registration submitted: " + savedFacility.getName(), null);

        return AuthResponse.of(accessToken, refreshTokenStr, toUserResponse(savedUser));
    }

    @Transactional
    public AuthResponse registerBloodBank(RegisterBloodBankRequest req) {
        validateEmailAndLicense(req.adminEmail(), req.licenseNumber());

        Facility facility = new Facility(
                req.bloodBankName().trim(),
                req.licenseNumber().trim().toUpperCase(),
                FacilityType.BLOOD_BANK,
                req.address().trim(),
                req.city().trim(),
                req.state().trim(),
                req.pincode().trim(),
                req.contactPhone().trim(),
                req.contactEmail().trim().toLowerCase()
        );
        facility.setOperatingHours(req.operatingHours());
        facility.setEmergencyContact(req.emergencyContact());
        Facility savedFacility = facilityRepository.save(facility);

        User adminUser = new User(
                req.adminEmail().trim().toLowerCase(),
                passwordEncoder.encode(req.adminPassword()),
                req.adminFullName().trim(),
                req.contactPhone().trim(),
                UserRole.BLOOD_BANK_USER,
                savedFacility,
                req.designation() != null ? req.designation() : "Blood Bank Director"
        );
        User savedUser = userRepository.save(adminUser);

        SecurityUser securityUser = new SecurityUser(savedUser);
        String accessToken = jwtService.generateToken(securityUser);
        String refreshTokenStr = jwtService.generateRefreshToken(savedUser.getEmail());

        auditService.record(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name(),
                savedFacility.getId(), "REGISTER_BLOOD_BANK", "Facility", savedFacility.getId(),
                "New blood bank registration submitted: " + savedFacility.getName(), null);

        return AuthResponse.of(accessToken, refreshTokenStr, toUserResponse(savedUser));
    }

    private void validateEmailAndLicense(String email, String licenseNumber) {
        if (userRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new BusinessRuleException("A user with this email address already exists.", "EMAIL_ALREADY_EXISTS");
        }
        if (facilityRepository.existsByLicenseNumber(licenseNumber.trim().toUpperCase())) {
            throw new BusinessRuleException("A facility with this license number already exists.", "LICENSE_ALREADY_EXISTS");
        }
    }

    public UserResponse toUserResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getPhone(),
                u.getRole().name(),
                u.getFacility() != null ? u.getFacility().getId() : null,
                u.getFacility() != null ? u.getFacility().getName() : null,
                u.getFacility() != null ? u.getFacility().getType().name() : null,
                u.getDesignation(),
                u.isActive()
        );
    }
}
