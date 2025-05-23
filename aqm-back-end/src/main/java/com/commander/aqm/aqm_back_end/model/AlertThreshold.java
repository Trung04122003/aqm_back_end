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
    private User user;

    private Float pm25Threshold;
    private Float pm10Threshold;
    private Float aqiThreshold;
}
