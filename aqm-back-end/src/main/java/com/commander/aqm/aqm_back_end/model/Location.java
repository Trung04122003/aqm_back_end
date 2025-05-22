package com.commander.aqm.aqm_back_end.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "Location")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    @Id @GeneratedValue private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private String timezone;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<Sensor> sensors;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<AirQualityData> airQualityData;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<WeatherData> weatherData;
}
