// src/main/java/com/commander/aqm/aqm_back_end/model/AirQualityData.java
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp_utc")
    private LocalDateTime timestampUtc;

    @Column(name = "pm25")
    private Float pm25;

    @Column(name = "pm10")
    private Float pm10;

    @Column(name = "no2")
    private Float no2;

    @Column(name = "co")
    private Float co;

    @Column(name = "o3")
    private Float o3;

    @Column(name = "so2")
    private Float so2;

    @Column(name = "aqi")
    private Integer aqi;

    @ManyToOne
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    /**
     * ✅ FIX: Add explicit getter for NO2 (for DTO mapping)
     */
    public Float getNO2() {
        return no2;
    }

    public void setNO2(Float no2) {
        this.no2 = no2;
    }
}