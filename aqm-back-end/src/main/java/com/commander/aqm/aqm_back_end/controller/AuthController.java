package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.AuthResponse;
import com.commander.aqm.aqm_back_end.dto.LoginRequest;
import com.commander.aqm.aqm_back_end.dto.RegisterRequest;
import com.commander.aqm.aqm_back_end.dto.UserDto;
import com.commander.aqm.aqm_back_end.model.AlertThreshold; // ✅ ADD
import com.commander.aqm.aqm_back_end.model.PasswordResetToken;
import com.commander.aqm.aqm_back_end.model.Role;
import com.commander.aqm.aqm_back_end.model.Status;
import com.commander.aqm.aqm_back_end.model.User;
import com.commander.aqm.aqm_back_end.repository.AlertThresholdRepository;
import com.commander.aqm.aqm_back_end.repository.PasswordResetTokenRepository;
import com.commander.aqm.aqm_back_end.repository.UserRepository;
import com.commander.aqm.aqm_back_end.service.EmailService;
import com.commander.aqm.aqm_back_end.service.PasswordResetService;
import com.commander.aqm.aqm_back_end.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional; // ✅ ADD
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login & Registration APIs")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final PasswordResetService resetService;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AlertThresholdRepository thresholdRepo; // ✅ ADD THIS

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // ✅ ENDPOINT 1: User Registration (FIXED)
    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    @Transactional // ✅ ADD: Ensure both user and threshold are saved together
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

        if (userRepo.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        // ✅ Check if email already exists
        if (userRepo.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already taken");
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(Role.USER)
                .status(Status.ACTIVE)
                .emailAlertsEnabled(false) // ✅ Default: no email alerts
                .build();

        userRepo.save(user);

        // ✅ NEW: Create default alert threshold for new user
        AlertThreshold defaultThreshold = AlertThreshold.builder()
                .user(user)
                .pm25Threshold(35.0f)  // WHO guideline: 24-hour mean
                .pm10Threshold(50.0f)  // WHO guideline: 24-hour mean
                .aqiThreshold(100.0f)  // Moderate AQI threshold
                .build();

        thresholdRepo.save(defaultThreshold);

        System.out.println("✅ User registered: " + user.getUsername());
        System.out.println("✅ Default threshold created: PM2.5=" + defaultThreshold.getPm25Threshold()
                + ", PM10=" + defaultThreshold.getPm10Threshold()
                + ", AQI=" + defaultThreshold.getAqiThreshold());

        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "username", user.getUsername(),
                "thresholdCreated", true
        ));
    }

    // ✅ ENDPOINT 2: Admin Registration (FIXED)
    @Operation(summary = "Register a new admin")
    @PostMapping("/register-admin")
    @Transactional // ✅ ADD
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegisterRequest request) {

        if (userRepo.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        if (userRepo.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already taken");
        }

        User admin = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .emailAlertsEnabled(false)
                .build();

        userRepo.save(admin);

        // ✅ NEW: Create default threshold for admin too
        AlertThreshold defaultThreshold = AlertThreshold.builder()
                .user(admin)
                .pm25Threshold(35.0f)
                .pm10Threshold(50.0f)
                .aqiThreshold(100.0f)
                .build();

        thresholdRepo.save(defaultThreshold);

        System.out.println("✅ Admin registered: " + admin.getUsername());

        return ResponseEntity.ok(Map.of(
                "message", "Admin registered successfully",
                "username", admin.getUsername(),
                "thresholdCreated", true
        ));
    }

    // ✅ ENDPOINT 3: Admin Registration (PROTECTED - Production Ready)
    @Operation(summary = "Create new admin (Admin only)")
    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional // ✅ ADD
    public ResponseEntity<?> createAdmin(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {

        if (userRepo.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        if (userRepo.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already taken");
        }

        User admin = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .emailAlertsEnabled(false)
                .build();

        userRepo.save(admin);

        // ✅ NEW: Create default threshold
        AlertThreshold defaultThreshold = AlertThreshold.builder()
                .user(admin)
                .pm25Threshold(35.0f)
                .pm10Threshold(50.0f)
                .aqiThreshold(100.0f)
                .build();

        thresholdRepo.save(defaultThreshold);

        System.out.println("🔒 Admin created by: " + currentUser.getUsername());

        return ResponseEntity.ok(Map.of(
                "message", "Admin created successfully",
                "username", admin.getUsername(),
                "role", admin.getRole(),
                "thresholdCreated", true
        ));
    }

    /**
     * ✅ ENDPOINT 4: Login (FIXED - Now accepts both username and email)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // ✅ FIX: Use findByUsernameOrEmail instead of findByUsername
            User user = userRepo.findByUsernameOrEmail(request.getUsernameOrEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Check if user account is active
            if (user.getStatus() != Status.ACTIVE) {
                return ResponseEntity.status(403)
                        .body("Account is not active. Please contact support.");
            }

            // ✅ Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                return ResponseEntity.status(401).body("Invalid credentials");
            }

            // ✅ Generate JWT token
            String token = jwtUtils.generateToken(user.getUsername());

            // ✅ Log successful login
            System.out.println("✅ User logged in: " + user.getUsername() + " (Role: " + user.getRole() + ")");

            // ✅ Return token and user info
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "user", UserDto.from(user)
            ));

        } catch (Exception e) {
            System.err.println("❌ Login error: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid username/email or password");
        }
    }

    // ==================== NEW PASSWORD RESET ENDPOINTS ====================

    /**
     * ✅ ENDPOINT 5: Forgot Password - Send Reset Email
     * Frontend calls: POST /api/auth/forgot-password
     */
    @Operation(summary = "Request password reset email")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            // Find user by email
            Optional<User> userOpt = userRepo.findByEmail(request.getEmail());

            if (userOpt.isEmpty()) {
                // ✅ Security: Don't reveal if email exists
                return ResponseEntity.ok(Map.of(
                        "message", "If email exists, reset link will be sent"
                ));
            }

            User user = userOpt.get();

            // Generate reset token
            String token = resetService.createToken(user);

            // Build reset link
            String resetLink = frontendUrl + "/reset-password?token=" + token;

            // Send email
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetLink);

            System.out.println("✅ Password reset email sent to: " + user.getEmail());

            return ResponseEntity.ok(Map.of(
                    "message", "Password reset email sent successfully"
            ));

        } catch (Exception e) {
            System.err.println("❌ Forgot password error: " + e.getMessage());
            e.printStackTrace();

            // ✅ Security: Generic message even on error
            return ResponseEntity.ok(Map.of(
                    "message", "If email exists, reset link will be sent"
            ));
        }
    }

    /**
     * ✅ ENDPOINT 6: Validate Reset Token
     * Frontend calls: POST /api/auth/validate-reset-token
     */
    @Operation(summary = "Validate password reset token")
    @PostMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");

            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "valid", false,
                        "message", "Token is required"
                ));
            }

            // Find token in database
            Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);

            if (tokenOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "valid", false,
                        "message", "Invalid token"
                ));
            }

            PasswordResetToken resetToken = tokenOpt.get();

            // Check if token is expired
            if (resetToken.getExpiry().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "valid", false,
                        "message", "Token expired"
                ));
            }

            // Token is valid
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "email", resetToken.getUser().getEmail()
            ));

        } catch (Exception e) {
            System.err.println("❌ Token validation error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", "Invalid token"
            ));
        }
    }

    /**
     * ✅ ENDPOINT 7: Reset Password
     * Frontend calls: POST /api/auth/reset-password
     */
    @Operation(summary = "Reset password with token")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            boolean success = resetService.validateAndResetPassword(
                    request.getToken(),
                    request.getNewPassword()
            );

            if (success) {
                System.out.println("✅ Password reset successful");
                return ResponseEntity.ok(Map.of(
                        "message", "Password reset successful"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Invalid or expired token"
                ));
            }

        } catch (Exception e) {
            System.err.println("❌ Password reset error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Password reset failed: " + e.getMessage()
            ));
        }
    }

    // ==================== OLD ENDPOINTS (Keep for backward compatibility) ====================

    @Deprecated
    @PostMapping("/reset-request")
    public String resetRequest(@RequestBody ResetRequest req) {
        return userRepo.findByEmail(req.getEmail())
                .map(foundUser -> {
                    String token = resetService.createToken(foundUser);
                    return "Reset token: " + token;
                })
                .orElse("No user found with that email.");
    }

    @Deprecated
    @PostMapping("/reset-confirm")
    public String confirmReset(@RequestBody ResetConfirm req) {
        boolean success = resetService.validateAndResetPassword(req.getToken(), req.getNewPassword());
        return success ? "Password reset successful." : "Invalid or expired token.";
    }

    // ==================== REQUEST DTOs ====================

    @Data
    public static class ForgotPasswordRequest {
        private String email;
    }

    @Data
    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;
    }

    @Data
    @Deprecated
    public static class ResetRequest {
        private String email;
    }

    @Data
    @Deprecated
    public static class ResetConfirm {
        private String token;
        private String newPassword;
    }
}