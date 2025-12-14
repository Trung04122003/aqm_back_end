// src/main/java/com/commander/aqm/aqm_back_end/service/AlertMonitoringService.java
package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.*;
import com.commander.aqm.aqm_back_end.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 🔔 Real-Time Alert Monitoring Service
 * Automatically checks new AQI data against user thresholds
 * and creates alerts when exceeded
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlertMonitoringService {

    private final AlertRepository alertRepo;
    private final AlertThresholdRepository thresholdRepo;
    private final UserRepository userRepo;
    private final AirQualityDataRepository aqDataRepo;
    private final EmailService emailService;

    /**
     * 🔍 Check if new AQI data triggers any alerts
     * Called after each AQI data fetch
     */
    @Async
    @Transactional
    public void checkAndCreateAlerts(AirQualityData newData) {
        try {
            log.info("🔍 Checking alerts for new AQI data: Location={}, AQI={}, PM2.5={}",
                    newData.getLocation().getName(), newData.getAqi(), newData.getPm25());

            // Get all active users with thresholds
            List<User> activeUsers = userRepo.findByStatus(Status.ACTIVE);

            for (User user : activeUsers) {
                // Get user's threshold settings
                AlertThreshold threshold = thresholdRepo.findByUser(user).orElse(null);

                if (threshold == null) {
                    log.debug("⚠️ User {} has no threshold settings, using defaults", user.getUsername());
                    // Use default thresholds
                    threshold = AlertThreshold.builder()
                            .user(user)
                            .pm25Threshold(35.5f)   // US EPA standard
                            .pm10Threshold(150f)    // US EPA standard
                            .aqiThreshold(100f)     // Moderate level
                            .build();
                }

                // Check each pollutant
                checkPM25Alert(user, threshold, newData);
                checkPM10Alert(user, threshold, newData);
                checkAQIAlert(user, threshold, newData);
                checkOtherPollutants(user, newData);
            }

            log.info("✅ Alert check completed for {} users", activeUsers.size());

        } catch (Exception e) {
            log.error("❌ Error checking alerts: {}", e.getMessage(), e);
        }
    }

    /**
     * 🔴 Check PM2.5 threshold
     */
    private void checkPM25Alert(User user, AlertThreshold threshold, AirQualityData data) {
        if (data.getPm25() == null) return;

        Float pm25Value = data.getPm25();
        Float pm25Threshold = threshold.getPm25Threshold() != null ? threshold.getPm25Threshold() : 35.5f;

        if (pm25Value > pm25Threshold) {
            // Check if alert already exists recently (avoid spam)
            if (!hasRecentAlert(user, "PM2.5", data.getLocation().getId())) {
                createAlert(user, threshold, data, "PM2.5", pm25Value);
                log.info("🚨 PM2.5 Alert created for user: {} | Value: {} > Threshold: {}",
                        user.getUsername(), pm25Value, pm25Threshold);
            }
        }
    }

    /**
     * 🟡 Check PM10 threshold
     */
    private void checkPM10Alert(User user, AlertThreshold threshold, AirQualityData data) {
        if (data.getPm10() == null) return;

        Float pm10Value = data.getPm10();
        Float pm10Threshold = threshold.getPm10Threshold() != null ? threshold.getPm10Threshold() : 150f;

        if (pm10Value > pm10Threshold) {
            if (!hasRecentAlert(user, "PM10", data.getLocation().getId())) {
                createAlert(user, threshold, data, "PM10", pm10Value);
                log.info("🚨 PM10 Alert created for user: {} | Value: {} > Threshold: {}",
                        user.getUsername(), pm10Value, pm10Threshold);
            }
        }
    }

    /**
     * 🔵 Check AQI threshold
     */
    private void checkAQIAlert(User user, AlertThreshold threshold, AirQualityData data) {
        if (data.getAqi() == null) return;

        Integer aqiValue = data.getAqi();
        Float aqiThreshold = threshold.getAqiThreshold() != null ? threshold.getAqiThreshold() : 100f;

        if (aqiValue > aqiThreshold) {
            if (!hasRecentAlert(user, "AQI", data.getLocation().getId())) {
                createAlert(user, threshold, data, "AQI", aqiValue.floatValue());
                log.info("🚨 AQI Alert created for user: {} | Value: {} > Threshold: {}",
                        user.getUsername(), aqiValue, aqiThreshold);
            }
        }
    }

    /**
     * 🟣 Check other pollutants (NO2, SO2, CO, O3)
     */
    private void checkOtherPollutants(User user, AirQualityData data) {
        // NO2 - Unhealthy above 0.1 mg/m³
        if (data.getNO2() != null && data.getNO2() > 0.1f) {
            if (!hasRecentAlert(user, "NO2", data.getLocation().getId())) {
                createAlert(user, null, data, "NO2", data.getNO2());
            }
        }

        // SO2 - Unhealthy above 0.5 mg/m³
        if (data.getSo2() != null && data.getSo2() > 0.5f) {
            if (!hasRecentAlert(user, "SO2", data.getLocation().getId())) {
                createAlert(user, null, data, "SO2", data.getSo2());
            }
        }

        // CO - Unhealthy above 10 mg/m³
        if (data.getCo() != null && data.getCo() > 10f) {
            if (!hasRecentAlert(user, "CO", data.getLocation().getId())) {
                createAlert(user, null, data, "CO", data.getCo());
            }
        }

        // O3 - Unhealthy above 0.12 mg/m³
        if (data.getO3() != null && data.getO3() > 0.12f) {
            if (!hasRecentAlert(user, "O3", data.getLocation().getId())) {
                createAlert(user, null, data, "O3", data.getO3());
            }
        }
    }

    /**
     * 🔔 Create and save alert
     */
    private void createAlert(User user, AlertThreshold threshold, AirQualityData data, String pollutant, Float value) {
        try {
            Alert alert = Alert.builder()
                    .user(user)
                    .threshold(threshold)
                    .aqData(data)
                    .pollutant(pollutant)
                    .value(value)
                    .isRead(false)
                    .triggeredAt(LocalDateTime.now())
                    .status(Alert.AlertStatus.SENT)
                    .build();

            alertRepo.save(alert);

            // Send email notification if enabled
            if (user.getEmailAlertsEnabled() != null && user.getEmailAlertsEnabled()) {
                try {
                    emailService.sendAlertEmail(user, alert);
                    log.info("📧 Email sent to: {}", user.getEmail());
                } catch (Exception e) {
                    log.error("❌ Failed to send email: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("❌ Failed to create alert: {}", e.getMessage(), e);
        }
    }

    /**
     * ⏰ Check if similar alert exists in last 30 minutes (avoid spam)
     */
    private boolean hasRecentAlert(User user, String pollutant, Long locationId) {
        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

        List<Alert> recentAlerts = alertRepo.findByUserAndPollutantAndTriggeredAtAfter(
                user, pollutant, thirtyMinutesAgo
        );

        // Check if any of these alerts are from the same location
        return recentAlerts.stream()
                .anyMatch(alert -> alert.getAqData().getLocation().getId().equals(locationId));
    }

    /**
     * 🎯 Manual trigger to check all locations for a user
     */
    public void checkAllLocationsForUser(User user) {
        log.info("🔍 Manual alert check for user: {}", user.getUsername());

        // Get latest data from all locations
        List<AirQualityData> latestData = aqDataRepo.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        data -> data.getLocation().getId(),
                        java.util.stream.Collectors.maxBy(
                                java.util.Comparator.comparing(AirQualityData::getTimestampUtc)
                        )
                ))
                .values().stream()
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();

        // Check each location
        for (AirQualityData data : latestData) {
            checkAndCreateAlerts(data);
        }
    }
}