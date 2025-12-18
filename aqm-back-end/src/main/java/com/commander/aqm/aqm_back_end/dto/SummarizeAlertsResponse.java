// src/main/java/com/commander/aqm/aqm_back_end/dto/SummarizeAlertsResponse.java
package com.commander.aqm.aqm_back_end.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for AI alerts summary response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummarizeAlertsResponse {

    /**
     * Overall summary in Vietnamese
     */
    private String summary;

    /**
     * Trend analysis (increasing/stable/decreasing)
     */
    private String trend;

    /**
     * Key findings from the alerts
     */
    private String keyFindings;

    /**
     * Recommended actions for users
     */
    private String recommendations;

    /**
     * Response timestamp
     */
    private String timestamp;

    /**
     * Whether AI call was successful
     */
    private Boolean success;

    /**
     * Error message if any
     */
    private String error;
}