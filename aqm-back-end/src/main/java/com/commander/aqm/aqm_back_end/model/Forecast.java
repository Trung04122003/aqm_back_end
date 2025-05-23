package com.commander.aqm.aqm_back_end.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Forecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Location location;

    private LocalDateTime timestampUtc;

    private Float predictedPm25;
    private Float predictedPm10;
    private Float predictedAqi;

    private String modelVersion; // E.g., "LSTM-1.3"
}
