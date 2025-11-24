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

    @Bean
    public CommandLineRunner seedEverything() {
        return args -> {
            // 👤 Seed user
            User user = userRepo.findByUsername("admin").orElse(null);
            if (user == null) {
                user = User.builder()
                        .username("admin")
                        .email("admin@aqm.local")
                        .fullName("Admin Tester")
                        .passwordHash(encoder.encode("Admin123"))
                        .role(Role.ADMIN)
                        .status(Status.ACTIVE)
                        .build();
                userRepo.save(user);
            }

            // 🌍 Location
            Location loc = locationRepo.findAll().stream().findFirst().orElse(null);
            if (loc == null) {
                loc = Location.builder()
                        .name("Hanoi")
                        .latitude(21.0285)
                        .longitude(105.8542)
                        .timezone("Asia/Ho_Chi_Minh")
                        .build();
                locationRepo.save(loc);
            }

            // 🛰️ Sensor
            Sensor sensor = Sensor.builder()
                    .serialNumber("SN-001")
                    .model("AQM-Edge")
                    .sensorType("AirQuality")
                    .installationDate(LocalDate.now().minusDays(10))
                    .status(SensorStatus.ACTIVE)
                    .location(loc)
                    .build();
            sensorRepo.save(sensor);

            // 🌫️ Air Quality
            for (int i = 0; i < 10; i++) {
                AirQualityData data = new AirQualityData();
                data.setLocation(loc);
                data.setSensor(sensor);
                data.setAqi(50 + i * 3);
                data.setPm25(15f + i);
                data.setPm10(30f + i);
                data.setCo(0.5f);
                data.setNo2(0.3f);
                data.setO3(0.2f);
                data.setSo2(0.1f);
                data.setTimestampUtc(LocalDateTime.now().minusHours(i));
                airRepo.save(data);
            }

            // 🌦️ Weather
            for (int i = 0; i < 5; i++) {
                WeatherData weather = WeatherData.builder()
                        .location(loc)
                        .temperatureC(28f + i)
                        .humidityPct(60f)
                        .windDirDeg(180)
                        .windSpeedMps(1.5f)
                        .pressureHpa(1010f)
                        .precipProbabilityPct(10f)
                        .timestampUtc(LocalDateTime.now().minusHours(i))
                        .build();
                weatherRepo.save(weather);
            }

            // 🌤️ Forecast
            Forecast forecast = Forecast.builder()
                    .location(loc)
                    .timestampUtc(LocalDateTime.now().plusHours(3))
                    .predictedAqi(95f)
                    .predictedPm25(22.5f)
                    .predictedPm10(42f)
                    .modelVersion("LSTM-1.0")
                    .build();
            forecastRepo.save(forecast);

            // 🛡️ Alert Threshold
            AlertThreshold threshold = thresholdRepo.findByUser(user).orElse(null);
            if (threshold == null) {
                threshold = AlertThreshold.builder()
                        .user(user)
                        .pm25Threshold(20f)
                        .pm10Threshold(35f)
                        .aqiThreshold(85f)
                        .build();
                thresholdRepo.save(threshold);
            }

            // 🚨 Alert
            Alert alert = Alert.builder()
                    .user(user)
                    .location(loc)
                    .pollutant("PM2.5")
                    .value(27.5f)
                    .triggeredAt(LocalDateTime.now().minusHours(1))
                    .isRead(false)
                    .build();
            alertRepo.save(alert);

            // 📊 Report
            Report report = Report.builder()
                    .user(user)
                    .location(loc)
                    .fromDate(LocalDateTime.now().minusDays(1))
                    .toDate(LocalDateTime.now())
                    .avgPm25(21.0)
                    .avgPm10(39.0)
                    .avgAqi(74.0)
                    .generatedAt(LocalDateTime.now())
                    .build();
            reportRepo.save(report);

            // 📩 Support Request
            SupportRequest ticket = SupportRequest.builder()
                    .user(user)
                    .subject("My AQI chart looks wrong")
                    .message("It says AQI 500 at 3AM? That’s not possible.")
                    .submittedAt(LocalDateTime.now().minusMinutes(10))
                    .status(RequestStatus.PENDING)
                    .build();
            supportRepo.save(ticket);

            System.out.println("🧬 All sample data seeded successfully.");
        };
    }
    //Lệnh reset DB
    @Component
    @Profile("reset")
    @RequiredArgsConstructor
    public class DatabaseResetRunner implements CommandLineRunner {

        private final EntityManager entityManager;

        @Transactional
        @Override
        public void run(String... args) {
            System.out.println("⚠️  Resetting database tables...");

            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE air_quality_data").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE alert").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE alert_threshold").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE forecast").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE location").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE support_request").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE user").executeUpdate();
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

            System.out.println("✅  Database reset complete!");
        }
    }

}