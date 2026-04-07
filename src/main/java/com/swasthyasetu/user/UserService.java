package com.swasthyasetu.user;

import com.swasthyasetu.common.ResourceNotFoundException;
import com.swasthyasetu.user.entity.DoctorProfile;
import com.swasthyasetu.user.entity.Role;
import com.swasthyasetu.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.util.Objects;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    public UserService(UserRepository userRepository, DoctorProfileRepository doctorProfileRepository) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
    }

    public User getUserById(@NonNull UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public @NonNull User updateUserProfile(@NonNull UUID id, String fullName, String phone, String profilePicUrl) {
        User user = getUserById(id);
        if (fullName != null) user.setFullName(fullName);
        if (phone != null) user.setPhone(phone);
        if (profilePicUrl != null) user.setProfilePicUrl(profilePicUrl);
        return Objects.requireNonNull(userRepository.save(user), "Failed to save user profile");
    }

    public Page<DoctorProfile> getActiveDoctors(String specialization, String name, @NonNull Pageable pageable) {
        return doctorProfileRepository.findActiveDoctors(specialization, name, pageable);
    }

    public DoctorProfile getDoctorProfile(@NonNull UUID doctorId) {
        return doctorProfileRepository.findById(doctorId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
    }

    public Page<User> getAllUsersAdmin(Role role, @NonNull Pageable pageable) {
        if (role != null) {
            return userRepository.findByRole(role, pageable);
        }
        return userRepository.findAll(pageable);
    }

    @Transactional
    public void setUserActiveStatus(@NonNull UUID userId, boolean isActive) {
        User user = getUserById(userId);
        user.setActive(isActive);
        userRepository.save(user);
    }
}
