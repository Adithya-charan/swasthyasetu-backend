package com.swasthyasetu.prescription.dto;

import com.swasthyasetu.prescription.entity.MedicineItem;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record IssuePrescriptionRequest(
    @NotNull UUID appointmentId,
    @NotNull UUID patientId,
    String diagnosis,
    String notes,
    List<MedicineItem> medicines
) {}
