package com.swasthyasetu.auth;

import com.swasthyasetu.auth.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findFirstByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);
}
