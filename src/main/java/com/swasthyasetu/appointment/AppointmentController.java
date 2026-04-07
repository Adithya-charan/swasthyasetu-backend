package com.swasthyasetu.appointment;

import com.swasthyasetu.appointment.dto.AppointmentDto;
import com.swasthyasetu.appointment.dto.BookAppointmentRequest;
import com.swasthyasetu.appointment.entity.Appointment;
import com.swasthyasetu.common.ApiResponse;
import com.swasthyasetu.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;
import java.util.Objects;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    private @NonNull UUID getUserId() {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Objects.requireNonNull(UUID.fromString(principal));
    }

    private boolean isRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private AppointmentDto mapDto(@NonNull Appointment appt) {
        return new AppointmentDto(appt.getId(), appt.getPatientId(), appt.getDoctorId(),
            appt.getScheduledAt(), appt.getDurationMinutes(), appt.getStatus().name(),
            appt.getReason(), appt.getMeetingLink(), appt.getCreatedAt());
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/appointments")
    public ApiResponse<AppointmentDto> bookAppointment(@Valid @NonNull @RequestBody BookAppointmentRequest req) {
        Appointment appt = Objects.requireNonNull(appointmentService.bookAppointment(getUserId(), req));
        return new ApiResponse<>(true, "Appointment booked", mapDto(appt));
    }

    @GetMapping("/appointments/my")
    public ApiResponse<PageResponse<AppointmentDto>> getMyAppointments(@NonNull Pageable pageable) {
        Page<Appointment> page;
        if (isRole("PATIENT")) {
            page = appointmentService.getPatientAppointments(getUserId(), pageable);
        } else if (isRole("DOCTOR")) {
            page = appointmentService.getDoctorAppointments(getUserId(), pageable);
        } else {
            throw new RuntimeException("Invalid role for this endpoint");
        }
        PageResponse<AppointmentDto> pr = new PageResponse<>(
            page.getContent().stream().map(Objects::requireNonNull).map(this::mapDto).collect(Collectors.toList()),
            page.getTotalElements(), page.getTotalPages(), page.getNumber()
        );
        return new ApiResponse<>(true, "Appointments fetched", pr);
    }

    @GetMapping("/appointments/{id}")
    public ApiResponse<AppointmentDto> getAppointment(@NonNull @PathVariable UUID id) {
        Appointment appt = Objects.requireNonNull(appointmentService.getAppointment(id));
        return new ApiResponse<>(true, "Appointment fetched", mapDto(appt));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/appointments/{id}/confirm")
    public ApiResponse<Void> confirmAppointment(@NonNull @PathVariable UUID id, @RequestParam(required=false) String meetingLink) {
        appointmentService.confirmAppointment(id, meetingLink);
        return new ApiResponse<>(true, "Appointment confirmed", null);
    }

    @PutMapping("/appointments/{id}/cancel")
    public ApiResponse<Void> cancelAppointment(@NonNull @PathVariable UUID id, @RequestParam String reason) {
        appointmentService.cancelAppointment(id, reason);
        return new ApiResponse<>(true, "Appointment cancelled", null);
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/appointments/{id}/complete")
    public ApiResponse<Void> completeAppointment(@NonNull @PathVariable UUID id) {
        appointmentService.completeAppointment(id);
        return new ApiResponse<>(true, "Appointment completed", null);
    }
}
