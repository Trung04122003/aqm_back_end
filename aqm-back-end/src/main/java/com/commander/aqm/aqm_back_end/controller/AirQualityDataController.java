// src/main/java/com/commander/aqm/aqm_back_end/controller/AirQualityDataController.java
package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.AirQualityResponseDto;
import com.commander.aqm.aqm_back_end.model.AirQualityData;
import com.commander.aqm.aqm_back_end.model.Location;
import com.commander.aqm.aqm_back_end.repository.AirQualityDataRepository;
import com.commander.aqm.aqm_back_end.repository.LocationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/data")
@Tag(name = "Air Quality Data", description = "Real-time and historical air quality data")
@RequiredArgsConstructor
public class AirQualityDataController {

    private final AirQualityDataRepository dataRepo;
    private final LocationRepository locationRepo;

    /**
     * ✅ FIXED: Get air quality data with proper response structure
     * Frontend expects: { current: {...}, history: [...] }
     */
    @Operation(summary = "Get air quality data for location with history")
    @GetMapping
    public ResponseEntity<AirQualityResponseDto> getData(
            @Parameter(description = "Location ID", required = true)
            @RequestParam Long locationId,

            @Parameter(description = "Time range (e.g., 24h, 7d, 30d)", example = "24h")
            @RequestParam(defaultValue = "24h") String range
    ) {
        try {
            log.info("📊 Fetching AQI data for location: {}, range: {}", locationId, range);

            // Parse time range
            LocalDateTime startTime = LocalDateTime.now().minusHours(parseHours(range));

            // Get data from database
            List<AirQualityData> data = dataRepo.findByLocationIdAndTimestampUtcAfter(locationId, startTime);

            log.info("✅ Found {} data points for location {}", data.size(), locationId);

            // Convert to standardized response DTO
            AirQualityResponseDto response = AirQualityResponseDto.from(data);

            // Log response structure
            if (response.getCurrent() != null) {
                log.info("📈 Current AQI: {}, PM2.5: {}",
                        response.getCurrent().getAqi(),
                        response.getCurrent().getPm25());
            } else {
                log.warn("⚠️ No current data available for location {}", locationId);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error fetching AQI data: {}", e.getMessage(), e);

            // Return empty response instead of error
            return ResponseEntity.ok(AirQualityResponseDto.builder()
                    .current(null)
                    .history(List.of())
                    .build());
        }
    }

    /**
     * ✅ Parse time range string to hours
     * Examples: "24h" -> 24, "7d" -> 168, "30d" -> 720
     */
    private long parseHours(String range) {
        try {
            if (range.endsWith("h")) {
                return Long.parseLong(range.replace("h", ""));
            } else if (range.endsWith("d")) {
                return Long.parseLong(range.replace("d", "")) * 24;
            } else if (range.endsWith("w")) {
                return Long.parseLong(range.replace("w", "")) * 24 * 7;
            }
            return 24L; // Default to 24 hours
        } catch (NumberFormatException e) {
            log.warn("⚠️ Invalid range format: {}, using default 24h", range);
            return 24L;
        }
    }

    @Operation(summary = "Get latest AQI for all locations")
    @GetMapping("/all-latest")
    public ResponseEntity<Map<Long, Integer>> getAllLatestAqi() {
        try {
            log.info("📊 Fetching latest AQI for all locations");

            // Get all locations
            List<Location> locations = locationRepo.findAll();

            Map<Long, Integer> aqiMap = new HashMap<>();

            for (Location location : locations) {
                // Get latest AQI data for this location
                Optional<AirQualityData> latestData = dataRepo
                        .findTopByLocationIdOrderByTimestampUtcDesc(location.getId());

                if (latestData.isPresent() && latestData.get().getAqi() != null) {
                    aqiMap.put(location.getId(), latestData.get().getAqi());
                }
            }

            log.info("✅ Returned AQI data for {} locations", aqiMap.size());

            return ResponseEntity.ok(aqiMap);

        } catch (Exception e) {
            log.error("❌ Error fetching all latest AQI: {}", e.getMessage());
            return ResponseEntity.status(500).body(new HashMap<>());
        }
    }
}