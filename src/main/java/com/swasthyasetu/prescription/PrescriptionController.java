package com.swasthyasetu.prescription;

import com.swasthyasetu.common.ApiResponse;
import com.swasthyasetu.prescription.dto.IssuePrescriptionRequest;
import com.swasthyasetu.prescription.entity.Prescription;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;
import java.util.Objects;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    private @NonNull UUID getUserId() {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Objects.requireNonNull(UUID.fromString(principal));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/prescriptions")
    public ApiResponse<Prescription> issuePrescription(@Valid @NonNull @RequestBody IssuePrescriptionRequest req) {
        Prescription p = prescriptionService.issuePrescription(getUserId(), req);
        return new ApiResponse<>(true, "Prescription issued", p);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/prescriptions/my")
    public ApiResponse<Page<Prescription>> getMyPrescriptions(@NonNull Pageable pageable) {
        return new ApiResponse<>(true, "Prescriptions", prescriptionService.getPatientPrescriptions(getUserId(), pageable));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/prescriptions/doctor/issued")
    public ApiResponse<Page<Prescription>> getIssued(@NonNull Pageable pageable) {
        return new ApiResponse<>(true, "Prescriptions", prescriptionService.getDoctorPrescriptions(getUserId(), pageable));
    }

    @GetMapping("/prescriptions/{id}")
    public ApiResponse<Prescription> getPrescription(@NonNull @PathVariable UUID id) {
        return new ApiResponse<>(true, "Prescription fetched", prescriptionService.getPrescription(id));
    }

    @GetMapping("/prescriptions/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@NonNull @PathVariable UUID id) {
        byte[] pdf = prescriptionService.generatePrescriptionPdf(id);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=prescription-" + id + ".pdf")
            .contentType((MediaType) Objects.requireNonNull(MediaType.APPLICATION_PDF))
            .body(pdf);
    }

    @PreAuthorize("hasRole('PHARMACIST')")
    @GetMapping("/pharmacist/prescriptions")
    public ApiResponse<Page<Prescription>> getUndispensed(@NonNull Pageable pageable) {
         return new ApiResponse<>(true, "Undispensed", prescriptionService.getUndispensedPrescriptions(pageable));
    }
}
