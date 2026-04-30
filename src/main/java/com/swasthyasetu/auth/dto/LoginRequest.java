package com.swasthyasetu.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Email/Phone is required")
    String email,
    
    @NotBlank(message = "Password is required")
    String password,

    String otp
) {}
