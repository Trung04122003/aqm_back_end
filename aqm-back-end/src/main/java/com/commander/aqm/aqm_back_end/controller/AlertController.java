// src/main/java/com/commander/aqm/aqm_back_end/controller/AlertController.java
package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.AlertDto;
import com.commander.aqm.aqm_back_end.security.JwtService;
import com.commander.aqm.aqm_back_end.service.AlertService;
import com.commander.aqm.aqm_back_end.service.AlertMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts", description = "Alert management endpoints")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final AlertMonitoringService alertMonitoringService;
    private final JwtService jwtService;

    /**
     * 📋 Get all alerts for current user
     */
    @Operation(summary = "Get all alerts for current user")
    @GetMapping
    public ResponseEntity<List<AlertDto>> getAll(@RequestHeader("Authorization") String auth) {
        var user = jwtService.extractUser(auth);

        log.info("📋 Getting all alerts for user: {}", user.getUsername());

        List<AlertDto> alerts = alertService.getAllAlerts(user).stream()
                .map(AlertDto::from)
                .collect(Collectors.toList());

        log.info("✅ Returned {} alerts", alerts.size());
        return ResponseEntity.ok(alerts);
    }

    /**
     * 🔔 Get unread alerts only
     */
    @Operation(summary = "Get unread alerts for current user")
    @GetMapping("/unread")
    public ResponseEntity<List<AlertDto>> getUnread(@RequestHeader("Authorization") String auth) {
        var user = jwtService.extractUser(auth);

        log.info("🔔 Getting unread alerts for user: {}", user.getUsername());

        List<AlertDto> alerts = alertService.getUnreadAlerts(user).stream()
                .map(AlertDto::from)
                .collect(Collectors.toList());

        log.info("✅ Returned {} unread alerts", alerts.size());
        return ResponseEntity.ok(alerts);
    }

    /**
     * ✅ Mark alert as read
     */
    @Operation(summary = "Mark alert as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<AlertDto> markAsRead(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth
    ) {
        var user = jwtService.extractUser(auth);

        log.info("✅ Marking alert {} as read for user: {}", id, user.getUsername());

        var alert = alertService.markAsRead(id, user);
        return ResponseEntity.ok(AlertDto.from(alert));
    }

    /**
     * 🔄 Manual trigger alert check for current user
     */
    @Operation(summary = "Manually check for new alerts")
    @PostMapping("/check")
    public ResponseEntity<?> manualCheck(@RequestHeader("Authorization") String auth) {
        try {
            var user = jwtService.extractUser(auth);

            log.info("🔍 Manual alert check triggered by user: {}", user.getUsername());

            alertMonitoringService.checkAllLocationsForUser(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Alert check completed",
                    "user", user.getUsername()
            ));

        } catch (Exception e) {
            log.error("❌ Manual alert check failed: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 📊 Get alert statistics
     */
    @Operation(summary = "Get alert statistics for user")
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestHeader("Authorization") String auth) {
        try {
            var user = jwtService.extractUser(auth);

            List<AlertDto> allAlerts = alertService.getAllAlerts(user).stream()
                    .map(AlertDto::from)
                    .toList();

            long unreadCount = allAlerts.stream().filter(a -> !a.isRead()).count();
            long pm25Count = allAlerts.stream().filter(a -> "PM2.5".equals(a.getPollutant())).count();
            long pm10Count = allAlerts.stream().filter(a -> "PM10".equals(a.getPollutant())).count();
            long aqiCount = allAlerts.stream().filter(a -> "AQI".equals(a.getPollutant())).count();

            return ResponseEntity.ok(Map.of(
                    "total", allAlerts.size(),
                    "unread", unreadCount,
                    "read", allAlerts.size() - unreadCount,
                    "byPollutant", Map.of(
                            "PM2.5", pm25Count,
                            "PM10", pm10Count,
                            "AQI", aqiCount
                    )
            ));

        } catch (Exception e) {
            log.error("❌ Failed to get stats: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}