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
    @JoinColumn(name = "user_id")
    private Long id;

    @Enumerated(EnumType.STRING)  // ✅ THÊM POLLUTANT FIELD
    private Pollutant pollutant;

    private Float thresholdValue;

    @Enumerated(EnumType.STRING)  // ✅ THÊM COMPARISON FIELD
    private ComparisonOperator comparison;

    @ManyToOne(optional = false)
    private User user;

    private Float pm25Threshold;
    private Float pm10Threshold;
    private Float aqiThreshold;
}

// ✅ THÊM ENUMS
enum Pollutant {
    PM25, PM10, NO2, CO, O3, SO2, AQI
}

enum ComparisonOperator {
    GREATER_THAN, LESS_THAN, GREATER_EQUAL, LESS_EQUAL
}