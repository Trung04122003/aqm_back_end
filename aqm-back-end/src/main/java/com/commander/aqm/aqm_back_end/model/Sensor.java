package com.commander.aqm.aqm_back_end.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "Sensor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {
    @Id @GeneratedValue private Long id;
    private String serialNumber;
    private String sensorType;
    private String model;
    private LocalDate installationDate;

    @Enumerated(EnumType.STRING)
    private SensorStatus status;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
}