package com.swasthyasetu.auth;

import com.swasthyasetu.auth.dto.AuthResponse;
import com.swasthyasetu.auth.dto.LoginRequest;
import com.swasthyasetu.auth.dto.RegisterRequest;
import com.swasthyasetu.common.ApiResponse;
import com.swasthyasetu.config.AppProperties;
import com.swasthyasetu.user.dto.UserDto;
import com.swasthyasetu.user.entity.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AppProperties appProperties;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider, AppProperties appProperties) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.appProperties = appProperties;
    }

    private UserDto mapUserDto(User user) {
        return new UserDto(user.getId(), user.getFullName(), user.getEmail(), 
            user.getRole().name(), user.getPhone(), user.getProfilePicUrl(), 
            user.getAccountStatus().name(), user.getCreatedAt(), null);
    }

    @PostMapping("/register")
    public ApiResponse<UserDto> register(@Valid @RequestBody RegisterRequest req) {
        User user = authService.register(req);
        return new ApiResponse<>(true, "Registration successful", mapUserDto(user));
    }

    @PostMapping("/send-otp")
    public ApiResponse<Void> sendOtp(@RequestBody java.util.Map<String, String> payload) {
        String identifier = payload.get("identifier");
        authService.sendOtp(identifier);
        return new ApiResponse<>(true, "OTP sent successfully", null);
    }

    @PostMapping("/send-otp-signup")
    public ApiResponse<Void> sendOtpSignup(@RequestBody java.util.Map<String, String> payload) {
        String identifier = payload.get("identifier");
        authService.sendOtpForSignup(identifier);
        return new ApiResponse<>(true, "OTP sent successfully", null);
    }

    @PostMapping("/verify-otp")
    public ApiResponse<Void> verifyOtp(@RequestBody java.util.Map<String, String> payload) {
        String phoneNumber = payload.get("phone");
        String otp = payload.get("otp");
        authService.verifyOtp(phoneNumber, otp);
        return new ApiResponse<>(true, "OTP verified successfully", null);
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        User user = authService.authenticate(req);
        
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/api/auth/refresh-token");
        refreshCookie.setMaxAge((int) (appProperties.getJwt().getRefreshExpiryMs() / 1000));
        response.addCookie(refreshCookie);
        
        return new ApiResponse<>(true, "Login successful", 
            new AuthResponse(accessToken, user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.getAccountStatus().name()));
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponse> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
        UUID userId = jwtTokenProvider.extractUserId(refreshToken);
        User user = authService.findById(userId);
        String accessToken = jwtTokenProvider.generateAccessToken(userId, user.getRole().name());
        return new ApiResponse<>(true, "Token refreshed", new AuthResponse(accessToken, userId, user.getFullName(), user.getEmail(), user.getRole().name(), user.getAccountStatus().name()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/api/auth/refresh-token");
        response.addCookie(refreshCookie);
        return new ApiResponse<>(true, "Logged out successfully", null);
    }
}
