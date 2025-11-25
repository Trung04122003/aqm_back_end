// src/main/java/com/commander/aqm/aqm_back_end/controller/AirQualityDataController.java
package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.AirQualityResponseDto;
import com.commander.aqm.aqm_back_end.model.AirQualityData;
import com.commander.aqm.aqm_back_end.repository.AirQualityDataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data")
@Tag(name = "Air Quality Data", description = "Real-time and historical air quality data")
@RequiredArgsConstructor
public class AirQualityDataController {

    private final AirQualityDataRepository dataRepo;

    @Operation(summary = "Get air quality data for location")
    @GetMapping
    public ResponseEntity<AirQualityResponseDto> getData(
            @Parameter(description = "Location ID", required = true)
            @RequestParam Long locationId,

            @Parameter(description = "Time range (e.g., 24h, 7d, 30d)", example = "24h")
            @RequestParam(defaultValue = "24h") String range
    ) {
        // ✅ Parse time range
        LocalDateTime start = LocalDateTime.now().minusHours(parseHours(range));

        // ✅ Get data from repository
        List<AirQualityData> data = dataRepo.findByLocationIdAndTimestampUtcAfter(locationId, start);

        // ✅ Convert to standardized DTO
        AirQualityResponseDto response = AirQualityResponseDto.from(data);

        return ResponseEntity.ok(response);
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
            // Default to 24 hours if format is invalid
            return 24L;
        } catch (NumberFormatException e) {
            return 24L;
        }
    }
}