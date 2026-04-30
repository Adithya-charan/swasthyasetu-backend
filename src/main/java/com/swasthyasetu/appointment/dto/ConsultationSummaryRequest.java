package com.swasthyasetu.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ConsultationSummaryRequest(
    @NotBlank String chiefComplaint,
    @NotBlank String diagnosis,
    String medicineList,
    String notes,
    @NotBlank String language
) {}
