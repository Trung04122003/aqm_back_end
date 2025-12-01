// src/main/java/com/commander/aqm/aqm_back_end/service/EmailService.java
package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.Alert;
import com.commander.aqm.aqm_back_end.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendAlertEmail(User user, Alert alert) {
        // Check if user has email alerts enabled
        if (user.getEmailAlertsEnabled() == null || !user.getEmailAlertsEnabled()) {
            log.debug("Email alerts disabled for user: {}", user.getUsername());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@aqm-system.com");
            message.setTo(user.getEmail());
            message.setSubject("🚨 AQM Winter Alert: " + alert.getPollutant() + " Exceeded");
            message.setText(buildEmailBody(user, alert));

            mailSender.send(message);
            log.info("✅ Email sent to: {} for alert ID: {}", user.getEmail(), alert.getId());

        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private String buildEmailBody(User user, Alert alert) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String locationName = alert.getAqData() != null && alert.getAqData().getLocation() != null
                ? alert.getAqData().getLocation().getName()
                : "Unknown Location";

        return String.format(
                """
                🎅 AQM Winter Alert System
                
                Hello %s,
                
                We detected elevated air quality levels in your monitored area:
                
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                📍 Location: %s
                🌫️ Pollutant: %s
                📊 Measured Value: %.1f %s
                🕒 Time: %s
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
                ⚠️ Health Advisory:
                %s
                
                🔗 View full details:
                https://aqm-system.com/alerts
                
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
                🎄 Stay safe and have a healthy Christmas!
                
                ---
                AQM Winter - Christmas 2025 Edition
                To manage your alert preferences, visit: https://aqm-system.com/thresholds
                """,
                user.getUsername(),
                locationName,
                alert.getPollutant(),
                alert.getValue(),
                getUnit(alert.getPollutant()),
                alert.getTriggeredAt().format(formatter),
                getHealthAdvisory(alert.getPollutant(), alert.getValue())
        );
    }

    private String getUnit(String pollutant) {
        return switch (pollutant.toUpperCase()) {
            case "AQI" -> "";
            case "PM2.5", "PM10", "NO2", "SO2", "O3", "CO" -> "µg/m³";
            default -> "";
        };
    }

    private String getHealthAdvisory(String pollutant, Float value) {
        if (pollutant.equalsIgnoreCase("AQI")) {
            if (value > 150) return "🔴 UNHEALTHY: Sensitive groups should limit outdoor activities.";
            if (value > 100) return "🟠 MODERATE: Unusually sensitive people should consider reducing prolonged outdoor exertion.";
            return "🟢 GOOD: Air quality is satisfactory.";
        } else if (pollutant.equalsIgnoreCase("PM2.5")) {
            if (value > 55) return "🔴 HIGH: Reduce outdoor activities, especially for children and elderly.";
            if (value > 35) return "🟠 ELEVATED: Sensitive individuals should limit prolonged outdoor exertion.";
            return "🟢 NORMAL: No health concerns.";
        } else if (pollutant.equalsIgnoreCase("PM10")) {
            if (value > 150) return "🔴 HIGH: Avoid prolonged outdoor activities.";
            if (value > 50) return "🟠 ELEVATED: Sensitive groups should reduce outdoor activities.";
            return "🟢 NORMAL: Air quality is acceptable.";
        }
        return "⚠️ Please check the dashboard for more information.";
    }
}