// src/main/java/com/commander/aqm/aqm_back_end/model/Alert.java
package com.commander.aqm.aqm_back_end.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Alert")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "threshold_id", nullable = false)
    private AlertThreshold threshold;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aq_data_id", nullable = false)
    private AirQualityData aqData;

    // ✅ NEW FIELDS - Match Frontend expectations
    @Column(name = "pollutant", length = 50)
    private String pollutant; // e.g., "PM2.5", "PM10", "NO2"

    @Column(name = "value")
    private Float value; // The actual measured value that triggered alert

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AlertStatus status = AlertStatus.SENT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (triggeredAt == null) {
            triggeredAt = LocalDateTime.now();
        }
    }

    // ✅ Enum for Alert Status
    public enum AlertStatus {
        SENT, ACKNOWLEDGED
    }
}