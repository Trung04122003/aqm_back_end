// src/main/java/com/commander/aqm/aqm_back_end/dto/SupportFAQResponse.java
package com.commander.aqm.aqm_back_end.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for AI-generated support response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportFAQResponse {

    /**
     * AI-suggested reply for admin to send
     */
    private String suggestedReply;

    /**
     * Category of the issue
     */
    private String category;

    /**
     * Confidence level (Low, Medium, High)
     */
    private String confidence;

    /**
     * Additional notes for admin
     */
    private String adminNotes;

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