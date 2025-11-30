// src/main/java/com/commander/aqm/aqm_back_end/controller/ProfileController.java
package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.ProfileUpdateRequest;
import com.commander.aqm.aqm_back_end.dto.UserDto;
import com.commander.aqm.aqm_back_end.model.User;
import com.commander.aqm.aqm_back_end.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Profile", description = "User profile management endpoints")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final com.commander.aqm.aqm_back_end.service.UserService userService;

    /**
     * ✅ GET current user profile
     */
    @Operation(summary = "Get current user profile")
    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByUsername(principal.getUsername());
        UserDto profile = profileService.getProfile(user);
        return ResponseEntity.ok(profile);
    }

    /**
     * ✅ UPDATE user profile
     */
    @Operation(summary = "Update user profile")
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        try {
            User currentUser = userService.getByUsername(principal.getUsername());
            UserDto updatedProfile = profileService.updateProfile(currentUser, request);

            return ResponseEntity.ok(Map.of(
                    "message", "Profile updated successfully",
                    "user", updatedProfile
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * ✅ CHANGE password
     */
    @Operation(summary = "Change password")
    @PostMapping("/profile/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        try {
            User currentUser = userService.getByUsername(principal.getUsername());
            profileService.changePassword(currentUser, request.getCurrentPassword(), request.getNewPassword());

            return ResponseEntity.ok(Map.of(
                    "message", "Password changed successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * DTO for password change
     */
    @Data
    public static class ChangePasswordRequest {
        @jakarta.validation.constraints.NotBlank(message = "Current password is required")
        private String currentPassword;

        @jakarta.validation.constraints.NotBlank(message = "New password is required")
        @jakarta.validation.constraints.Size(min = 6, message = "New password must be at least 6 characters")
        private String newPassword;

        @jakarta.validation.constraints.NotBlank(message = "Confirm password is required")
        private String confirmPassword;
    }
}