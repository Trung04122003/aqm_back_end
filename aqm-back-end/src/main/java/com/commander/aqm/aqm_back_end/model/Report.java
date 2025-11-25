// src/main/java/com/commander/aqm/aqm_back_end/model/Report.java
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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Column(name = "start_timestamp")
    private LocalDateTime startTimestamp;

    @Column(name = "end_timestamp")
    private LocalDateTime endTimestamp;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // ✅ NEW ANALYSIS FIELDS
    @Column(name = "avg_pm25")
    private Double avgPm25;

    @Column(name = "avg_pm10")
    private Double avgPm10;

    @Column(name = "avg_aqi")
    private Double avgAqi;

    @Column(name = "max_aqi")
    private Integer maxAqi;

    @Column(name = "min_aqi")
    private Integer minAqi;

    @Column(name = "good_days")
    private Integer goodDays = 0;

    @Column(name = "moderate_days")
    private Integer moderateDays = 0;

    @Column(name = "unhealthy_days")
    private Integer unhealthyDays = 0;

    @Column(name = "total_data_points")
    private Integer totalDataPoints = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ✅ Enum for Report Type
    public enum ReportType {
        DAILY, WEEKLY, MONTHLY, CUSTOM
    }
}