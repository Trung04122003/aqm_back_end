// src/main/java/com/commander/aqm/aqm_back_end/service/impl/AlertServiceImpl.java
package com.commander.aqm.aqm_back_end.service.impl;

import com.commander.aqm.aqm_back_end.model.*;
import com.commander.aqm.aqm_back_end.repository.*;
import com.commander.aqm.aqm_back_end.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertThresholdRepository thresholdRepo;
    private final AlertRepository alertRepo;
    private final AirQualityDataRepository airQualityRepo;

    @Override
    @Transactional
    public void evaluateAndTriggerAlerts(User user, Float pm25, Float pm10, Float aqi, Long locationId) {
        // Get user's thresholds
        AlertThreshold threshold = thresholdRepo.findByUser(user).orElse(null);
        if (threshold == null) return;

        // ✅ Get the latest AirQualityData for this location (to link in Alert)
        List<AirQualityData> recentData = airQualityRepo
                .findByLocationIdAndTimestampUtcAfter(locationId, LocalDateTime.now().minusMinutes(10));

        if (recentData.isEmpty()) return;
        AirQualityData latestData = recentData.get(recentData.size() - 1);

        // ✅ Check PM2.5 threshold
        if (threshold.getPm25Threshold() != null && pm25 != null && pm25 > threshold.getPm25Threshold()) {
            createAlert(user, threshold, latestData, "PM2.5", pm25);
        }

        // ✅ Check PM10 threshold
        if (threshold.getPm10Threshold() != null && pm10 != null && pm10 > threshold.getPm10Threshold()) {
            createAlert(user, threshold, latestData, "PM10", pm10);
        }

        // ✅ Check AQI threshold
        if (threshold.getAqiThreshold() != null && aqi != null && aqi > threshold.getAqiThreshold()) {
            createAlert(user, threshold, latestData, "AQI", aqi);
        }
    }

    // ✅ Helper method to create alert
    private void createAlert(User user, AlertThreshold threshold, AirQualityData aqData, String pollutant, Float value) {
        Alert alert = Alert.builder()
                .user(user)
                .threshold(threshold)
                .aqData(aqData)
                .pollutant(pollutant)
                .value(value)
                .triggeredAt(LocalDateTime.now())
                .isRead(false)
                .status(Alert.AlertStatus.SENT)
                .build();

        alertRepo.save(alert);
    }

    @Override
    public List<Alert> getUnreadAlerts(User user) {
        return alertRepo.findByUserAndIsReadFalse(user);
    }

    @Override
    public List<Alert> getAllAlerts(User user) {
        return alertRepo.findByUser(user);
    }

    @Override
    @Transactional
    public Alert markAsRead(Long alertId, User user) {
        Alert alert = alertRepo.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        // ✅ Verify alert belongs to user
        if (!alert.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: Alert does not belong to this user");
        }

        alert.setIsRead(true);
        alert.setStatus(Alert.AlertStatus.ACKNOWLEDGED);
        return alertRepo.save(alert);
    }
}