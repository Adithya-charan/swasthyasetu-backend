package com.swasthyasetu.user;

import com.swasthyasetu.common.ApiResponse;
import com.swasthyasetu.common.PageResponse;
import com.swasthyasetu.user.dto.DoctorProfileDto;
import com.swasthyasetu.user.dto.UserDto;
import com.swasthyasetu.user.entity.DoctorProfile;
import com.swasthyasetu.user.entity.Role;
import com.swasthyasetu.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;
import java.util.Objects;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private @NonNull UUID getAuthenticatedUserId() {
        String principal = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Objects.requireNonNull(UUID.fromString(principal));
    }

    private UserDto mapUserDto(@NonNull User user) {
        return new UserDto(user.getId(), user.getFullName(), user.getEmail(), 
                           user.getRole().name(), user.getPhone(), user.getProfilePicUrl(), 
                           user.isActive(), user.getCreatedAt());
    }

    private DoctorProfileDto mapDoctorProfileDto(@NonNull DoctorProfile dp) {
        return new DoctorProfileDto(mapUserDto(Objects.requireNonNull(dp.getUser())), dp.getSpecialization(), 
                                    dp.getQualifications(), dp.getExperienceYears(), 
                                    dp.getLicenseNumber(), dp.getConsultationFee(), dp.getBio());
    }

    @GetMapping("/users/me")
    public ApiResponse<UserDto> getMe() {
        User user = Objects.requireNonNull(userService.getUserById(getAuthenticatedUserId()));
        return new ApiResponse<>(true, "Success", mapUserDto(user));
    }

    @PutMapping("/users/me")
    public ApiResponse<UserDto> updateMe(@RequestParam(required=false) String fullName, 
                                         @RequestParam(required=false) String phone, 
                                         @RequestParam(required=false) String profilePicUrl) {
        User updated = Objects.requireNonNull(userService.updateUserProfile(getAuthenticatedUserId(), fullName, phone, profilePicUrl));
        return new ApiResponse<>(true, "Updated profile", mapUserDto(updated));
    }

    @GetMapping("/doctors")
    public ApiResponse<PageResponse<DoctorProfileDto>> getDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String name,
            @NonNull Pageable pageable) {
        Page<DoctorProfile> page = Objects.requireNonNull(userService.getActiveDoctors(specialization, name, pageable));
        PageResponse<DoctorProfileDto> pr = new PageResponse<>(
            page.getContent().stream()
                .filter(Objects::nonNull)
                .map(this::mapDoctorProfileDto)
                .collect(Collectors.toList()),
            page.getTotalElements(), page.getTotalPages(), page.getNumber()
        );
        return new ApiResponse<>(true, "Doctors fetched", pr);
    }

    @GetMapping("/doctors/{id}")
    public ApiResponse<DoctorProfileDto> getDoctorById(@NonNull @PathVariable UUID id) {
        DoctorProfile dp = Objects.requireNonNull(userService.getDoctorProfile(id));
        return new ApiResponse<>(true, "Doctor fetched", mapDoctorProfileDto(dp));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public ApiResponse<PageResponse<UserDto>> getAllUsers(
            @RequestParam(required = false) Role role, @NonNull Pageable pageable) {
        Page<User> page = Objects.requireNonNull(userService.getAllUsersAdmin(role, pageable));
        PageResponse<UserDto> pr = new PageResponse<>(
            page.getContent().stream()
                .filter(Objects::nonNull)
                .map(this::mapUserDto)
                .collect(Collectors.toList()),
            page.getTotalElements(), page.getTotalPages(), page.getNumber()
        );
        return new ApiResponse<>(true, "Users fetched", pr);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/users/{id}/activate")
    public ApiResponse<Void> activateUser(@NonNull @PathVariable UUID id, @RequestParam boolean active) {
        userService.setUserActiveStatus(id, active);
        return new ApiResponse<>(true, "User status updated", null);
    }
}
