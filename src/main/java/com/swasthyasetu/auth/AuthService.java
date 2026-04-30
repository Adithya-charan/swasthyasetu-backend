package com.swasthyasetu.auth;

import com.swasthyasetu.auth.entity.OtpVerification;
import com.swasthyasetu.auth.dto.LoginRequest;
import com.swasthyasetu.auth.dto.RegisterRequest;
import com.swasthyasetu.common.ResourceConflictException;
import com.swasthyasetu.user.DoctorProfileRepository;
import com.swasthyasetu.user.UserRepository;
import com.swasthyasetu.user.entity.AccountStatus;
import com.swasthyasetu.user.entity.DoctorProfile;
import com.swasthyasetu.user.entity.Role;
import com.swasthyasetu.user.entity.User;
import com.swasthyasetu.user.entity.VerificationStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.swasthyasetu.common.SmsService smsService;
    private final OtpVerificationRepository otpVerificationRepository;

    public AuthService(UserRepository userRepository, 
                       DoctorProfileRepository doctorProfileRepository, 
                       PasswordEncoder passwordEncoder,
                       com.swasthyasetu.common.SmsService smsService,
                       OtpVerificationRepository otpVerificationRepository) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.smsService = smsService;
        this.otpVerificationRepository = otpVerificationRepository;
    }

    @Transactional
    public void sendOtp(String identifier) {
        String cleanIdentifier = identifier.trim();
        User user = userRepository.findByEmail(cleanIdentifier)
            .orElseGet(() -> {
                String phone = cleanIdentifier.replaceAll("[^0-9]", "");
                if (phone.length() > 10) phone = phone.substring(phone.length() - 10);
                return userRepository.findByPhone(phone).orElse(null);
            });

        if (user == null) {
            throw new BadCredentialsException("Account not found with this identifier");
        }

        String phoneNumber = user.getPhone();
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new RuntimeException("No phone number associated with this account");
        }

        String otp = smsService.generateOtp();
        System.out.println("[AuthService] Generated OTP for " + phoneNumber + ": " + otp);
        
        smsService.sendOtp(phoneNumber, otp);
        
        OtpVerification verification = new OtpVerification(phoneNumber, otp, 5); 
        otpVerificationRepository.save(verification);
    }

    @Transactional
    public void sendOtpForSignup(String identifier) {
        String phone = identifier.replaceAll("[^0-9]", "");
        if (phone.length() > 10) phone = phone.substring(phone.length() - 10);

        String otp = smsService.generateOtp();
        System.out.println("[AuthService] Generated Signup OTP for " + phone + ": " + otp);
        
        smsService.sendOtp(phone, otp);
        
        OtpVerification verification = new OtpVerification(phone, otp, 10); // 10 mins for signup
        otpVerificationRepository.save(verification);
    }

    @Transactional
    public boolean verifyOtp(String phoneNumber, String otp) {
        // DEMO BYPASS: Accept any 6-digit OTP for testing
        if (otp != null && (otp.equals("123456") || otp.length() == 6)) {
            System.out.println("[AuthService] DEMO BYPASS: Accepted OTP " + otp + (phoneNumber != null ? " for " + phoneNumber : ""));
            return true;
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new BadCredentialsException("Phone number is required for OTP verification");
        }

        String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
        if (cleanPhone.length() > 10) cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
        if (cleanPhone.isEmpty()) {
            throw new BadCredentialsException("Invalid phone number format");
        }

        OtpVerification verification = otpVerificationRepository.findFirstByPhoneNumberOrderByCreatedAtDesc(cleanPhone)
            .orElseThrow(() -> new BadCredentialsException("OTP not sent or expired"));

        if (verification.isExpired()) {
            throw new BadCredentialsException("OTP has expired");
        }

        if (!verification.getOtp().equals(otp)) {
            throw new BadCredentialsException("Invalid OTP code");
        }

        otpVerificationRepository.delete(verification);
        return true;
    }

    @Transactional
    public User register(RegisterRequest req) {
        verifyOtp(req.phone(), req.otp());

        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new ResourceConflictException("Email is already in use");
        }
        User user = new User();
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        Role role = Role.valueOf(req.role().toUpperCase());
        user.setRole(role);
        user.setPhone(req.phone());
        user.setAccountStatus(AccountStatus.ACTIVE); // AUTO-APPROVE FOR DEV
        User savedUser = userRepository.save(user);

        if (role == Role.DOCTOR) {
            DoctorProfile dp = new DoctorProfile();
            dp.setUser(savedUser);
            dp.setSpecialization(req.specialization());
            dp.setClinicHospitalInfo(req.clinicHospitalInfo());
            dp.setLicenseNumber(req.licenseNumber());
            dp.setLanguagePreference(req.languagePreference());
            dp.setVerificationStatus(VerificationStatus.VERIFIED); // AUTO-VERIFY FOR DEV
            doctorProfileRepository.save(dp);
        }
        
        return savedUser;
    }

    public User findById(java.util.UUID id) {
        if (id == null) throw new BadCredentialsException("Invalid User ID");
        return userRepository.findById(id)
            .orElseThrow(() -> new BadCredentialsException("User not found"));
    }

    public User authenticate(LoginRequest req) {
        String cleanIdentifier = req.email().trim();
        
        // DEMO BYPASS for specific email domains or addresses
        boolean isDemo = cleanIdentifier.endsWith("@example.com") || 
                         cleanIdentifier.endsWith("@swasthyasetu.com") || 
                         cleanIdentifier.startsWith("demo_");
        
        User user = userRepository.findByEmail(cleanIdentifier)
            .orElseGet(() -> {
                String phone = cleanIdentifier.replaceAll("[^0-9]", "");
                if (phone.length() >= 10) {
                    if (phone.length() > 10) phone = phone.substring(phone.length() - 10);
                    return userRepository.findByPhone(phone).orElse(null);
                }
                return null;
            });
        
        if (user == null && isDemo) {
            // Create a demo user on the fly if it doesn't exist
            System.out.println("[AuthService] Creating demo user: " + cleanIdentifier);
            user = new User();
            user.setEmail(cleanIdentifier);
            user.setFullName(cleanIdentifier.split("@")[0].toUpperCase());
            user.setPasswordHash(passwordEncoder.encode(req.password()));
            user.setPhone("9999999999");
            user.setAccountStatus(AccountStatus.ACTIVE);
            
            // Assign role based on email if possible
            if (cleanIdentifier.contains("doctor")) user.setRole(Role.DOCTOR);
            else if (cleanIdentifier.contains("pharmacist")) user.setRole(Role.PHARMACIST);
            else if (cleanIdentifier.contains("admin")) user.setRole(Role.ADMIN);
            else user.setRole(Role.PATIENT);
            
            user = userRepository.save(user);

            if (user.getRole() == Role.DOCTOR) {
                DoctorProfile dp = new DoctorProfile();
                dp.setUser(user);
                dp.setSpecialization("General Physician");
                dp.setClinicHospitalInfo("Demo Hospital");
                dp.setLicenseNumber("DEMO12345");
                dp.setVerificationStatus(VerificationStatus.VERIFIED);
                doctorProfileRepository.save(dp);
            }
        }
        
        if (user == null) {
            throw new BadCredentialsException("Account not found");
        }
        
        if (!isDemo && !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid password");
        }

        // Bypassing OTP for demo users or if they provide '123456'
        if (!isDemo) {
            verifyOtp(user.getPhone(), req.otp());
        }

        if (user.getAccountStatus() == AccountStatus.PENDING) {
            throw new BadCredentialsException("Account pending approval");
        }
        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new BadCredentialsException("Account suspended");
        }
        return user;
    }
}
