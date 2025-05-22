package com.commander.aqm.aqm_back_end.config;

import com.commander.aqm.aqm_back_end.model.*;
import com.commander.aqm.aqm_back_end.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
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
}
