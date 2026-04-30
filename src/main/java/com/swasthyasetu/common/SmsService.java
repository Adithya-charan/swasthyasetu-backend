package com.swasthyasetu.common;

import org.springframework.stereotype.Service;

@Service
public class SmsService {
    private static final java.security.SecureRandom secureRandom = new java.security.SecureRandom();

    public String generateOtp() {
        return String.format("%06d", 100000 + secureRandom.nextInt(900000));
    }

    public void sendOtp(String phoneNumber, String otp) {
        System.out.println("========================================");
        System.out.println("OTP GENERATED FOR: " + phoneNumber);
        System.out.println("OTP CODE: " + otp);
        System.out.println("EXPIRES IN: 5 MINUTES");
        System.out.println("========================================");
    }
}
