package com.commander.aqm.aqm_back_end.config;

import com.commander.aqm.aqm_back_end.model.*;
import com.commander.aqm.aqm_back_end.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final LocationRepository locationRepo;
    private final SensorRepository sensorRepo;
    private final AirQualityDataRepository airRepo;
    private final WeatherDataRepository weatherRepo;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            if(locationRepo.count() == 0) {
// 📍 Seed Location
                Location loc = new Location();
                loc.setName("Hanoi");
                loc.setLatitude(21.0285);
                loc.setLongitude(105.8542);
                loc.setTimezone("Asia/Ho_Chi_Minh");
                locationRepo.save(loc);

                // 🛰️ Seed Sensor
                Sensor sensor = new Sensor();
                sensor.setSerialNumber("SENSOR-001");
                sensor.setModel("AQM-Pro");
                sensor.setSensorType("AirQuality");
                sensor.setStatus(SensorStatus.ACTIVE);
                sensor.setLocation(loc);
                sensor.setInstallationDate(LocalDate.now().minusDays(30));
                sensorRepo.save(sensor);

                // 🌫️ Seed Air Quality Data
                for (int i = 0; i < 10; i++) {
                    AirQualityData d = new AirQualityData();
                    d.setLocation(loc);
                    d.setSensor(sensor);
                    d.setAqi(50 + i * 5);
                    d.setPm25(15f + i);
                    d.setPm10(20f + i * 2);
                    d.setCo(0.5f);
                    d.setNo2(0.3f);
                    d.setO3(0.1f);
                    d.setSo2(0.05f);
                    d.setTimestampUtc(LocalDateTime.now().minusHours(i));
                    airRepo.save(d);
                }

                // 🌦️ Seed Weather Data
                for (int i = 0; i < 5; i++) {
                    WeatherData w = new WeatherData();
                    w.setLocation(loc);
                    w.setTemperatureC(28.0f + i);
                    w.setHumidityPct(60f + i);
                    w.setPressureHpa(1010f + i);
                    w.setWindSpeedMps(1.5f + i);
                    w.setWindDirDeg(180);
                    w.setTimestampUtc(LocalDateTime.now().minusHours(i));
                    weatherRepo.save(w);
                }
            }

            System.out.println("🌱 Sample data seeded.");
        };
    }

    @Bean
    CommandLineRunner demoSensorData(LocationRepository locRepo, SensorRepository sensorRepo, AirQualityDataRepository dataRepo) {
        return args -> {
            Location loc = locRepo.findAll().stream().findFirst().orElse(null);
            Sensor sensor = sensorRepo.findAll().stream().findFirst().orElse(null);

            if (loc != null && sensor != null) {
                for (int i = 0; i < 24; i++) {
                    AirQualityData data = new AirQualityData();
                    data.setLocation(loc);
                    data.setSensor(sensor);
                    data.setTimestampUtc(LocalDateTime.now().minusHours(i));
                    data.setPm25(20f + i);
                    data.setPm10(30f + i);
                    data.setAqi(50 + i);
                    dataRepo.save(data);
                }
            }
        };
    }
}


