package com.commander.aqm.aqm_back_end.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AirQualityData")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirQualityData {
    @Id @GeneratedValue private Long id;

    private LocalDateTime timestampUtc;
    private Float pm25, pm10, no2, co, o3, so2;
    private Integer aqi;

    @ManyToOne
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
}
