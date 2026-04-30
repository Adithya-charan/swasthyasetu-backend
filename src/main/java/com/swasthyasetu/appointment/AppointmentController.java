package com.swasthyasetu.appointment;

import com.swasthyasetu.appointment.dto.AppointmentDto;
import com.swasthyasetu.appointment.dto.BookAppointmentRequest;
import com.swasthyasetu.appointment.dto.ConsultationSummaryRequest;
import com.swasthyasetu.appointment.entity.Appointment;
import com.swasthyasetu.common.ApiResponse;
import com.swasthyasetu.common.PageResponse;
import com.swasthyasetu.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;
import java.util.Objects;

import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final UserRepository userRepository;

    public AppointmentController(AppointmentService appointmentService, UserRepository userRepository) {
        this.appointmentService = appointmentService;
        this.userRepository = userRepository;
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
        String patientName = userRepository.findById(appt.getPatientId())
            .map(u -> u.getFullName()).orElse("Unknown Patient");
        String doctorName = userRepository.findById(appt.getDoctorId())
            .map(u -> u.getFullName()).orElse("Unknown Doctor");

        return new AppointmentDto(appt.getId(), 
            appt.getPatientId(), patientName,
            appt.getDoctorId(), doctorName,
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

    @GetMapping("/appointments/{id}/video-room")
    public ApiResponse<Map<String, String>> getVideoRoom(@NonNull @PathVariable UUID id) {
        String roomName = appointmentService.getVideoRoomName(id);
        return new ApiResponse<>(true, "Room name fetched", Map.of("video_room_name", roomName));
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/consultations/{appointmentId}/summary")
    public ApiResponse<String> saveSummary(@NonNull @PathVariable UUID appointmentId, @Valid @NonNull @RequestBody ConsultationSummaryRequest req) {
        String summary = appointmentService.saveSummary(appointmentId, req);
        return new ApiResponse<>(true, "Summary generated and saved", summary);
    }

    @GetMapping("/consultations/{appointmentId}/summary")
    public ApiResponse<Appointment> getSummary(@NonNull @PathVariable UUID appointmentId) {
        Appointment appt = appointmentService.getAppointment(appointmentId);
        // Ensure only the patient or doctor of this appointment can view it
        UUID currentUserId = getUserId();
        if (!appt.getPatientId().equals(currentUserId) && !appt.getDoctorId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized access to this consultation summary");
        }
        return new ApiResponse<>(true, "Summary fetched", appt);
    }
}
