package com.swasthyasetu.auth;

import com.swasthyasetu.auth.dto.LoginRequest;
import com.swasthyasetu.auth.dto.RegisterRequest;
import com.swasthyasetu.common.ResourceConflictException;
import com.swasthyasetu.user.UserRepository;
import com.swasthyasetu.user.entity.Role;
import com.swasthyasetu.user.entity.User;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest req) {
        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new ResourceConflictException("Email is already in use");
        }
        User user = new User();
        user.setFullName(req.fullName());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(Role.valueOf(req.role().toUpperCase()));
        user.setPhone(req.phone());
        return userRepository.save(user);
    }

    public User authenticate(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (!user.isActive()) {
            throw new BadCredentialsException("User account is inactive");
        }
        return user;
    }
}
