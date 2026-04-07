package com.swasthyasetu.prescription;

import com.swasthyasetu.prescription.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    Page<Prescription> findByPatientIdOrderByIssuedAtDesc(UUID patientId, Pageable pageable);
    Page<Prescription> findByDoctorIdOrderByIssuedAtDesc(UUID doctorId, Pageable pageable);
    
    @Query("SELECT p FROM Prescription p WHERE NOT EXISTS (SELECT 1 FROM MedicineOrder mo WHERE mo.prescriptionId = p.id)")
    Page<Prescription> findUndispensedPrescriptions(Pageable pageable);
}
