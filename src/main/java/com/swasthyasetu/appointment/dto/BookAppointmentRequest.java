package com.swasthyasetu.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookAppointmentRequest(
    @NotNull UUID doctorId,
    @NotNull @Future LocalDateTime scheduledAt,
    String reason
) {}
