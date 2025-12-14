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

    // ==================== SEEDING METHODS ====================

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

    // ✅ FIXED: 63 VIETNAM PROVINCES
    private List<Location> seedVietnamCities() {
        List<Location> cities = new ArrayList<>();

        Object[][] cityData = {
                // Northern Vietnam (24 provinces)
                {"Ha Noi", 21.0285, 105.8542},
                {"Hai Phong", 20.8449, 106.6881},
                {"Quang Ninh", 21.0064, 107.2925},
                {"Bac Ninh", 21.1861, 106.0763},
                {"Hai Duong", 20.9373, 106.3145},
                {"Hung Yen", 20.6464, 106.0511},
                {"Vinh Phuc", 21.3609, 105.5474},
                {"Thai Nguyen", 21.5671, 105.8252},
                {"Bac Giang", 21.2819, 106.1975},
                {"Lang Son", 21.8536, 106.7610},
                {"Cao Bang", 22.6356, 106.2522},
                {"Ha Giang", 22.8025, 104.9784},
                {"Tuyen Quang", 21.8267, 105.2280},
                {"Phu Tho", 21.2683, 105.2045},
                {"Lao Cai", 22.4809, 103.9755},
                {"Yen Bai", 21.7168, 104.8986},
                {"Lai Chau", 22.3864, 103.4702},
                {"Dien Bien", 21.8042, 103.1076},
                {"Son La", 21.1022, 103.7289},
                {"Hoa Binh", 20.6861, 105.3131},
                {"Ninh Binh", 20.2506, 105.9745},
                {"Nam Dinh", 20.4388, 106.1621},
                {"Thai Binh", 20.4464, 106.3365},
                {"Ha Nam", 20.5835, 105.9230},

                // Central Vietnam (19 provinces)
                {"Thanh Hoa", 19.8067, 105.7851},
                {"Nghe An", 19.2342, 104.9200},
                {"Ha Tinh", 18.3559, 105.8877},
                {"Quang Binh", 17.4739, 106.6229},
                {"Quang Tri", 16.7943, 107.1856},
                {"Thua Thien Hue", 16.4637, 107.5909},
                {"Da Nang", 16.0544, 108.2022},
                {"Quang Nam", 15.5394, 108.0191},
                {"Quang Ngai", 15.1214, 108.8044},
                {"Binh Dinh", 13.7830, 109.2196},
                {"Phu Yen", 13.0882, 109.0929},
                {"Khanh Hoa", 12.2585, 109.0526},
                {"Ninh Thuan", 11.6739, 108.8629},
                {"Binh Thuan", 10.9265, 108.0720},
                {"Kon Tum", 14.3497, 108.0004},
                {"Gia Lai", 13.9830, 108.0009},
                {"Dak Lak", 12.7100, 108.2378},
                {"Dak Nong", 12.2646, 107.6098},
                {"Lam Dong", 11.5753, 108.1429},

                // Southern Vietnam (20 provinces)
                {"Ho Chi Minh City", 10.8231, 106.6297},
                {"Ba Ria - Vung Tau", 10.5417, 107.2429},
                {"Binh Duong", 11.3254, 106.4770},
                {"Binh Phuoc", 11.7511, 106.7234},
                {"Dong Nai", 10.9599, 107.1676},
                {"Tay Ninh", 11.3351, 106.0979},
                {"Long An", 10.5355, 106.4056},
                {"Tien Giang", 10.4493, 106.3420},
                {"Ben Tre", 10.2433, 106.3757},
                {"Vinh Long", 10.2397, 105.9571},
                {"Tra Vinh", 9.8124, 106.2992},
                {"Dong Thap", 10.4938, 105.6881},
                {"An Giang", 10.5216, 105.1258},
                {"Kien Giang", 10.0125, 105.0810},
                {"Can Tho", 10.0452, 105.7469},
                {"Hau Giang", 9.7571, 105.6412},
                {"Soc Trang", 9.6028, 105.9739},
                {"Bac Lieu", 9.2515, 105.7346},
                {"Ca Mau", 9.1526, 105.1960},
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
            // Only 1 sensor per province for simplicity
            Sensor sensor = Sensor.builder()
                    .serialNumber("VN-" + city.getName().replace(" ", "").substring(0, Math.min(5, city.getName().length())).toUpperCase())
                    .model("AQM-Pro-2024")
                    .sensorType("AirQuality")
                    .installationDate(LocalDate.now().minusMonths(6))
                    .status(SensorStatus.ACTIVE)
                    .location(city)
                    .build();
            sensors.add(sensorRepo.save(sensor));
        }

        return sensors;
    }

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

    // ✅ UPDATED: AQI for all 63 provinces
    private int getBaseAQIForCity(String cityName) {
        // Northern - Generally higher pollution
        if (cityName.equals("Ha Noi")) return 95;
        if (cityName.equals("Hai Phong")) return 75;
        if (cityName.equals("Bac Ninh") || cityName.equals("Hai Duong") || cityName.equals("Hung Yen")) return 80;
        if (cityName.equals("Thai Nguyen") || cityName.equals("Bac Giang")) return 70;
        if (cityName.equals("Quang Ninh")) return 65;

        // Highland/Mountain - Better air
        if (cityName.equals("Ha Giang") || cityName.equals("Lao Cai") || cityName.equals("Cao Bang") || cityName.equals("Lai Chau")) return 40;
        if (cityName.equals("Dien Bien") || cityName.equals("Son La") || cityName.equals("Hoa Binh")) return 45;

        // Central Coast - Moderate
        if (cityName.equals("Da Nang")) return 55;
        if (cityName.equals("Thua Thien Hue")) return 60;
        if (cityName.equals("Quang Nam") || cityName.equals("Quang Ngai")) return 50;
        if (cityName.equals("Khanh Hoa") || cityName.equals("Ninh Thuan") || cityName.equals("Binh Thuan")) return 55;

        // Central Highland
        if (cityName.equals("Kon Tum") || cityName.equals("Gia Lai") || cityName.equals("Dak Lak") ||
                cityName.equals("Dak Nong") || cityName.equals("Lam Dong")) return 42;

        // Southern - Urban areas higher
        if (cityName.equals("Ho Chi Minh City")) return 85;
        if (cityName.equals("Binh Duong") || cityName.equals("Dong Nai")) return 80;
        if (cityName.equals("Ba Ria - Vung Tau")) return 65;

        // Mekong Delta - Generally good
        if (cityName.equals("Can Tho")) return 60;
        if (cityName.equals("An Giang") || cityName.equals("Dong Thap") || cityName.equals("Vinh Long")) return 55;
        if (cityName.equals("Ben Tre") || cityName.equals("Tra Vinh") || cityName.equals("Soc Trang") ||
                cityName.equals("Bac Lieu") || cityName.equals("Ca Mau")) return 50;

        return 65; // Default
    }

    // ✅ UPDATED: Temperature for all 63 provinces
    private float getBaseTempForCity(String cityName) {
        // Northern provinces - cooler
        if (cityName.equals("Ha Noi") || cityName.equals("Hai Phong")) return 22f;
        if (cityName.contains("Cao Bang") || cityName.contains("Ha Giang") ||
                cityName.contains("Lao Cai") || cityName.contains("Lai Chau")) return 18f;

        // Central provinces
        if (cityName.equals("Da Nang") || cityName.contains("Hue")) return 26f;

        // Highland - cooler
        if (cityName.contains("Lam Dong") || cityName.contains("Dak Lak") ||
                cityName.contains("Kon Tum")) return 20f;

        // Southern - hot
        if (cityName.equals("Ho Chi Minh City")) return 29f;
        if (cityName.equals("Can Tho") || cityName.contains("Mau")) return 28f;

        return 26f; // Default
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