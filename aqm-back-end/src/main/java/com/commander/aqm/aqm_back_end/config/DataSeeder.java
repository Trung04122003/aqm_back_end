// src/main/java/com/commander/aqm/aqm_back_end/config/DataSeeder.java
// IMPROVED VERSION - Smart Seeding Strategy

package com.commander.aqm.aqm_back_end.config;

import com.commander.aqm.aqm_back_end.model.*;
import com.commander.aqm.aqm_back_end.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 🎯 SMART DATA SEEDER - Hybrid Strategy
 *
 * Modes:
 * 1. INITIAL_SETUP: Seed cities + users only (let API fetch real data)
 * 2. FULL_DEMO: Seed everything for offline demo
 * 3. SKIP: Production mode (no seeding)
 */
@Slf4j
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

    // ✅ Configuration from application.yml
    @Value("${aqm.seed.mode:INITIAL_SETUP}")
    private String seedMode;

    @Value("${aqm.seed.force:false}")
    private boolean forceReseed;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            log.info("🌱 ========== DATA SEEDER STARTING ==========");
            log.info("🎯 Seed Mode: {}", seedMode);

            // Check if already seeded (unless force = true)
            if (!forceReseed && isAlreadySeeded()) {
                log.info("✅ Database already has data. Skipping seeding.");
                log.info("   (Set aqm.seed.force=true to force re-seed)");
                return;
            }

            switch (seedMode.toUpperCase()) {
                case "INITIAL_SETUP":
                    runInitialSetup();
                    break;
                case "FULL_DEMO":
                    runFullDemo();
                    break;
                case "SKIP":
                    log.info("⏭️ Seeding skipped (production mode)");
                    break;
                default:
                    log.warn("⚠️ Unknown seed mode: {}. Running INITIAL_SETUP.", seedMode);
                    runInitialSetup();
            }

            log.info("🎉 ========== DATA SEEDING COMPLETE ==========");
        };
    }

    /**
     * 🎯 MODE 1: INITIAL_SETUP
     * Seeds essential data only:
     * - Users (admin + test users)
     * - Locations (cities)
     * - Sensors
     *
     * Data will come from real API calls!
     */
    private void runInitialSetup() {
        log.info("🎯 Running INITIAL_SETUP mode...");
        log.info("   → Will seed: Users, Cities, Sensors");
        log.info("   → Real data from API: Air Quality, Weather, Forecasts");

        seedUsers();
        log.info("✅ Seeded users");

        seedVietnamCities();
        log.info("✅ Seeded {} cities", locationRepo.count());

        List<Sensor> sensors = seedSensors(locationRepo.findAll());
        log.info("✅ Created {} sensors", sensors.size());

        // Alert thresholds for all users
        seedAlertThresholds(userRepo.findAll());
        log.info("✅ Created alert thresholds");

        log.info("💡 TIP: Use 'Fetch New Data' button to get real AQI data!");
    }

    /**
     * 🎯 MODE 2: FULL_DEMO
     * Seeds everything for offline demo/testing
     */
    private void runFullDemo() {
        log.info("🎯 Running FULL_DEMO mode...");
        log.info("   → Will seed EVERYTHING (7 days of synthetic data)");

        // Step 1: Basic setup
        seedUsers();
        log.info("✅ Seeded users");

        seedVietnamCities();
        log.info("✅ Seeded cities");

        User admin = userRepo.findByUsername("admin").orElse(null);
        Location firstCity = locationRepo.findAll().stream().findFirst().orElse(null);

        if (admin == null || firstCity == null) {
            log.error("❌ Failed to create admin or cities!");
            return;
        }

        // Step 2: Sensors
        List<Sensor> sensors = seedSensors(locationRepo.findAll());
        log.info("✅ Created {} sensors", sensors.size());

        // Step 3: Historical data (7 days)
        seedAirQualityData(locationRepo.findAll(), sensors);
        log.info("✅ Generated 7 days of air quality data");

        seedWeatherData(locationRepo.findAll());
        log.info("✅ Generated weather data");

        seedForecasts(locationRepo.findAll());
        log.info("✅ Generated forecasts");

        // Step 4: User-specific data
        seedAlertThresholds(userRepo.findAll());
        log.info("✅ Created alert thresholds");

        seedAlerts(admin, firstCity);
        log.info("✅ Generated sample alerts");

        seedReports(admin, firstCity);
        log.info("✅ Generated sample reports");

        seedSupportRequests(admin);
        log.info("✅ Created support requests");

        log.info("🎊 Full demo data ready!");
    }

    /**
     * Check if database already has seeded data
     */
    private boolean isAlreadySeeded() {
        return userRepo.count() > 0 && locationRepo.count() > 0;
    }

    // ==================== SEEDING METHODS (Keep Original Logic) ====================

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

        // Regular users
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

    private List<Location> seedVietnamCities() {
        List<Location> cities = new ArrayList<>();

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

    private List<Sensor> seedSensors(List<Location> cities) {
        List<Sensor> sensors = new ArrayList<>();

        for (Location city : cities) {
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

    // ✅ KEEP ALL YOUR ORIGINAL METHODS:
    // - seedAirQualityData()
    // - seedWeatherData()
    // - seedForecasts()
    // - seedAlertThresholds()
    // - seedAlerts()
    // - seedReports()
    // - seedSupportRequests()
    // - Helper methods (getBaseAQIForCity, etc.)

    private void seedAirQualityData(List<Location> cities, List<Sensor> sensors) {
        LocalDateTime now = LocalDateTime.now();

        for (Location city : cities) {
            Sensor sensor = sensors.stream()
                    .filter(s -> s.getLocation().getId().equals(city.getId()))
                    .findFirst()
                    .orElse(null);

            if (sensor == null) continue;

            int baseAQI = getBaseAQIForCity(city.getName());

            for (int day = 7; day >= 0; day--) {
                for (int hour = 0; hour < 24; hour++) {
                    LocalDateTime timestamp = now.minusDays(day).minusHours(hour);

                    int hourlyVariation = getHourlyVariation(hour);
                    int dailyNoise = random.nextInt(21) - 10;
                    int aqi = Math.max(10, baseAQI + hourlyVariation + dailyNoise);

                    AirQualityData data = new AirQualityData();
                    data.setLocation(city);
                    data.setSensor(sensor);
                    data.setAqi(aqi);
                    data.setPm25(calculatePM25FromAQI(aqi));
                    data.setPm10(calculatePM10FromAQI(aqi));
                    data.setCo(0.3f + random.nextFloat() * 0.5f);
                    data.setNO2(0.02f + random.nextFloat() * 0.03f);
                    data.setO3(0.05f + random.nextFloat() * 0.05f);
                    data.setSo2(0.01f + random.nextFloat() * 0.02f);
                    data.setTimestampUtc(timestamp);

                    airRepo.save(data);
                }
            }
        }
    }

    private void seedWeatherData(List<Location> cities) {
        LocalDateTime now = LocalDateTime.now();

        for (Location city : cities) {
            for (int hour = 24; hour >= 0; hour--) {
                LocalDateTime timestamp = now.minusHours(hour);

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

    private void seedForecasts(List<Location> cities) {
        LocalDateTime now = LocalDateTime.now();

        for (Location city : cities) {
            int baseAQI = getBaseAQIForCity(city.getName());

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

    private void seedAlerts(User user, Location city) {
        AlertThreshold threshold = thresholdRepo.findByUser(user).orElse(null);
        if (threshold == null) return;

        List<AirQualityData> recentData = airRepo.findByLocationIdAndTimestampUtcAfter(
                city.getId(),
                LocalDateTime.now().minusDays(1)
        );

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

    private int getBaseAQIForCity(String cityName) {
        return switch (cityName) {
            case "Ha Noi" -> 95;
            case "Ho Chi Minh City" -> 85;
            case "Da Nang" -> 55;
            case "Hai Phong" -> 75;
            case "Can Tho" -> 65;
            case "Hue" -> 60;
            default -> 70;
        };
    }

    private float getBaseTempForCity(String cityName) {
        return switch (cityName) {
            case "Ha Noi" -> 22f;
            case "Ho Chi Minh City" -> 29f;
            case "Da Nang" -> 26f;
            case "Hai Phong" -> 23f;
            case "Can Tho" -> 28f;
            case "Hue" -> 25f;
            default -> 26f;
        };
    }

    private int getHourlyVariation(int hour) {
        if (hour >= 7 && hour <= 9) return 15;
        if (hour >= 17 && hour <= 19) return 20;
        if (hour >= 22 || hour <= 5) return -10;
        return 0;
    }

    private float getHourlyTempVariation(int hour) {
        if (hour >= 12 && hour <= 15) return 3f;
        if (hour >= 0 && hour <= 6) return -3f;
        return 0f;
    }

    private Float calculatePM25FromAQI(int aqi) {
        if (aqi <= 50) return aqi * 12f / 50f;
        if (aqi <= 100) return 12.1f + (aqi - 51) * 23.9f / 49f;
        if (aqi <= 150) return 35.5f + (aqi - 101) * 19.5f / 49f;
        return 55.5f + (aqi - 151) * 94.5f / 49f;
    }

    private Float calculatePM10FromAQI(int aqi) {
        return calculatePM25FromAQI(aqi) * 1.8f;
    }
}