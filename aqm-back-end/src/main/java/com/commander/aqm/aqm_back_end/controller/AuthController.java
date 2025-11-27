package com.commander.aqm.aqm_back_end.controller;

//import com.commander.aqm.aqm_back_end.dto.AuthRequest;
import com.commander.aqm.aqm_back_end.dto.AuthResponse;
import com.commander.aqm.aqm_back_end.dto.LoginRequest;
import com.commander.aqm.aqm_back_end.dto.RegisterRequest;
import com.commander.aqm.aqm_back_end.dto.UserDto;
import com.commander.aqm.aqm_back_end.model.Role;
import com.commander.aqm.aqm_back_end.model.Status;
import com.commander.aqm.aqm_back_end.model.User;
import com.commander.aqm.aqm_back_end.repository.UserRepository;
import com.commander.aqm.aqm_back_end.service.PasswordResetService;
import com.commander.aqm.aqm_back_end.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login & Registration APIs")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final PasswordResetService resetService;

    // ✅ ENDPOINT 1: User Registration (PUBLIC)
    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

        if (userRepo.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(Role.USER) // ✅ Explicitly set USER role
                .status(Status.ACTIVE)
                .build();

        userRepo.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    // ✅ ENDPOINT 2: Admin Registration (PUBLIC - Self Registration)
    @Operation(summary = "Register a new admin")
    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegisterRequest request) {

        if (userRepo.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        // ⚠️ OPTION A: Public admin registration (KHÔNG AN TOÀN - Chỉ dùng dev)
        User admin = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(Role.ADMIN) // ✅ Set ADMIN role
                .status(Status.ACTIVE)
                .build();

        userRepo.save(admin);
        return ResponseEntity.ok("Admin registered successfully");
    }

    // ✅ ENDPOINT 3: Admin Registration (PROTECTED - Production Ready)
    @Operation(summary = "Create new admin (Admin only)")
    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('ADMIN')") // ✅ Chỉ Admin hiện tại mới tạo được
    public ResponseEntity<?> createAdmin(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {

        if (userRepo.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        User admin = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();

        userRepo.save(admin);

        // Log audit trail
        System.out.println("🔐 Admin created by: " + currentUser.getUsername());

        return ResponseEntity.ok(Map.of(
                "message", "Admin created successfully",
                "username", admin.getUsername(),
                "role", admin.getRole()
        ));
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        User user = userRepo.findByUsername(request.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        String token = jwtUtils.generateToken(user.getUsername());

        // ✅ CONSISTENT RESPONSE FORMAT
        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", UserDto.from(user)
        ));
    }


//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
//        if (userRepo.existsByUsername(request.getUsername())) {
//            return ResponseEntity.badRequest().body("Username already taken");
//        }
//
//        User user = User.builder()
//                .username(request.getUsername())
//                .passwordHash(passwordEncoder.encode(request.getPassword()))
//                .email(request.getEmail())
//                .fullName(request.getFullName())
//                .build();
//
//        userRepo.save(user);
//        return ResponseEntity.ok("Registered successfully");
//    }

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
//        User user = userRepo.findByUsername(request.getUsername())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
//            return ResponseEntity.status(401).body("Invalid credentials");
//        }
//
//        String token = jwtUtils.generateToken(user.getUsername());
//        return ResponseEntity.ok(new AuthResponse(token));
//    }


    @PostMapping("/reset-request")
    public String resetRequest(@RequestBody ResetRequest req) {
        return userRepo.findByEmail(req.getEmail())
                .map(foundUser -> {
                    String token = resetService.createToken(foundUser);
                    return "Reset token: " + token;
                })
                .orElse("No user found with that email.");
    }

    @PostMapping("/reset-confirm")
    public String confirmReset(@RequestBody ResetConfirm req) {
        boolean success = resetService.validateAndResetPassword(req.getToken(), req.getNewPassword());
        return success ? "Password reset successful." : "Invalid or expired token.";
    }

    @Data
    public static class ResetRequest {
        private String email;
    }

    @Data
    public static class ResetConfirm {
        private String token;
        private String newPassword;
    }
}
