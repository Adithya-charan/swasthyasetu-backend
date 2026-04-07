package com.swasthyasetu.appointment;

import com.swasthyasetu.appointment.dto.BookAppointmentRequest;
import com.swasthyasetu.appointment.entity.Appointment;
import com.swasthyasetu.appointment.entity.AppointmentStatus;
import com.swasthyasetu.common.ResourceConflictException;
import com.swasthyasetu.common.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.lang.NonNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public Appointment bookAppointment(@NonNull UUID patientId, @NonNull BookAppointmentRequest req) {
        LocalDateTime endTime = req.scheduledAt().plusMinutes(30);
        long count = appointmentRepository.countOverlappingAppointments(req.doctorId(), req.scheduledAt(), endTime);
        if (count > 0) {
            throw new ResourceConflictException("Doctor already has an appointment in this time slot.");
        }
        Appointment appt = new Appointment();
        appt.setPatientId(patientId);
        appt.setDoctorId(req.doctorId());
        appt.setScheduledAt(req.scheduledAt());
        appt.setReason(req.reason());
        return appointmentRepository.save(appt);
    }

    public Page<Appointment> getPatientAppointments(@NonNull UUID patientId, @NonNull Pageable pageable) {
        return appointmentRepository.findByPatientId(patientId, pageable);
    }

    public Page<Appointment> getDoctorAppointments(@NonNull UUID doctorId, @NonNull Pageable pageable) {
        return appointmentRepository.findByDoctorId(doctorId, pageable);
    }

    public Appointment getAppointment(@NonNull UUID id) {
        return appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
    }

    @Transactional
    public void confirmAppointment(@NonNull UUID id, String meetingLink) {
        Appointment appt = getAppointment(id);
        appt.setStatus(AppointmentStatus.CONFIRMED);
        if (meetingLink != null) appt.setMeetingLink(meetingLink);
        appointmentRepository.save(appt);
    }

    @Transactional
    public void cancelAppointment(@NonNull UUID id, String reason) {
        Appointment appt = getAppointment(id);
        appt.setStatus(AppointmentStatus.CANCELLED);
        appt.setCancellationReason(reason);
        appointmentRepository.save(appt);
    }

    @Transactional
    public void completeAppointment(@NonNull UUID id) {
        Appointment appt = getAppointment(id);
        appt.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appt);
    }
}
