package com.swasthyasetu.prescription;

import com.swasthyasetu.appointment.AppointmentService;
import com.swasthyasetu.appointment.entity.Appointment;
import com.swasthyasetu.appointment.entity.AppointmentStatus;
import com.swasthyasetu.common.ResourceConflictException;
import com.swasthyasetu.common.ResourceNotFoundException;
import com.swasthyasetu.prescription.dto.IssuePrescriptionRequest;
import com.swasthyasetu.prescription.entity.MedicineItem;
import com.swasthyasetu.prescription.entity.Prescription;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;
import java.util.Objects;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Service
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentService appointmentService;

    public PrescriptionService(PrescriptionRepository prescriptionRepository, AppointmentService appointmentService) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentService = appointmentService;
    }

    @Transactional
    public Prescription issuePrescription(@NonNull UUID doctorId, @NonNull IssuePrescriptionRequest req) {
        Appointment appt = Objects.requireNonNull(appointmentService.getAppointment(Objects.requireNonNull(req.appointmentId())));
        if (appt.getStatus() != AppointmentStatus.COMPLETED) {
            throw new ResourceConflictException("Cannot issue prescription for an incomplete appointment");
        }
        Prescription pres = new Prescription();
        pres.setAppointmentId(req.appointmentId());
        pres.setDoctorId(doctorId);
        pres.setPatientId(req.patientId());
        pres.setDiagnosis(req.diagnosis());
        pres.setNotes(req.notes());
        pres.setMedicines(req.medicines());
        return Objects.requireNonNull(prescriptionRepository.save(pres));
    }

    public @NonNull Page<Prescription> getPatientPrescriptions(@NonNull UUID patientId, @NonNull Pageable pageable) {
        return Objects.requireNonNull(prescriptionRepository.findByPatientIdOrderByIssuedAtDesc(patientId, pageable));
    }

    public @NonNull Page<Prescription> getDoctorPrescriptions(@NonNull UUID doctorId, @NonNull Pageable pageable) {
        return Objects.requireNonNull(prescriptionRepository.findByDoctorIdOrderByIssuedAtDesc(doctorId, pageable));
    }

    public @NonNull Page<Prescription> getUndispensedPrescriptions(@NonNull Pageable pageable) {
        return Objects.requireNonNull(prescriptionRepository.findUndispensedPrescriptions(pageable));
    }

    public @NonNull Prescription getPrescription(@NonNull UUID id) {
        return Objects.requireNonNull(prescriptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Prescription not found")));
    }

    public byte[] generatePrescriptionPdf(@NonNull UUID id) {
        Prescription pres = getPrescription(id);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            document.add(new Paragraph("SWASTHYA SETU E-PRESCRIPTION"));
            document.add(new Paragraph("Prescription ID: " + pres.getId()));
            document.add(new Paragraph("Date: " + pres.getIssuedAt().toString()));
            document.add(new Paragraph("Diagnosis: " + pres.getDiagnosis()));
            document.add(new Paragraph("Medicines:"));
            
            for (MedicineItem m : pres.getMedicines()) {
                document.add(new Paragraph(" - " + m.getName() + " | " + m.getDosage() + " | " + m.getFrequency() + " | " + m.getDurationDays() + " days"));
            }
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
        return baos.toByteArray();
    }
}
