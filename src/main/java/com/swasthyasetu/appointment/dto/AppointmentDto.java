package com.swasthyasetu.appointment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentDto(
    UUID id,
    UUID patientId,
    UUID doctorId,
    LocalDateTime scheduledAt,
    int durationMinutes,
    String status,
    String reason,
    String meetingLink,
    LocalDateTime createdAt
) {}
