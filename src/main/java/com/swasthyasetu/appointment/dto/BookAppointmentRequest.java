package com.swasthyasetu.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookAppointmentRequest(
    @NotNull UUID doctorId,
    @NotNull @Future @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX") LocalDateTime scheduledAt,
    String reason
) {}
