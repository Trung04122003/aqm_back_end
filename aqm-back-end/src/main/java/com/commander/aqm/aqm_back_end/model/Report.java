package com.commander.aqm.aqm_back_end.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Report")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Location location;

    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    private Double avgPm25;
    private Double avgPm10;
    private Double avgAqi;

    private LocalDateTime generatedAt;
}
