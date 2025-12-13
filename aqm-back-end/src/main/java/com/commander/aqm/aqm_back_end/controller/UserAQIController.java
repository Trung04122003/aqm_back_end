// src/main/java/com/commander/aqm/aqm_back_end/controller/UserAQIController.java
package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.model.AirQualityData;
import com.commander.aqm.aqm_back_end.service.RealTimeAQIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 🌍 User-accessible AQI endpoints
 * Non-admin users can fetch real-time data
 */
@Slf4j
@RestController
@RequestMapping("/api/aqi")
@Tag(name = "AQI APIs", description = "Real-time air quality data for users")
@RequiredArgsConstructor
public class UserAQIController {

    private final RealTimeAQIService realTimeAQIService;

    /**
     * 🔄 Fetch fresh AQI data for a location
     * Available to all authenticated users
     */
    @PostMapping("/fetch/{locationId}")
    @Operation(summary = "Fetch fresh AQI data from external API")
    public ResponseEntity<?> fetchAQIForLocation(@PathVariable Long locationId) {
        try {
            log.info("🌍 User requesting fresh AQI data for location: {}", locationId);

            // Trigger fetch from external API
            realTimeAQIService.manualFetchForLocation(locationId);

            // Get the latest data
            AirQualityData latest = realTimeAQIService.getLatestAQI(locationId);

            return ResponseEntity.ok(Map.of(
                    "message", "Fresh AQI data fetched successfully",
                    "locationId", locationId,
                    "data", latest != null ? latest : "Data will be available shortly",
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            log.error("❌ Failed to fetch AQI for location {}: {}", locationId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "error", "Failed to fetch AQI data",
                            "message", e.getMessage(),
                            "locationId", locationId
                    ));
        }
    }

    /**
     * 📊 Get latest AQI data for a location
     */
    @GetMapping("/latest/{locationId}")
    @Operation(summary = "Get latest AQI data from database")
    public ResponseEntity<?> getLatestAQI(@PathVariable Long locationId) {
        try {
            AirQualityData latest = realTimeAQIService.getLatestAQI(locationId);

            if (latest == null) {
                return ResponseEntity.ok(Map.of(
                        "message", "No data available. Try fetching new data.",
                        "locationId", locationId
                ));
            }

            return ResponseEntity.ok(latest);

        } catch (Exception e) {
            log.error("❌ Failed to get latest AQI: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}