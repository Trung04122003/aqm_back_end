package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.ForecastDto;
import com.commander.aqm.aqm_back_end.model.Forecast;
import com.commander.aqm.aqm_back_end.service.ForecastService;
import com.commander.aqm.aqm_back_end.service.ForecastGenerationService; // ✅ ADD
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;
    private final ForecastGenerationService generationService; // ✅ ADD

    @GetMapping
    public List<ForecastDto> getForecast(@RequestParam("location") Long locationId) {
        return forecastService.getForecastByLocation(locationId).stream()
                .map(ForecastDto::from)
                .toList();
    }

    /**
     * ✅ NEW: Generate forecast based on real-time data
     */
    @PostMapping("/generate/{locationId}")
    public ResponseEntity<?> generateForecast(@PathVariable Long locationId) {
        try {
            System.out.println("🔮 Generating forecast for location: " + locationId);

            List<Forecast> forecasts = generationService.generateForecast(locationId);

            return ResponseEntity.ok(Map.of(
                    "message", "Forecast generated successfully",
                    "count", forecasts.size(),
                    "forecasts", forecasts.stream().map(ForecastDto::from).toList()
            ));

        } catch (RuntimeException e) {
            System.err.println("❌ Forecast generation error: " + e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to generate forecast: " + e.getMessage()
            ));
        }
    }
}