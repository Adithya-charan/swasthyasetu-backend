package com.swasthyasetu.record;

import com.swasthyasetu.record.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
    List<MedicalRecord> findByPatientIdOrderByUploadedAtDesc(UUID patientId);
}
