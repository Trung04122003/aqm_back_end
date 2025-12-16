package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.AirQualityData;
import com.commander.aqm.aqm_back_end.model.Forecast;
import com.commander.aqm.aqm_back_end.model.Location;
import com.commander.aqm.aqm_back_end.repository.AirQualityDataRepository;
import com.commander.aqm.aqm_back_end.repository.ForecastRepository;
import com.commander.aqm.aqm_back_end.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForecastGenerationService {

    private final AirQualityDataRepository airQualityRepo;
    private final ForecastRepository forecastRepo;
    private final LocationRepository locationRepo;

    /**
     * 🔮 Generate forecast for next 48 hours based on real-time data
     */
    @Transactional
    public List<Forecast> generateForecast(Long locationId) {
        log.info("🔮 Generating forecast for location: {}", locationId);

        // Get location
        Location location = locationRepo.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        // Get recent data (last 24 hours) for trend analysis
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        List<AirQualityData> recentData = airQualityRepo
                .findByLocationIdAndTimestampUtcAfter(locationId, last24h);

        if (recentData.isEmpty()) {
            throw new RuntimeException("No recent data available for forecasting");
        }

        log.info("📊 Found {} data points for trend analysis", recentData.size());

        // Calculate trends
        double avgPm25 = recentData.stream()
                .filter(d -> d.getPm25() != null)
                .mapToDouble(AirQualityData::getPm25)
                .average().orElse(0.0);

        double avgPm10 = recentData.stream()
                .filter(d -> d.getPm10() != null)
                .mapToDouble(AirQualityData::getPm10)
                .average().orElse(0.0);

        double avgAqi = recentData.stream()
                .filter(d -> d.getAqi() != null)
                .mapToDouble(AirQualityData::getAqi)
                .average().orElse(0.0);

        // Calculate trend (increasing/decreasing)
        double trendFactor = calculateTrend(recentData);

        log.info("📈 Trend analysis: avgPM2.5={}, avgPM10={}, avgAQI={}, trend={}",
                avgPm25, avgPm10, avgAqi, trendFactor);

        // Delete old forecasts for this location
        forecastRepo.deleteByLocationId(locationId);

        // Generate forecasts for next 48 hours (every 3 hours = 16 predictions)
        List<Forecast> forecasts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= 16; i++) {
            LocalDateTime forecastTime = now.plusHours(i * 3);

            // Apply trend and random variation
            double hourlyTrend = trendFactor * i * 0.1; // Gradual change
            double randomVariation = (Math.random() - 0.5) * 5; // ±2.5 variation

            float predictedPm25 = (float) (avgPm25 + hourlyTrend + randomVariation);
            float predictedPm10 = (float) (avgPm10 + hourlyTrend * 1.2 + randomVariation);
            float predictedAqi = (float) (avgAqi + hourlyTrend * 2 + randomVariation);

            // Ensure non-negative values
            predictedPm25 = Math.max(0, predictedPm25);
            predictedPm10 = Math.max(0, predictedPm10);
            predictedAqi = Math.max(0, predictedAqi);

            Forecast forecast = Forecast.builder()
                    .location(location)
                    .timestampUtc(forecastTime)
                    .predictedPm25(predictedPm25)
                    .predictedPm10(predictedPm10)
                    .predictedAqi(Float.valueOf((int) predictedAqi))
                    .modelVersion("Linear-Trend-v1.0")
                    .build();

            forecasts.add(forecast);
        }

        forecastRepo.saveAll(forecasts);

        log.info("✅ Generated {} forecasts for location: {}", forecasts.size(), location.getName());

        return forecasts;
    }

    /**
     * Calculate trend factor (positive = increasing, negative = decreasing)
     */
    private double calculateTrend(List<AirQualityData> data) {
        if (data.size() < 2) return 0.0;

        // Compare first half vs second half average
        int mid = data.size() / 2;

        double firstHalfAvg = data.subList(0, mid).stream()
                .filter(d -> d.getAqi() != null)
                .mapToDouble(AirQualityData::getAqi)
                .average().orElse(0.0);

        double secondHalfAvg = data.subList(mid, data.size()).stream()
                .filter(d -> d.getAqi() != null)
                .mapToDouble(AirQualityData::getAqi)
                .average().orElse(0.0);

        return (secondHalfAvg - firstHalfAvg) / firstHalfAvg;
    }
}