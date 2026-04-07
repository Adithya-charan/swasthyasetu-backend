package com.swasthyasetu.appointment;

import com.swasthyasetu.appointment.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    Page<Appointment> findByPatientId(UUID patientId, Pageable pageable);
    Page<Appointment> findByDoctorId(UUID doctorId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctorId = :doctorId " +
           "AND a.status IN ('PENDING', 'CONFIRMED') " +
           "AND a.scheduledAt >= :startTime AND a.scheduledAt < :endTime")
    long countOverlappingAppointments(@Param("doctorId") UUID doctorId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);
}
