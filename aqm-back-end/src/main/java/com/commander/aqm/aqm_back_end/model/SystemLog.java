package com.commander.aqm.aqm_back_end.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_logs")
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private LogLevel level;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String user;

    public void setMessage(String message) {
    }

    public void setLevel(LogLevel level) {
    }

    public void setUser(String user) {
    }

    public enum LogLevel {
        INFO,
        WARNING,
        ERROR
    }

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }

    // getters + setters
}
