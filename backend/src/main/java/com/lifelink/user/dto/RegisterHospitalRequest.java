package com.lifelink.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterHospitalRequest(
    @NotBlank(message = "Hospital name is required")
    String hospitalName,

    @NotBlank(message = "License or registration number is required")
    String licenseNumber,

    @NotBlank(message = "Address is required")
    String address,

    @NotBlank(message = "City is required")
    String city,

    @NotBlank(message = "State is required")
    String state,

    @NotBlank(message = "Pincode is required")
    String pincode,

    @NotBlank(message = "Facility phone is required")
    String contactPhone,

    @NotBlank(message = "Facility email is required")
    @Email(message = "Invalid email format")
    String contactEmail,

    @NotBlank(message = "Administrator full name is required")
    String adminFullName,

    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid email format")
    String adminEmail,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String adminPassword,

    String designation,
    String operatingHours,
    String emergencyContact
) {}
