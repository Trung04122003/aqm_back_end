// src/main/java/com/commander/aqm/aqm_back_end/config/DataSeeder.java
package com.commander.aqm.aqm_back_end.config;

import com.commander.aqm.aqm_back_end.model.*;
import com.commander.aqm.aqm_back_end.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 🇻🇳 Vietnam Cities Data Seeder
 * Seeds 6 major cities with realistic air quality data
 */
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final UserRepository userRepo;
    private final LocationRepository locationRepo;
    private final SensorRepository sensorRepo;
    private final AirQualityDataRepository airRepo;
    private final WeatherDataRepository weatherRepo;
    private final ForecastRepository forecastRepo;
    private final AlertThresholdRepository thresholdRepo;
    private final AlertRepository alertRepo;
    private final ReportRepository reportRepo;
    private final SupportRequestRepository supportRepo;
    private final PasswordEncoder encoder;

    private final Random random = new Random();

    @Bean
    public CommandLineRunner seedVietnamData() {
        return args -> {
            System.out.println("🇻🇳 ========== SEEDING VIETNAM CITIES DATA ==========");

            // 👥 STEP 1: Seed Users (không cần list, chỉ call method)
            seedUsers();
            System.out.println("✅ Seeded users");

            // 🌍 STEP 2: Seed Cities
            seedVietnamCities();
            System.out.println("✅ Seeded cities");

            // Lấy admin và city đầu tiên từ repo (an toàn, tránh rỗng)
            User admin = userRepo.findByUsername("admin").orElse(null);
            Location firstCity = locationRepo.findAll().stream().findFirst().orElse(null);

            if (admin == null || firstCity == null) {
                System.out.println("⚠️ Skipping further seeding: Admin or cities not found");
                return; // Thoát sớm nếu không có data cơ bản
            }

            // 🛰️ STEP 3: Seed Sensors
            List<Sensor> sensors = seedSensors(locationRepo.findAll()); // Truyền full list cities
            System.out.println("✅ Created " + sensors.size() + " sensors");

            // 🌫️ STEP 4: Seed Air Quality Data
            seedAirQualityData(locationRepo.findAll(), sensors);
            System.out.println("✅ Generated air quality data");

            // 🌦️ STEP 5: Seed Weather Data
            seedWeatherData(locationRepo.findAll());
            System.out.println("✅ Generated weather data");

            // 🔮 STEP 6: Seed Forecasts
            seedForecasts(locationRepo.findAll());
            System.out.println("✅ Generated forecasts");

            // 🛡️ STEP 7: Seed Alert Thresholds
            seedAlertThresholds(userRepo.findAll()); // Truyền full users từ repo
            System.out.println("✅ Created alert thresholds");

            // 🚨 STEP 8: Seed Alerts (sử dụng admin và firstCity)
            seedAlerts(admin, firstCity);
            System.out.println("✅ Generated sample alerts");

            // 📊 STEP 9: Seed Reports
            seedReports(admin, firstCity);
            System.out.println("✅ Generated sample reports");

            // 📩 STEP 10: Seed Support Requests
            seedSupportRequests(admin);
            System.out.println("✅ Created support requests");

            System.out.println("🎉 ========== VIETNAM DATA SEEDING COMPLETE ==========");
        };
    }

    /**
     * 👥 Seed Users (Admin + Regular Users)
     */
    private List<User> seedUsers() {
        List<User> users = new ArrayList<>();

        // Admin user
        if (userRepo.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@aqm.vn")
                    .fullName("System Administrator")
                    .passwordHash(encoder.encode("Admin123"))
                    .role(Role.ADMIN)
                    .status(Status.ACTIVE)
                    .build();
            users.add(userRepo.save(admin));
        }

        // Regular users for each city
        String[] userNames = {"nguyen_van_a", "tran_thi_b", "le_van_c"};
        String[] fullNames = {"Nguyen Van A", "Tran Thi B", "Le Van C"};
        String[] emails = {"nguyenvana@aqm.vn", "tranthib@aqm.vn", "levanc@aqm.vn"};

        for (int i = 0; i < userNames.length; i++) {
            if (userRepo.findByUsername(userNames[i]).isEmpty()) {
                User user = User.builder()
                        .username(userNames[i])
                        .email(emails[i])
                        .fullName(fullNames[i])
                        .passwordHash(encoder.encode("User123"))
                        .role(Role.USER)
                        .status(Status.ACTIVE)
                        .build();
                users.add(userRepo.save(user));
            }
        }

        return users;
    }

    /**
     * 🇻🇳 Seed 6 Major Vietnam Cities
     */
    private List<Location> seedVietnamCities() {
        List<Location> cities = new ArrayList<>();

        // City data: [name, latitude, longitude]
        Object[][] cityData = {
                {"Ha Noi", 21.0285, 105.8542},
                {"Ho Chi Minh City", 10.8231, 106.6297},
                {"Da Nang", 16.0544, 108.2022},
                {"Hai Phong", 20.8449, 106.6881},
                {"Can Tho", 10.0452, 105.7469},
                {"Hue", 16.4637, 107.5909}
        };

        for (Object[] data : cityData) {
            String name = (String) data[0];
            if (locationRepo.findAll().stream().noneMatch(l -> l.getName().equals(name))) {
                Location city = Location.builder()
                        .name(name)
                        .latitude((Double) data[1])
                        .longitude((Double) data[2])
                        .timezone("Asia/Ho_Chi_Minh")
                        .build();
                cities.add(locationRepo.save(city));
            }
        }

        return cities;
    }

    /**
     * 🛰️ Create Sensors for each city
     */
    private List<Sensor> seedSensors(List<Location> cities) {
        List<Sensor> sensors = new ArrayList<>();

        for (Location city : cities) {
            // Create 2 sensors per city
            for (int i = 1; i <= 2; i++) {
                Sensor sensor = Sensor.builder()
                        .serialNumber("VN-" + city.getName().substring(0, 3).toUpperCase() + "-" + i)
                        .model("AQM-Pro-2024")
                        .sensorType("AirQuality")
                        .installationDate(LocalDate.now().minusMonths(6))
                        .status(SensorStatus.ACTIVE)
                        .location(city)
                        .build();
                sensors.add(sensorRepo.save(sensor));
            }
        }

        return sensors;
    }

    /**
     * 🌫️ Generate realistic Air Quality Data for last 7 days
     */
    private void seedAirQualityData(List<Location> cities, List<Sensor> sensors) {
        LocalDateTime now = LocalDateTime.now();

        for (Location city : cities) {
            Sensor sensor = sensors.stream()
                    .filter(s -> s.getLocation().getId().equals(city.getId()))
                    .findFirst()
                    .orElse(null);

            if (sensor == null) continue;

            // Get base pollution levels for each city
            int baseAQI = getBaseAQIForCity(city.getName());

            // Generate data for last 7 days (hourly)
            for (int day = 7; day >= 0; day--) {
                for (int hour = 0; hour < 24; hour++) {
                    LocalDateTime timestamp = now.minusDays(day).minusHours(hour);

                    // Add realistic variations
                    int hourlyVariation = getHourlyVariation(hour);
                    int dailyNoise = random.nextInt(21) - 10; // -10 to +10
                    int aqi = Math.max(10, baseAQI + hourlyVariation + dailyNoise);

                    AirQualityData data = new AirQualityData();
                    data.setLocation(city);
                    data.setSensor(sensor);
                    data.setAqi(aqi);
                    data.setPm25(calculatePM25FromAQI(aqi));
                    data.setPm10(calculatePM10FromAQI(aqi));
                    data.setCo(0.3f + random.nextFloat() * 0.5f);
                    data.setNo2(0.02f + random.nextFloat() * 0.03f);
                    data.setO3(0.05f + random.nextFloat() * 0.05f);
                    data.setSo2(0.01f + random.nextFloat() * 0.02f);
                    data.setTimestampUtc(timestamp);

                    airRepo.save(data);
                }
            }
        }
    }

    /**
     * 🌦️ Generate Weather Data
     */
    private void seedWeatherData(List<Location> cities) {
        LocalDateTime now = LocalDateTime.now();

        for (Location city : cities) {
            // Generate weather for last 24 hours
            for (int hour = 24; hour >= 0; hour--) {
                LocalDateTime timestamp = now.minusHours(hour);

                // Base temperature varies by city (North cooler than South)
                float baseTemp = getBaseTempForCity(city.getName());
                float hourlyTempVariation = getHourlyTempVariation(hour);

                WeatherData weather = WeatherData.builder()
                        .location(city)
                        .temperatureC(baseTemp + hourlyTempVariation)
                        .humidityPct(65f + random.nextFloat() * 20f)
                        .windDirDeg(random.nextInt(360))
                        .windSpeedMps(1f + random.nextFloat() * 3f)
                        .pressureHpa(1010f + random.nextFloat() * 15f)
                        .precipProbabilityPct(random.nextFloat() * 30f)
                        .timestampUtc(timestamp)
                        .build();

                weatherRepo.save(weather);
            }
        }
    }

    /**
     * 🔮 Generate Forecasts (next 24 hours)
     */
    private void seedForecasts(List<Location> cities) {
        LocalDateTime now = LocalDateTime.now();

        for (Location city : cities) {
            int baseAQI = getBaseAQIForCity(city.getName());

            // Generate forecasts for next 24 hours (every 3 hours)
            for (int hour = 3; hour <= 24; hour += 3) {
                LocalDateTime forecastTime = now.plusHours(hour);

                int predictedAQI = baseAQI + random.nextInt(21) - 10;
                predictedAQI = Math.max(10, predictedAQI);

                Forecast forecast = Forecast.builder()
                        .location(city)
                        .timestampUtc(forecastTime)
                        .predictedAqi(Float.valueOf(predictedAQI))
                        .predictedPm25(calculatePM25FromAQI(predictedAQI))
                        .predictedPm10(calculatePM10FromAQI(predictedAQI))
                        .modelVersion("LSTM-v2.0")
                        .build();

                forecastRepo.save(forecast);
            }
        }
    }

    /**
     * 🛡️ Create Alert Thresholds
     */
    private void seedAlertThresholds(List<User> users) {
        for (User user : users) {
            if (thresholdRepo.findByUser(user).isEmpty()) {
                AlertThreshold threshold = AlertThreshold.builder()
                        .user(user)
                        .pm25Threshold(35f)
                        .pm10Threshold(50f)
                        .aqiThreshold(100f)
                        .build();
                thresholdRepo.save(threshold);
            }
        }
    }

    /**
     * 🚨 Generate Sample Alerts
     */
    private void seedAlerts(User user, Location city) {
        AlertThreshold threshold = thresholdRepo.findByUser(user).orElse(null);
        if (threshold == null) return;

        // Get recent high pollution data
        List<AirQualityData> recentData = airRepo.findByLocationIdAndTimestampUtcAfter(
                city.getId(),
                LocalDateTime.now().minusDays(1)
        );

        // Create alerts for high pollution events
        for (AirQualityData data : recentData) {
            if (data.getAqi() != null && data.getAqi() > 100) {
                Alert alert = Alert.builder()
                        .user(user)
                        .threshold(threshold)
                        .aqData(data)
                        .pollutant("AQI")
                        .value(Float.valueOf(data.getAqi()))
                        .triggeredAt(data.getTimestampUtc())
                        .isRead(random.nextBoolean())
                        .status(Alert.AlertStatus.SENT)
                        .build();
                alertRepo.save(alert);
            }
        }
    }

    /**
     * 📊 Generate Sample Reports
     */
    private void seedReports(User user, Location city) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);

        List<AirQualityData> weekData = airRepo.findByLocationIdAndTimestampUtcBetween(
                city.getId(), weekAgo, now
        );

        if (!weekData.isEmpty()) {
            double avgPm25 = weekData.stream()
                    .filter(d -> d.getPm25() != null)
                    .mapToDouble(AirQualityData::getPm25)
                    .average().orElse(0);

            double avgPm10 = weekData.stream()
                    .filter(d -> d.getPm10() != null)
                    .mapToDouble(AirQualityData::getPm10)
                    .average().orElse(0);

            double avgAqi = weekData.stream()
                    .filter(d -> d.getAqi() != null)
                    .mapToDouble(AirQualityData::getAqi)
                    .average().orElse(0);

            int goodDays = (int) weekData.stream()
                    .filter(d -> d.getAqi() != null && d.getAqi() <= 50)
                    .count();

            int moderateDays = (int) weekData.stream()
                    .filter(d -> d.getAqi() != null && d.getAqi() > 50 && d.getAqi() <= 100)
                    .count();

            int unhealthyDays = (int) weekData.stream()
                    .filter(d -> d.getAqi() != null && d.getAqi() > 100)
                    .count();

            Report report = Report.builder()
                    .user(user)
                    .location(city)
                    .reportType(Report.ReportType.WEEKLY)
                    .startTimestamp(weekAgo)
                    .endTimestamp(now)
                    .avgPm25(avgPm25)
                    .avgPm10(avgPm10)
                    .avgAqi(avgAqi)
                    .goodDays(goodDays)
                    .moderateDays(moderateDays)
                    .unhealthyDays(unhealthyDays)
                    .totalDataPoints(weekData.size())
                    .build();

            reportRepo.save(report);
        }
    }

    /**
     * 📩 Create Support Requests
     */
    private void seedSupportRequests(User user) {
        String[] subjects = {
                "Sensor not updating",
                "High pollution alert not received",
                "Dashboard data incorrect"
        };

        String[] messages = {
                "My sensor shows no data for the last 2 hours. Please check.",
                "I set threshold at PM2.5 > 35 but didn't receive alerts yesterday.",
                "The AQI chart shows incorrect values for last week."
        };

        for (int i = 0; i < subjects.length; i++) {
            SupportRequest ticket = SupportRequest.builder()
                    .user(user)
                    .subject(subjects[i])
                    .message(messages[i])
                    .submittedAt(LocalDateTime.now().minusDays(3 - i))
                    .status(RequestStatus.PENDING)
                    .build();
            supportRepo.save(ticket);
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Base AQI for each city (realistic averages)
     */
    private int getBaseAQIForCity(String cityName) {
        return switch (cityName) {
            case "Ha Noi" -> 95;  // Higher pollution
            case "Ho Chi Minh City" -> 85;
            case "Da Nang" -> 55;  // Better air quality
            case "Hai Phong" -> 75;
            case "Can Tho" -> 65;
            case "Hue" -> 60;
            default -> 70;
        };
    }

    /**
     * Base temperature for each city
     */
    private float getBaseTempForCity(String cityName) {
        return switch (cityName) {
            case "Ha Noi" -> 22f;  // Cooler
            case "Ho Chi Minh City" -> 29f;  // Hot
            case "Da Nang" -> 26f;
            case "Hai Phong" -> 23f;
            case "Can Tho" -> 28f;
            case "Hue" -> 25f;
            default -> 26f;
        };
    }

    /**
     * Hourly variation (higher pollution during rush hours)
     */
    private int getHourlyVariation(int hour) {
        if (hour >= 7 && hour <= 9) return 15;  // Morning rush
        if (hour >= 17 && hour <= 19) return 20; // Evening rush
        if (hour >= 22 || hour <= 5) return -10; // Night (cleaner)
        return 0;
    }

    /**
     * Hourly temperature variation
     */
    private float getHourlyTempVariation(int hour) {
        if (hour >= 12 && hour <= 15) return 3f;  // Afternoon hot
        if (hour >= 0 && hour <= 6) return -3f;   // Night cool
        return 0f;
    }

    /**
     * Calculate PM2.5 from AQI (simplified EPA formula)
     */
    private Float calculatePM25FromAQI(int aqi) {
        if (aqi <= 50) return aqi * 12f / 50f;
        if (aqi <= 100) return 12.1f + (aqi - 51) * 23.9f / 49f;
        if (aqi <= 150) return 35.5f + (aqi - 101) * 19.5f / 49f;
        return 55.5f + (aqi - 151) * 94.5f / 49f;
    }

    /**
     * Calculate PM10 from AQI (simplified)
     */
    private Float calculatePM10FromAQI(int aqi) {
        return calculatePM25FromAQI(aqi) * 1.8f;
    }

    // You can add seasonal multipliers:
    private float getSeasonalMultiplier() {
        int month = LocalDateTime.now().getMonthValue();
        if (month >= 11 || month <= 2) return 1.3f; // Winter - worse
        if (month >= 6 && month <= 8) return 0.8f;  // Rainy - better
        return 1.0f;
    }

    // ==================== DATABASE RESET ====================

    @Component
//    @Profile("reset")
    @RequiredArgsConstructor
    public static class DatabaseResetRunner implements CommandLineRunner {

        private final EntityManager entityManager;

        @Transactional
        @Override
        public void run(String... args) {
            System.out.println("⚠️ Resetting database tables...");

            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE air_quality_data").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE Alert").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE alert_threshold").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE Forecast").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE weather_data").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE Report").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE support_request").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE Sensor").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE Location").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE `User`").executeUpdate();
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

            System.out.println("✅ Database reset complete!");
        }
    }
}