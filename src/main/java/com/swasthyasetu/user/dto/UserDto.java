package com.swasthyasetu.user.dto;

import java.util.UUID;
import java.time.LocalDateTime;

public record UserDto(
    UUID id,
    String fullName,
    String email,
    String role,
    String phone,
    String profilePicUrl,
    String accountStatus,
    LocalDateTime createdAt,
    String verificationPdf
) {}
