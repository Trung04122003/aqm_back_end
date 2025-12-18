// src/main/java/com/commander/aqm/aqm_back_end/dto/ChatResponse.java
package com.commander.aqm.aqm_back_end.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for AI chat response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    /**
     * AI's response message in Vietnamese
     */
    private String message;

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

    /**
     * Suggested follow-up questions (optional)
     */
    private String[] suggestions;
}