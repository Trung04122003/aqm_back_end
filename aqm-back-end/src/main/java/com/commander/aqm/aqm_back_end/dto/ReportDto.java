// src/main/java/com/commander/aqm/aqm_back_end/dto/ReportDto.java
package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.Report;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportDto {
    private Long id;
    private Long locationId;
    private String locationName;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    // ✅ Statistical data
    private Double avgPm25;
    private Double avgPm10;
    private Double avgAqi;
    private Integer maxAqi;
    private Integer minAqi;

    // ✅ Distribution breakdown
    private Integer goodDays;
    private Integer moderateDays;
    private Integer unhealthyDays;
    private Integer totalDataPoints;

    private LocalDateTime generatedAt;

    // ✅ Static factory method
    public static ReportDto from(Report report) {
        if (report == null) return null;

        ReportDto dto = new ReportDto();
        dto.setId(report.getId());
        dto.setLocationId(report.getLocation().getId());
        dto.setLocationName(report.getLocation().getName());
        dto.setFromDate(report.getStartTimestamp());
        dto.setToDate(report.getEndTimestamp());

        // Statistics
        dto.setAvgPm25(report.getAvgPm25());
        dto.setAvgPm10(report.getAvgPm10());
        dto.setAvgAqi(report.getAvgAqi());
        dto.setMaxAqi(report.getMaxAqi());
        dto.setMinAqi(report.getMinAqi());

        // Distribution
        dto.setGoodDays(report.getGoodDays());
        dto.setModerateDays(report.getModerateDays());
        dto.setUnhealthyDays(report.getUnhealthyDays());
        dto.setTotalDataPoints(report.getTotalDataPoints());

        dto.setGeneratedAt(report.getCreatedAt());

        return dto;
    }
}