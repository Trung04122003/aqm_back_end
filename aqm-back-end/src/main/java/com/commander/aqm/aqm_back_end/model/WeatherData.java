package com.commander.aqm.aqm_back_end.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "WeatherData")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherData {
    @Id @GeneratedValue private Long id;

    private LocalDateTime timestampUtc;
    private Float temperatureC, humidityPct, windSpeedMps, precipProbabilityPct, pressureHpa;
    private Integer windDirDeg;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
}
