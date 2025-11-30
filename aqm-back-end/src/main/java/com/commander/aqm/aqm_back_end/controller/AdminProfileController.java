// src/main/java/com/commander/aqm/aqm_back_end/controller/AdminProfileController.java
package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.ProfileUpdateRequest;
import com.commander.aqm.aqm_back_end.dto.UserDto;
import com.commander.aqm.aqm_back_end.model.User;
import com.commander.aqm.aqm_back_end.service.ProfileService;
import com.commander.aqm.aqm_back_end.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Profile", description = "Admin profile management")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    /**
     * ✅ GET admin profile
     */
    @Operation(summary = "Get admin profile")
    @GetMapping("/profile")
    public ResponseEntity<UserDto> getAdminProfile(@AuthenticationPrincipal UserDetails principal) {
        User admin = userService.getByUsername(principal.getUsername());
        UserDto profile = profileService.getProfile(admin);
        return ResponseEntity.ok(profile);
    }

    /**
     * ✅ UPDATE admin profile
     */
    @Operation(summary = "Update admin profile")
    @PutMapping("/profile")
    public ResponseEntity<?> updateAdminProfile(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        try {
            User currentAdmin = userService.getByUsername(principal.getUsername());
            UserDto updatedProfile = profileService.updateProfile(currentAdmin, request);

            return ResponseEntity.ok(Map.of(
                    "message", "🎅 Admin profile updated successfully!",
                    "user", updatedProfile
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * ✅ CHANGE admin password
     */
    @Operation(summary = "Change admin password")
    @PostMapping("/profile/change-password")
    public ResponseEntity<?> changeAdminPassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ProfileController.ChangePasswordRequest request
    ) {
        try {
            // Validate passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "New password and confirm password do not match"
                ));
            }

            User currentAdmin = userService.getByUsername(principal.getUsername());
            profileService.changePassword(currentAdmin, request.getCurrentPassword(), request.getNewPassword());

            return ResponseEntity.ok(Map.of(
                    "message", "🎄 Admin password changed successfully!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}