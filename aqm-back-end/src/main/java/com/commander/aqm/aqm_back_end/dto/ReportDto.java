// src/main/java/com/commander/aqm/aqm_back_end/dto/ReportDto.java
package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private Long id;
    private String username;           // ✅ User's username
    private String locationName;       // ✅ Location name
    private String fromDate;           // ✅ Period start
    private String toDate;             // ✅ Period end
    private Double avgAqi;             // ✅ Average AQI
    private Double avgPm25;
    private Double avgPm10;
    private Integer maxAqi;
    private Integer minAqi;
    private Integer goodDays;
    private Integer moderateDays;
    private Integer unhealthyDays;
    private Integer totalDataPoints;
    private LocalDateTime generatedAt; // ✅ Creation timestamp

    // ✅ Static factory method to convert from Report entity
    public static ReportDto from(Report report) {
        if (report == null) return null;

        return ReportDto.builder()
                .id(report.getId())
                .username(report.getUser() != null ? report.getUser().getUsername() : "Unknown")
                .locationName(report.getLocation() != null ? report.getLocation().getName() : "Unknown")
                .fromDate(report.getStartTimestamp() != null ? report.getStartTimestamp().toString() : "")
                .toDate(report.getEndTimestamp() != null ? report.getEndTimestamp().toString() : "")
                .avgAqi(report.getAvgAqi())
                .avgPm25(report.getAvgPm25())
                .avgPm10(report.getAvgPm10())
                .maxAqi(report.getMaxAqi())
                .minAqi(report.getMinAqi())
                .goodDays(report.getGoodDays())
                .moderateDays(report.getModerateDays())
                .unhealthyDays(report.getUnhealthyDays())
                .totalDataPoints(report.getTotalDataPoints())
                .generatedAt(report.getCreatedAt())
                .build();
    }
}