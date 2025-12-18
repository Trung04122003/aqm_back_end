// src/main/java/com/commander/aqm/aqm_back_end/dto/ExplainAQIResponse.java
package com.commander.aqm.aqm_back_end.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for AI explanation response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExplainAQIResponse {

    /**
     * AI-generated explanation in Vietnamese
     */
    private String explanation;

    /**
     * Health recommendations
     */
    private String recommendations;

    /**
     * Air quality status (Good, Moderate, Unhealthy, etc.)
     */
    private String status;

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