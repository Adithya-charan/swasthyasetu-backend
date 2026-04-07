package com.swasthyasetu.user.dto;

import java.math.BigDecimal;

public record DoctorProfileDto(
    UserDto user,
    String specialization,
    String qualifications,
    int experienceYears,
    String licenseNumber,
    BigDecimal consultationFee,
    String bio
) {}
