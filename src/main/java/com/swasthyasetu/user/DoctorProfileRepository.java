package com.swasthyasetu.user;

import com.swasthyasetu.user.entity.DoctorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {
    @Query("SELECT dp FROM DoctorProfile dp JOIN dp.user u " +
           "WHERE u.isActive = true AND u.role = 'DOCTOR' " +
           "AND (:specialization IS NULL OR dp.specialization = :specialization) " +
           "AND (:name IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<DoctorProfile> findActiveDoctors(@Param("specialization") String specialization, @Param("name") String name, Pageable pageable);
}
