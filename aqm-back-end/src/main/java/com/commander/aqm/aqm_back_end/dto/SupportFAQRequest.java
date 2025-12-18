// src/main/java/com/commander/aqm/aqm_back_end/dto/SupportFAQRequest.java
package com.commander.aqm.aqm_back_end.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for requesting AI-generated support ticket response
 * Used in: Admin Support page - AI suggest reply
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportFAQRequest {

    /**
     * Support ticket ID
     */
    private Long ticketId;

    /**
     * User's question/issue
     */
    private String userMessage;

    /**
     * Ticket subject
     */
    private String subject;

    /**
     * User information for context
     */
    private String userName;

    /**
     * Previous conversation history if any
     */
    private String previousReplies;
}