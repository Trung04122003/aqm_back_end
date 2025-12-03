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

    /**
     * ✅ EXISTING: Send alert email to user
     */
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
            message.setText(buildAlertEmailBody(user, alert));

            mailSender.send(message);
            log.info("✅ Alert email sent to: {} for alert ID: {}", user.getEmail(), alert.getId());

        } catch (Exception e) {
            log.error("❌ Failed to send alert email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    /**
     * ✅ NEW: Send password reset email
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String username, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@aqm-system.com");
            message.setTo(toEmail);
            message.setSubject("🔑 AQM System - Password Reset Request");
            message.setText(buildPasswordResetEmailBody(username, resetLink));

            mailSender.send(message);
            log.info("✅ Password reset email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * ✅ EXISTING: Build alert email body
     */
    private String buildAlertEmailBody(User user, Alert alert) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String locationName = alert.getAqData() != null && alert.getAqData().getLocation() != null
                ? alert.getAqData().getLocation().getName()
                : "Unknown Location";

        return String.format(
                """
                🎅 AQM Winter Alert System
                
                Hello %s,
                
                We detected elevated air quality levels in your monitored area:
                
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                📍 Location: %s
                🌫️ Pollutant: %s
                📊 Measured Value: %.1f %s
                🕒 Time: %s
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
                ⚠️ Health Advisory:
                %s
                
                🔗 View full details:
                https://aqm-system.com/alerts
                
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
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

    /**
     * ✅ NEW: Build password reset email body
     */
    private String buildPasswordResetEmailBody(String username, String resetLink) {
        return String.format(
                """
                🔑 AQM System - Password Reset Request
                
                Hello %s,
                
                We received a request to reset your password for your AQM System account.
                
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
                🔗 Click the link below to reset your password:
                
                %s
                
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
                ⚠️ Important Security Information:
                
                • This link will expire in 15 minutes
                • If you didn't request this reset, please ignore this email
                • Your password will remain unchanged until you create a new one
                • Never share this link with anyone
                
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                
                Need help? Contact our support team at support@aqm-system.com
                
                ---
                🎄 AQM Winter - Christmas 2025 Edition
                Air Quality Monitoring System
                """,
                username,
                resetLink
        );
    }

    /**
     * ✅ EXISTING: Get unit for pollutant
     */
    private String getUnit(String pollutant) {
        return switch (pollutant.toUpperCase()) {
            case "AQI" -> "";
            case "PM2.5", "PM10", "NO2", "SO2", "O3", "CO" -> "µg/m³";
            default -> "";
        };
    }

    /**
     * ✅ EXISTING: Get health advisory
     */
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