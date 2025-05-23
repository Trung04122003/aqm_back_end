package com.commander.aqm.aqm_back_end.service.impl;

import com.commander.aqm.aqm_back_end.model.*;
import com.commander.aqm.aqm_back_end.repository.*;
import com.commander.aqm.aqm_back_end.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertThresholdRepository thresholdRepo;
    private final AlertRepository alertRepo;
    private final LocationRepository locationRepo;

    @Override
    public void evaluateAndTriggerAlerts(User user, Float pm25, Float pm10, Float aqi, Long locationId) {
        AlertThreshold threshold = thresholdRepo.findByUser(user).orElse(null);
        if (threshold == null) return;

        Location loc = locationRepo.findById(locationId).orElse(null);
        if (loc == null) return;

        if (threshold.getPm25Threshold() != null && pm25 > threshold.getPm25Threshold()) {
            alertRepo.save(Alert.builder()
                    .user(user)
                    .location(loc)
                    .pollutant("PM2.5")
                    .value(pm25)
                    .triggeredAt(LocalDateTime.now())
                    .isRead(false)
                    .build());
        }

        if (threshold.getPm10Threshold() != null && pm10 > threshold.getPm10Threshold()) {
            alertRepo.save(Alert.builder()
                    .user(user)
                    .location(loc)
                    .pollutant("PM10")
                    .value(pm10)
                    .triggeredAt(LocalDateTime.now())
                    .isRead(false)
                    .build());
        }

        if (threshold.getAqiThreshold() != null && aqi > threshold.getAqiThreshold()) {
            alertRepo.save(Alert.builder()
                    .user(user)
                    .location(loc)
                    .pollutant("AQI")
                    .value(aqi)
                    .triggeredAt(LocalDateTime.now())
                    .isRead(false)
                    .build());
        }
    }

    @Override
    public List<Alert> getUnreadAlerts(User user) {
        return alertRepo.findByUserAndIsReadFalse(user);
    }

    @Override
    public List<Alert> getAllAlerts(User user) {
        return alertRepo.findByUser(user);
    }
}
