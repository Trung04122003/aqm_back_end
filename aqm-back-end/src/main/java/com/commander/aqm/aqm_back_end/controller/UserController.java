package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.UserDto;
import com.commander.aqm.aqm_back_end.model.AirQualityData;
import com.commander.aqm.aqm_back_end.model.Alert;
import com.commander.aqm.aqm_back_end.model.Location;
import com.commander.aqm.aqm_back_end.model.User;
import com.commander.aqm.aqm_back_end.repository.UserRepository;
import com.commander.aqm.aqm_back_end.service.EmailService;
import com.commander.aqm.aqm_back_end.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User APIs", description = "Endpoints for user profile and identity")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepo;
    private final EmailService emailService;

    @Operation(summary = "Get current logged-in user")
    @GetMapping("/me")
    public UserDto getCurrentUser(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByUsername(principal.getUsername());
        return UserDto.from(user);
    }

    @GetMapping("/test-email")
    public ResponseEntity<?> testEmail() {
        User testUser = userRepo.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create fake alert
        Alert testAlert = Alert.builder()
                .pollutant("PM2.5")
                .value(75.5f)
                .triggeredAt(LocalDateTime.now())
                .build();

        // Mock aqData
        AirQualityData mockData = new AirQualityData();
        Location mockLocation = new Location();
        mockLocation.setName("Da Nang");
        mockData.setLocation(mockLocation);
        testAlert.setAqData(mockData);

        emailService.sendAlertEmail(testUser, testAlert);
        return ResponseEntity.ok("Test email sent to: " + testUser.getEmail());
    }
}
