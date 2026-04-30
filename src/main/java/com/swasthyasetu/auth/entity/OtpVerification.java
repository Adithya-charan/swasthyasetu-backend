package com.swasthyasetu.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_records")
public class OtpVerification {
    @Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    
    private String phoneNumber;
    private String otp;
    private java.time.LocalDateTime expiryTime;
    private boolean verifiedStatus;
    private java.time.LocalDateTime createdAt;

    public OtpVerification() {
        this.createdAt = java.time.LocalDateTime.now();
        this.verifiedStatus = false;
    }

    public OtpVerification(String phoneNumber, String otp, int expiryMinutes) {
        this();
        this.phoneNumber = phoneNumber;
        this.otp = otp;
        this.expiryTime = java.time.LocalDateTime.now().plusMinutes(expiryMinutes);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public java.time.LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(java.time.LocalDateTime expiryTime) { this.expiryTime = expiryTime; }
    public boolean isVerifiedStatus() { return verifiedStatus; }
    public void setVerifiedStatus(boolean verifiedStatus) { this.verifiedStatus = verifiedStatus; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isExpired() {
        return java.time.LocalDateTime.now().isAfter(expiryTime);
    }
}
