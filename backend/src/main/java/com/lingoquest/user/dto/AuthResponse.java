package com.lingoquest.user.dto;

public record AuthResponse(
        String token,
        long expireInMs,
        UserProfile user
) {}
