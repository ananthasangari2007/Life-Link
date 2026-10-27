package com.lifelink.facility.dto;

import com.lifelink.facility.model.Facility;

public record FacilityDto(
    Long id,
    String name,
    String licenseNumber,
    String type,
    String status,
    String address,
    String city,
    String state,
    String pincode,
    String contactPhone,
    String contactEmail,
    Double latitude,
    Double longitude,
    String operatingHours,
    String emergencyContact,
    String approvalNotes
) {
    public static FacilityDto fromEntity(Facility f) {
        if (f == null) return null;
        return new FacilityDto(
                f.getId(),
                f.getName(),
                f.getLicenseNumber(),
                f.getType().name(),
                f.getStatus().name(),
                f.getAddress(),
                f.getCity(),
                f.getState(),
                f.getPincode(),
                f.getContactPhone(),
                f.getContactEmail(),
                f.getLatitude(),
                f.getLongitude(),
                f.getOperatingHours(),
                f.getEmergencyContact(),
                f.getApprovalNotes()
        );
    }
}
