package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.model.AlertThreshold;
import com.commander.aqm.aqm_back_end.model.User;
import com.commander.aqm.aqm_back_end.repository.AlertThresholdRepository;
import com.commander.aqm.aqm_back_end.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/thresholds")
@RequiredArgsConstructor
public class AlertThresholdController {

    private final AlertThresholdRepository repo;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> setThreshold(@RequestHeader("Authorization") String auth, @RequestBody AlertThreshold body) {
        try {
            User user = jwtService.extractUser(auth);
            body.setUser(user);

            // ✅ Check if threshold exists, update instead of creating new
            AlertThreshold saved = repo.findByUser(user)
                    .map(existing -> {
                        existing.setPm25Threshold(body.getPm25Threshold());
                        existing.setPm10Threshold(body.getPm10Threshold());
                        existing.setAqiThreshold(body.getAqiThreshold());
                        return repo.save(existing);
                    })
                    .orElseGet(() -> repo.save(body));

            System.out.println("✅ Threshold saved for user: " + user.getUsername());
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            System.err.println("❌ Error saving threshold: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to save threshold: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestHeader("Authorization") String auth) {
        try {
            User user = jwtService.extractUser(auth);

            // ✅ If no threshold exists, create default one
            AlertThreshold threshold = repo.findByUser(user)
                    .orElseGet(() -> {
                        System.out.println("⚠️ No threshold found for user: " + user.getUsername() + ", creating default...");

                        AlertThreshold defaultThreshold = AlertThreshold.builder()
                                .user(user)
                                .pm25Threshold(35.0f)
                                .pm10Threshold(50.0f)
                                .aqiThreshold(100.0f)
                                .build();

                        return repo.save(defaultThreshold);
                    });

            return ResponseEntity.ok(threshold);

        } catch (Exception e) {
            System.err.println("❌ Error getting threshold: " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to get threshold: " + e.getMessage());
        }
    }
}