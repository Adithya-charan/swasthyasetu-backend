package com.swasthyasetu.config;

import com.swasthyasetu.appointment.AppointmentRepository;
import com.swasthyasetu.appointment.entity.Appointment;
import com.swasthyasetu.appointment.entity.AppointmentStatus;
import com.swasthyasetu.pharmacy.MedicineRepository;
import com.swasthyasetu.pharmacy.entity.Medicine;
import com.swasthyasetu.user.DoctorProfileRepository;
import com.swasthyasetu.user.UserRepository;
import com.swasthyasetu.user.entity.AccountStatus;
import com.swasthyasetu.user.entity.DoctorProfile;
import com.swasthyasetu.user.entity.Role;
import com.swasthyasetu.user.entity.User;
import com.swasthyasetu.user.entity.VerificationStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicineRepository medicineRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DoctorProfileRepository doctorProfileRepository,
                           AppointmentRepository appointmentRepository,
                           MedicineRepository medicineRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicineRepository = medicineRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            System.out.println("[DataInitializer] Starting cleanup and seeding...");
            // Cleanup Legacy Dummy Data
            userRepository.findByEmail("admin@swasthya.com").ifPresent(userRepository::delete);
            userRepository.findByEmail("doctor@swasthya.com").ifPresent(userRepository::delete);
            userRepository.findByEmail("rajesh@swasthya.com").ifPresent(userRepository::delete);
            userRepository.findByEmail("patient@swasthya.com").ifPresent(userRepository::delete);
            userRepository.findByEmail("priya@swasthya.com").ifPresent(userRepository::delete);
            userRepository.findByEmail("pharmacist@swasthya.com").ifPresent(userRepository::delete);

            // Ensure Admin accounts are always ACTIVE and have the correct password
            String[] adminEmails = {"adithyayaramallla2007@gmail.com", "adithyayaramalla2007@gmail.com"};
            for (String email : adminEmails) {
                userRepository.findByEmail(email).ifPresentOrElse(
                    user -> {
                        user.setFullName("Admin Adithya"); // Force reset name
                        user.setAccountStatus(AccountStatus.ACTIVE);
                        user.setIsActive(true);
                        user.setRole(Role.ADMIN);
                        user.setPasswordHash(passwordEncoder.encode("adi123"));
                        userRepository.save(user);
                        System.out.println("[DataInitializer] ✓ Verified & Updated Admin Account: " + email);
                    },
                    () -> {
                        System.out.println("[DataInitializer] + Seeding New Admin Account: " + email);
                        seedUser("Admin Staff", email, "adi123", Role.ADMIN);
                    }
                );
            }
            System.out.println("[DataInitializer] Initialization completed successfully.");
        } catch (Exception e) {
            System.err.println("[DataInitializer] CRITICAL ERROR DURING INITIALIZATION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private User seedUser(String name, String email, String password, Role role) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setPhone("9876543210");
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setIsActive(true);
        return userRepository.save(user);
    }

    private void seedDoctorProfile(User doctor, String specialization, String qualifications, int experience, String license) {
        DoctorProfile dp = new DoctorProfile();
        dp.setUser(doctor);
        dp.setSpecialization(specialization);
        dp.setQualifications(qualifications);
        dp.setExperienceYears(experience);
        dp.setLicenseNumber(license);
        dp.setConsultationFee(new BigDecimal("500.00"));
        dp.setBio("Experienced " + specialization + " specialist with " + experience + " years of practice.");
        dp.setVerificationStatus(VerificationStatus.VERIFIED);
        doctorProfileRepository.save(dp);
    }

    private void seedAppointment(User patient, User doctor, LocalDateTime scheduledAt, String reason, AppointmentStatus status) {
        Appointment appt = new Appointment();
        appt.setPatientId(patient.getId());
        appt.setDoctorId(doctor.getId());
        appt.setScheduledAt(scheduledAt);
        appt.setReason(reason);
        appt.setStatus(status);
        if (status == AppointmentStatus.CONFIRMED) {
            appt.setMeetingLink("/consultation/" + patient.getId());
        }
        appointmentRepository.save(appt);
    }

    private void seedMedicine(String name, String generic, String mfr, String category, int qty, BigDecimal price) {
        Medicine m = new Medicine();
        m.setName(name);
        m.setGenericName(generic);
        m.setManufacturer(mfr);
        m.setCategory(category);
        m.setStockQuantity(qty);
        m.setUnitPrice(price);
        m.setExpiryDate(LocalDate.now().plusYears(2));
        medicineRepository.save(m);
    }
}
