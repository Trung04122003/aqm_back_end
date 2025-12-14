// src/main/java/com/commander/aqm/aqm_back_end/controller/WeatherDataController.java
package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.WeatherDataDto;
import com.commander.aqm.aqm_back_end.model.Location;
import com.commander.aqm.aqm_back_end.model.WeatherData;
import com.commander.aqm.aqm_back_end.repository.LocationRepository;
import com.commander.aqm.aqm_back_end.service.RealTimeWeatherService;
import com.commander.aqm.aqm_back_end.service.WeatherDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 🌤️ Weather Data Controller with Smart Real-Time Fetching
 */
@Slf4j
@RestController
@RequestMapping("/api/weather")
@Tag(name = "Weather APIs", description = "Weather data by location")
@RequiredArgsConstructor
public class WeatherDataController {

    private final WeatherDataService weatherDataService;
    private final RealTimeWeatherService realTimeWeatherService;
    private final LocationRepository locationRepo;

    /**
     * 🌤️ SMART WEATHER ENDPOINT
     * - Returns latest weather from DB if fresh (< 30 min old)
     * - Auto-fetches new data if stale or missing
     * - Always returns real-time data to Dashboard
     */
    @GetMapping
    @Operation(summary = "Get current weather (auto-refreshes if stale)")
    public ResponseEntity<List<WeatherDataDto>> getWeatherByLocation(
            @RequestParam("location") Long locationId
    ) {
        try {
            log.info("🌤️ Getting weather for location: {}", locationId);

            // Get latest weather from DB
            WeatherData latest = realTimeWeatherService.getLatestWeather(locationId);

            // Check if data is stale (older than 30 minutes) or missing
            boolean needsRefresh = latest == null ||
                    Duration.between(latest.getTimestampUtc(), LocalDateTime.now()).toMinutes() > 30;

            if (needsRefresh) {
                log.info("⏰ Weather data is stale or missing, fetching fresh data...");

                try {
                    // Fetch fresh data from OpenWeatherMap
                    Location location = locationRepo.findById(locationId)
                            .orElseThrow(() -> new RuntimeException("Location not found"));

                    latest = realTimeWeatherService.fetchAndSaveWeatherData(location);
                    log.info("✅ Fresh weather data fetched successfully");

                } catch (Exception e) {
                    log.error("❌ Failed to fetch fresh weather, using cached data: {}", e.getMessage());
                    // Continue with stale data if fetch fails
                }
            } else {
                log.info("✅ Using cached weather data (age: {} minutes)",
                        Duration.between(latest.getTimestampUtc(), LocalDateTime.now()).toMinutes());
            }

            // Return as list (Dashboard expects array)
            if (latest != null) {
                return ResponseEntity.ok(List.of(WeatherDataDto.from(latest)));
            } else {
                log.warn("⚠️ No weather data available for location {}", locationId);
                return ResponseEntity.ok(List.of()); // Empty list
            }

        } catch (Exception e) {
            log.error("❌ Error getting weather: {}", e.getMessage(), e);
            return ResponseEntity.ok(List.of()); // Return empty on error
        }
    }

    /**
     * 🔄 MANUAL FETCH ENDPOINT (for "Fetch New Data" button)
     */
    @PostMapping("/fetch/{locationId}")
    @Operation(summary = "Force fetch fresh weather from API")
    public ResponseEntity<?> fetchWeatherForLocation(@PathVariable Long locationId) {
        try {
            log.info("🌍 Manual fetch: Getting fresh weather for location: {}", locationId);

            Location location = locationRepo.findById(locationId)
                    .orElseThrow(() -> new RuntimeException("Location not found"));

            WeatherData weather = realTimeWeatherService.fetchAndSaveWeatherData(location);

            return ResponseEntity.ok(Map.of(
                    "message", "Fresh weather data fetched successfully",
                    "data", WeatherDataDto.from(weather),
                    "timestamp", LocalDateTime.now()
            ));

        } catch (Exception e) {
            log.error("❌ Failed to fetch weather: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of(
                            "error", "Failed to fetch weather data",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * 📊 GET CURRENT WEATHER ONLY (single object, not array)
     */
    @GetMapping("/current/{locationId}")
    @Operation(summary = "Get latest weather record")
    public ResponseEntity<?> getCurrentWeather(@PathVariable Long locationId) {
        try {
            WeatherData latest = realTimeWeatherService.getLatestWeather(locationId);

            if (latest == null) {
                return ResponseEntity.ok(Map.of(
                        "message", "No weather data available. Try fetching new data.",
                        "locationId", locationId
                ));
            }

            return ResponseEntity.ok(WeatherDataDto.from(latest));

        } catch (Exception e) {
            log.error("❌ Error getting current weather: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}