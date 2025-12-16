// src/main/java/com/commander/aqm/aqm_back_end/service/RealTimeAQIService.java
package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.AirQualityData;
import com.commander.aqm.aqm_back_end.model.Location;
import com.commander.aqm.aqm_back_end.model.Sensor;
import com.commander.aqm.aqm_back_end.repository.AirQualityDataRepository;
import com.commander.aqm.aqm_back_end.repository.LocationRepository;
import com.commander.aqm.aqm_back_end.repository.SensorRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RealTimeAQIService {

    private final LocationRepository locationRepo;
    private final AirQualityDataRepository aqDataRepo;
    private final SensorRepository sensorRepo;  // ✅ ADD THIS
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openweather.api.key:8d5af07bbcf8969b864f267d5465e1d2}")
    private String openWeatherApiKey;

    /**
     * 🔄 Auto-fetch every 30 minutes (disabled by default in dev)
     */
    @Scheduled(cron = "0 */30 * * * *")
    public void fetchAllLocationsData() {
        log.info("🌍 Starting scheduled AQI data fetch...");

        List<Location> locations = locationRepo.findAll();

        for (Location location : locations) {
            try {
                fetchAndSaveAQIData(location);
                Thread.sleep(2000); // Rate limit: 2 seconds between requests
            } catch (Exception e) {
                log.error("❌ Failed to fetch data for location: {}", location.getName(), e);
            }
        }

        log.info("✅ Scheduled AQI fetch completed for {} locations", locations.size());
    }

    /**
     * 📡 Fetch data using OpenWeatherMap API
     */
    public AirQualityData fetchAndSaveAQIData(Location location) {
        try {
            log.info("🔍 Fetching AQI data for: {} (lat={}, lon={})",
                    location.getName(), location.getLatitude(), location.getLongitude());

            // Validate API key
            if (openWeatherApiKey == null || openWeatherApiKey.isEmpty() || openWeatherApiKey.equals("YOUR_API_KEY_HERE")) {
                log.error("❌ OpenWeatherMap API key not configured!");
                throw new RuntimeException("API key not configured. Please set OPENWEATHER_API_KEY in application.yml");
            }

            String url = String.format(
                    "https://api.openweathermap.org/data/2.5/air_pollution?lat=%f&lon=%f&appid=%s",
                    location.getLatitude(),
                    location.getLongitude(),
                    openWeatherApiKey
            );

            log.info("📞 Calling OpenWeatherMap API: {}", url.replace(openWeatherApiKey, "***"));

            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isEmpty()) {
                throw new RuntimeException("Empty response from OpenWeatherMap API");
            }

            log.info("✅ Received response from OpenWeatherMap");

            JsonNode root = objectMapper.readTree(response);
            JsonNode airData = root.path("list").get(0);

            if (airData == null) {
                throw new RuntimeException("Invalid response format from OpenWeatherMap");
            }

            // Extract pollutants
            JsonNode components = airData.path("components");
            int owAqi = airData.path("main").path("aqi").asInt();

            // Convert OpenWeather AQI (1-5) to US AQI (0-500)
            double pm25Value = components.path("pm2_5").asDouble();
            int usAqi = convertToUSAQI(owAqi, pm25Value);

            // ✅ FIXED: Find sensor for this location
            Sensor sensor = sensorRepo.findAll().stream()
                    .filter(s -> s.getLocation().getId().equals(location.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No sensor found for location: " + location.getName()));

            log.info("🔧 Using sensor: {} for location: {}", sensor.getSerialNumber(), location.getName());

            // Create and save data
            AirQualityData aqData = AirQualityData.builder()
                    .location(location)
                    .sensor(sensor)  // ✅ ADD sensor
                    .timestampUtc(LocalDateTime.now())
                    .pm25((float) pm25Value)
                    .pm10((float) components.path("pm10").asDouble())
                    .no2((float) components.path("no2").asDouble())
                    .so2((float) components.path("so2").asDouble())
                    .co((float) components.path("co").asDouble())
                    .o3((float) components.path("o3").asDouble())
                    .aqi(usAqi)
                    .build();

            aqDataRepo.save(aqData);

            log.info("✅ Saved AQI data for {}: PM2.5={}, AQI={}",
                    location.getName(), aqData.getPm25(), aqData.getAqi());

            return aqData;

        } catch (Exception e) {
            log.error("❌ Error fetching OpenWeather data for {}: {}", location.getName(), e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch AQI data: " + e.getMessage(), e);
        }
    }

    /**
     * 🔄 Convert OpenWeather AQI (1-5) to US AQI (0-500)
     */
    private int convertToUSAQI(int owAqi, double pm25) {
        // Use PM2.5 concentration to calculate US AQI
        if (pm25 >= 0 && pm25 < 12.1) {
            return (int) ((50 - 0) / (12.0 - 0) * (pm25 - 0) + 0);
        } else if (pm25 >= 12.1 && pm25 < 35.5) {
            return (int) ((100 - 51) / (35.4 - 12.1) * (pm25 - 12.1) + 51);
        } else if (pm25 >= 35.5 && pm25 < 55.5) {
            return (int) ((150 - 101) / (55.4 - 35.5) * (pm25 - 35.5) + 101);
        } else if (pm25 >= 55.5 && pm25 < 150.5) {
            return (int) ((200 - 151) / (150.4 - 55.5) * (pm25 - 55.5) + 151);
        } else if (pm25 >= 150.5 && pm25 < 250.5) {
            return (int) ((300 - 201) / (250.4 - 150.5) * (pm25 - 150.5) + 201);
        } else if (pm25 >= 250.5) {
            return (int) ((500 - 301) / (500.4 - 250.5) * (pm25 - 250.5) + 301);
        }
        return owAqi * 100; // Fallback
    }

    /**
     * 📊 Manual trigger for testing
     */
    public void manualFetchForLocation(Long locationId) {
        Location location = locationRepo.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found with ID: " + locationId));

        log.info("🎯 Manual fetch triggered for location: {}", location.getName());
        fetchAndSaveAQIData(location);
    }

    /**
     * 🔍 Get latest AQI for a location
     */
    public AirQualityData getLatestAQI(Long locationId) {
        Optional<AirQualityData> latest = aqDataRepo.findTopByLocationIdOrderByTimestampUtcDesc(locationId);

        if (latest.isEmpty()) {
            log.warn("⚠️ No AQI data found for location ID: {}", locationId);
            return null;
        }

        return latest.get();
    }
}