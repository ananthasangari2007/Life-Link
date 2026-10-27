package com.lifelink.user.dto;

public record AuthResponse(
    String token,
    String refreshToken,
    String tokenType,
    UserResponse user
) {
    public static AuthResponse of(String token, String refreshToken, UserResponse user) {
        return new AuthResponse(token, refreshToken, "Bearer", user);
    }
}
