package com.swasthyasetu.appointment;

import com.swasthyasetu.appointment.dto.BookAppointmentRequest;
import com.swasthyasetu.appointment.dto.ConsultationSummaryRequest;
import com.swasthyasetu.appointment.entity.Appointment;
import com.swasthyasetu.appointment.entity.AppointmentStatus;
import com.swasthyasetu.webrtc.GeminiService;
import com.swasthyasetu.common.ResourceConflictException;
import com.swasthyasetu.common.ResourceNotFoundException;
import com.swasthyasetu.user.UserService;
import com.swasthyasetu.user.entity.AccountStatus;
import com.swasthyasetu.user.entity.DoctorProfile;
import com.swasthyasetu.user.entity.User;
import com.swasthyasetu.user.entity.VerificationStatus;
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
    private final GeminiService geminiService;
    private final UserService userService;

    public AppointmentService(AppointmentRepository appointmentRepository, GeminiService geminiService, UserService userService) {
        this.appointmentRepository = appointmentRepository;
        this.geminiService = geminiService;
        this.userService = userService;
    }

    @Transactional
    public Appointment bookAppointment(@NonNull UUID patientId, @NonNull BookAppointmentRequest req) {
        System.out.println("[AppointmentService] Booking appointment: Patient=" + patientId + ", Doctor=" + req.doctorId() + ", Time=" + req.scheduledAt());
        try {
            User patient = userService.getUserById(patientId);
            if (patient.getAccountStatus() != AccountStatus.ACTIVE) {
                throw new ResourceConflictException("Patient account is not active");
            }
            
            DoctorProfile doctor = userService.getDoctorProfile(req.doctorId());
            if (doctor.getUser().getAccountStatus() != AccountStatus.ACTIVE || doctor.getVerificationStatus() != VerificationStatus.VERIFIED) {
                throw new ResourceConflictException("Doctor is not active or verified");
            }

            LocalDateTime searchStart = req.scheduledAt().minusMinutes(30);
            LocalDateTime searchEnd = req.scheduledAt().plusMinutes(30);
            long count = appointmentRepository.countOverlappingAppointments(req.doctorId(), searchStart, searchEnd);
            if (count > 0) {
                System.out.println("[AppointmentService] Overlap detected: " + count + " existing appointments in window " + searchStart + " to " + searchEnd);
                throw new ResourceConflictException("Doctor already has an appointment in this time slot.");
            }
            Appointment appt = new Appointment();
            appt.setPatientId(patientId);
            appt.setDoctorId(req.doctorId());
            appt.setScheduledAt(req.scheduledAt());
            appt.setReason(req.reason());
            Appointment saved = appointmentRepository.save(appt);
            System.out.println("[AppointmentService] Appointment saved: " + saved.getId());
            return saved;
        } catch (Exception e) {
            System.err.println("[AppointmentService] Booking failed: " + e.getMessage());
            throw e;
        }
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

    @Transactional
    public String getVideoRoomName(@NonNull UUID id) {
        Appointment appt = getAppointment(id);
        if (appt.getVideoRoomName() == null) {
            appt.setVideoRoomName("SwasthyaSetuAppointment" + id.toString().substring(0, 8));
            appointmentRepository.save(appt);
        }
        return appt.getVideoRoomName();
    }

    @Transactional
    public String saveSummary(@NonNull UUID id, @NonNull ConsultationSummaryRequest req) {
        Appointment appt = getAppointment(id);
        appt.setChiefComplaint(req.chiefComplaint());
        appt.setDiagnosis(req.diagnosis());
        appt.setMedicineList(req.medicineList());
        appt.setNotes(req.notes());
        appt.setSummaryLanguage(req.language());
        
        String summary = geminiService.generateSummary(
            req.chiefComplaint(), req.diagnosis(), req.medicineList(), req.notes(), req.language()
        );
        
        appt.setSummaryText(summary);
        appt.setSummaryGeneratedAt(LocalDateTime.now());
        appointmentRepository.save(appt);
        return summary;
    }
}
