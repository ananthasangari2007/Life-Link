package com.lifelink.user.dto;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    String phone,
    String role,
    Long facilityId,
    String facilityName,
    String facilityType,
    String designation,
    boolean active
) {}
