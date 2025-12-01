package com.commander.aqm.aqm_back_end.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AlertThreshold")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ✅ Keep simple numeric thresholds (as your UI expects)
    @Column(name = "pm25_threshold")
    private Float pm25Threshold;

    @Column(name = "pm10_threshold")
    private Float pm10Threshold;

    @Column(name = "aqi_threshold")
    private Float aqiThreshold;
}