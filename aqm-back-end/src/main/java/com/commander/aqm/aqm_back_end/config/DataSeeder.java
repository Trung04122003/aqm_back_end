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

    @Bean
    CommandLineRunner runner() {
        return args -> {
            if (locationRepo.count() == 0) {
                Location hanoi = new Location();
                hanoi.setName("Hanoi City");
                hanoi.setLatitude(21.0285);
                hanoi.setLongitude(105.8544);
                hanoi.setTimezone("Asia/Ho_Chi_Minh");
                locationRepo.save(hanoi);

                Sensor sensor = new Sensor();
                sensor.setSerialNumber("SENSOR-0001");
                sensor.setSensorType("PM2.5");
                sensor.setModel("Model X");
                sensor.setStatus(SensorStatus.ACTIVE);
                sensor.setInstallationDate(LocalDate.now());
                sensor.setLocation(hanoi);
                sensorRepo.save(sensor);
            }
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
