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
            user.isActive(), user.getCreatedAt());
    }

    @PostMapping("/register")
    public ApiResponse<UserDto> register(@Valid @RequestBody RegisterRequest req) {
        User user = authService.register(req);
        return new ApiResponse<>(true, "Registration successful", mapUserDto(user));
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
        
        return new ApiResponse<>(true, "Login successful", new AuthResponse(accessToken));
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponse> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
        UUID userId = jwtTokenProvider.extractUserId(refreshToken);
        // Note: in prod checking the db state of user is recommended here.
        String accessToken = jwtTokenProvider.generateAccessToken(userId, "PATIENT"); // placeholder role
        return new ApiResponse<>(true, "Token refreshed", new AuthResponse(accessToken));
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
