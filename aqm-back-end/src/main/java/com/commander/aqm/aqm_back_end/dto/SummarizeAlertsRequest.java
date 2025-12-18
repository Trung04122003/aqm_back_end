// src/main/java/com/commander/aqm/aqm_back_end/dto/SummarizeAlertsRequest.java
package com.commander.aqm.aqm_back_end.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

/**
 * DTO for requesting AI summary of alerts
 * Used in: Alerts.tsx - AI summary button
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummarizeAlertsRequest {

    /**
     * List of alerts to summarize
     */
    private List<AlertSummaryItem> alerts;

    /**
     * Time period (e.g., "last 24 hours")
     */
    private String timePeriod;

    /**
     * Inner class for alert item details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AlertSummaryItem {
        private Long id;
        private String pollutant;
        private Float value;
        private String locationName;
        private String triggeredAt;
        private Boolean isRead;
    }
}