package com.commander.aqm.aqm_back_end.controller;

//import com.commander.aqm.aqm_back_end.dto.AuthRequest;
import com.commander.aqm.aqm_back_end.dto.AuthResponse;
import com.commander.aqm.aqm_back_end.dto.LoginRequest;
import com.commander.aqm.aqm_back_end.dto.RegisterRequest;
import com.commander.aqm.aqm_back_end.dto.UserDto;
import com.commander.aqm.aqm_back_end.model.User;
import com.commander.aqm.aqm_back_end.repository.UserRepository;
import com.commander.aqm.aqm_back_end.service.PasswordResetService;
import com.commander.aqm.aqm_back_end.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        if (userRepo.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .build();

        userRepo.save(user);
        return ResponseEntity.ok("Registered successfully");
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        User user = userRepo.findByUsername(request.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        String token = jwtUtils.generateToken(user.getUsername());
        // ✅ TRẢ VỀ ĐÚNG FORMAT
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", UserDto.from(user)); // convert to DTO

        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("status", 200);
        wrapper.put("data", response);

        return ResponseEntity.ok(wrapper);
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
