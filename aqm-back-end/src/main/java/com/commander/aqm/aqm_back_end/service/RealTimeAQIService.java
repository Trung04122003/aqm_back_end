// aqm-back-end/src/main/java/.../service/RealTimeAQIService.java
package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.AirQualityData;
import com.commander.aqm.aqm_back_end.model.Location;
import com.commander.aqm.aqm_back_end.repository.AirQualityDataRepository;
import com.commander.aqm.aqm_back_end.repository.LocationRepository;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class RealTimeAQIService {

    private final LocationRepository locationRepo;
    private final AirQualityDataRepository aqDataRepo;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openweather.api.key:1ed284bcb1a6f5b2d3e432485840cda4}")
    private String openWeatherApiKey;

    @Value("${waqi.api.token:3c8fe25410427a3846b0b6897434a71c85d65c5d}")
    private String waqiToken;

//    /**
//     * 🔄 Auto-fetch every 30 minutes
//     * Cron: 0 */30 * * * * = Every 30 minutes
//     */
    @Scheduled(cron = "0 */30 * * * *")
    public void fetchAllLocationsData() {
        log.info("🌍 Starting scheduled AQI data fetch...");

        List<Location> locations = locationRepo.findAll();

        for (Location location : locations) {
            try {
                fetchAndSaveAQIData(location);
                Thread.sleep(2000); // Rate limit: 2 seconds between requests
            } catch (Exception e) {
                log.error("❌ Failed to fetch data for location: " + location.getName(), e);
            }
        }

        log.info("✅ Scheduled AQI fetch completed for {} locations", locations.size());
    }

    /**
     * 📡 Fetch data using OpenWeatherMap API
     */
    public AirQualityData fetchAndSaveAQIData(Location location) {
        try {
            String url = String.format(
                    "https://api.openweathermap.org/data/2.5/air_pollution?lat=%f&lon=%f&appid=%s",
                    location.getLatitude(),
                    location.getLongitude(),
                    openWeatherApiKey
            );

            log.info("🔍 Fetching AQI data for: {} (lat={}, lon={})",
                    location.getName(), location.getLatitude(), location.getLongitude());

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode airData = root.path("list").get(0);

            // Extract pollutants
            JsonNode components = airData.path("components");
            int aqi = airData.path("main").path("aqi").asInt();

            // Convert OpenWeather AQI (1-5) to US AQI (0-500)
            int usAqi = convertToUSAQI(aqi, components.path("pm2_5").asDouble());

            // Create and save data
            AirQualityData aqData = AirQualityData.builder()
                    .location(location)
                    .timestampUtc(LocalDateTime.now())
                    .pm25((float) components.path("pm2_5").asDouble())
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
            log.error("❌ Error fetching OpenWeather data for " + location.getName(), e);
            throw new RuntimeException("Failed to fetch AQI data", e);
        }
    }

    /**
     * 📡 Alternative: Fetch using WAQI API (World Air Quality Index)
     */
    public AirQualityData fetchFromWAQI(Location location) {
        try {
            // WAQI uses city name or coordinates
            String url = String.format(
                    "https://api.waqi.info/feed/geo:%f;%f/?token=%s",
                    location.getLatitude(),
                    location.getLongitude(),
                    waqiToken
            );

            log.info("🔍 Fetching WAQI data for: {}", location.getName());

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (!"ok".equals(root.path("status").asText())) {
                throw new RuntimeException("WAQI API returned error status");
            }

            JsonNode data = root.path("data");
            JsonNode iaqi = data.path("iaqi");

            // Extract pollutants
            Float pm25 = iaqi.has("pm25") ? (float) iaqi.path("pm25").path("v").asDouble() : null;
            Float pm10 = iaqi.has("pm10") ? (float) iaqi.path("pm10").path("v").asDouble() : null;
            Float no2 = iaqi.has("no2") ? (float) iaqi.path("no2").path("v").asDouble() : null;
            Float so2 = iaqi.has("so2") ? (float) iaqi.path("so2").path("v").asDouble() : null;
            Float co = iaqi.has("co") ? (float) iaqi.path("co").path("v").asDouble() : null;
            Float o3 = iaqi.has("o3") ? (float) iaqi.path("o3").path("v").asDouble() : null;

            int aqi = data.path("aqi").asInt();

            // Create and save data
            AirQualityData aqData = AirQualityData.builder()
                    .location(location)
                    .timestampUtc(LocalDateTime.now())
                    .pm25(pm25)
                    .pm10(pm10)
                    .no2(no2)
                    .so2(so2)
                    .co(co)
                    .o3(o3)
                    .aqi(aqi)
                    .build();

            aqDataRepo.save(aqData);
            log.info("✅ Saved WAQI data for {}: PM2.5={}, AQI={}",
                    location.getName(), pm25, aqi);

            return aqData;

        } catch (Exception e) {
            log.error("❌ Error fetching WAQI data for " + location.getName(), e);
            throw new RuntimeException("Failed to fetch WAQI data", e);
        }
    }

    /**
     * 🔄 Convert OpenWeather AQI (1-5) to US AQI (0-500)
     * OpenWeather uses European scale, we need US EPA standard
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
        return owAqi * 100; // Fallback: simple conversion
    }

    /**
     * 📊 Manual trigger for testing
     */
    public void manualFetchForLocation(Long locationId) {
        Location location = locationRepo.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        fetchAndSaveAQIData(location);
    }

    /**
     * 🔍 Get latest AQI for a location
     */
    public AirQualityData getLatestAQI(Long locationId) {
        return aqDataRepo.findTopByLocationIdOrderByTimestampUtcDesc(locationId)
                .orElse(null);
    }
}