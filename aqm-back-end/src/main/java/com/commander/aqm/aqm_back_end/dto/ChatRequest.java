// src/main/java/com/commander/aqm/aqm_back_end/dto/ChatRequest.java
package com.commander.aqm.aqm_back_end.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

/**
 * DTO for AI chat request
 * Used in: Chat widget component
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRequest {

    /**
     * User's message/question
     */
    private String message;

    /**
     * Conversation history for context
     */
    private List<ChatMessage> history;

    /**
     * User context (role, location, etc.)
     */
    private String userContext;

    /**
     * Inner class for chat message in history
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatMessage {
        private String role; // "user" or "assistant"
        private String content;
        private String timestamp;
    }
}