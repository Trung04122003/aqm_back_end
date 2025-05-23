package com.commander.aqm.aqm_back_end.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Alert")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Location location;

    private String pollutant; // e.g., "PM2.5"
    private Float value;

    private LocalDateTime triggeredAt;

    @Column(name = "is_read")
    private Boolean isRead;
}
