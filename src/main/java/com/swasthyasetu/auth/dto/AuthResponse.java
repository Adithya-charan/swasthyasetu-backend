package com.swasthyasetu.auth.dto;

import java.util.UUID;

public record AuthResponse(
    String accessToken,
    UUID userId,
    String fullName,
    String email,
    String role,
    String accountStatus
) {}
