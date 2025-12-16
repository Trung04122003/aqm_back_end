// src/main/java/com/commander/aqm/aqm_back_end/service/RealTimeWeatherService.java
package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.Location;
import com.commander.aqm.aqm_back_end.model.WeatherData;
import com.commander.aqm.aqm_back_end.repository.LocationRepository;
import com.commander.aqm.aqm_back_end.repository.WeatherDataRepository;
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

/**
 * 🌤️ Real-Time Weather Data Service
 * Fetches current weather from OpenWeatherMap API
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RealTimeWeatherService {

    // ✅ Make public for WeatherDataController to access
    public final LocationRepository locationRepo;
    private final WeatherDataRepository weatherRepo;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openweather.api.key:8d5af07bbcf8969b864f267d5465e1d2}")
    private String openWeatherApiKey;

    /**
     * 🔄 Auto-fetch weather every 30 minutes
     */
    @Scheduled(cron = "0 */30 * * * *")
    public void fetchAllLocationsWeather() {
        log.info("🌍 Starting scheduled weather data fetch...");

        List<Location> locations = locationRepo.findAll();

        for (Location location : locations) {
            try {
                fetchAndSaveWeatherData(location);
                Thread.sleep(2000); // Rate limit
            } catch (Exception e) {
                log.error("❌ Failed to fetch weather for: {}", location.getName(), e);
            }
        }

        log.info("✅ Weather fetch completed for {} locations", locations.size());
    }

    /**
     * 🌤️ Fetch and save weather data for location
     */
    public WeatherData fetchAndSaveWeatherData(Location location) {
        try {
            log.info("🔍 Fetching weather for: {} (lat={}, lon={})",
                    location.getName(), location.getLatitude(), location.getLongitude());

            if (openWeatherApiKey == null || openWeatherApiKey.isEmpty()) {
                throw new RuntimeException("Weather API key not configured");
            }

            String url = String.format(
                    "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
                    location.getLatitude(),
                    location.getLongitude(),
                    openWeatherApiKey
            );

            log.info("📞 Calling Weather API...");
            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isEmpty()) {
                throw new RuntimeException("Empty response from Weather API");
            }

            JsonNode root = objectMapper.readTree(response);

            // Extract weather data
            JsonNode main = root.path("main");
            JsonNode wind = root.path("wind");
            JsonNode rain = root.path("rain");

            WeatherData weatherData = WeatherData.builder()
                    .location(location)
                    .timestampUtc(LocalDateTime.now())
                    .temperatureC((float) main.path("temp").asDouble())
                    .humidityPct((float) main.path("humidity").asDouble())
                    .pressureHpa((float) main.path("pressure").asDouble())
                    .windSpeedMps((float) wind.path("speed").asDouble())
                    .windDirDeg(wind.path("deg").asInt())
                    .precipProbabilityPct(rain.path("1h").asDouble() > 0 ? 100f : 0f) // Simplified
                    .build();

            weatherRepo.save(weatherData);

            log.info("✅ Saved weather for {}: Temp={}°C, Humidity={}%",
                    location.getName(), weatherData.getTemperatureC(), weatherData.getHumidityPct());

            return weatherData;

        } catch (Exception e) {
            log.error("❌ Error fetching weather for {}: {}", location.getName(), e.getMessage());
            throw new RuntimeException("Failed to fetch weather: " + e.getMessage(), e);
        }
    }

    /**
     * 🎯 Manual fetch for specific location
     */
    public void manualFetchForLocation(Long locationId) {
        Location location = locationRepo.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found: " + locationId));

        log.info("🎯 Manual weather fetch for: {}", location.getName());
        fetchAndSaveWeatherData(location);
    }

    /**
     * 📊 Get latest weather for location
     */
    public WeatherData getLatestWeather(Long locationId) {
        return weatherRepo.findTopByLocationIdOrderByTimestampUtcDesc(locationId)
                .orElse(null);
    }
}