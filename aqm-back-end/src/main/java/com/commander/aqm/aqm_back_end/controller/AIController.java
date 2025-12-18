// src/main/java/com/commander/aqm/aqm_back_end/controller/AIController.java
package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.*;
import com.commander.aqm.aqm_back_end.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AI Controller - Provides AI-powered insights and assistance
 *
 * Architecture Principle:
 * - AI is a Decision Support Layer
 * - AI does NOT replace logic or make automatic decisions
 * - AI interprets data, suggests actions, and supports user understanding
 *
 * All endpoints are authenticated (USER or ADMIN role required)
 */
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI APIs", description = "AI-powered insights and assistance")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final AIService aiService;

    /**
     * Explain current AQI with health recommendations
     *
     * Usage: Dashboard.tsx - "AI Explain" button
     *
     * POST /api/ai/explain-current-aqi
     * Body: ExplainAQIRequest
     * Response: ExplainAQIResponse
     */
    @PostMapping("/explain-current-aqi")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "AI explains current AQI",
            description = "Get AI-generated explanation of air quality with health recommendations"
    )
    public ResponseEntity<ExplainAQIResponse> explainCurrentAQI(
            @RequestBody ExplainAQIRequest request
    ) {
        log.info("🤖 AI Request: Explain AQI for location: {}, AQI: {}",
                request.getLocationName(), request.getAqi());

        try {
            ExplainAQIResponse response = aiService.explainCurrentAQI(request);

            if (response.getSuccess()) {
                log.info("✅ AI Response: Successfully generated explanation");
                return ResponseEntity.ok(response);
            } else {
                log.warn("⚠️ AI Response: Failed - {}", response.getError());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("❌ AI Controller Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    ExplainAQIResponse.builder()
                            .success(false)
                            .error("Lỗi hệ thống: " + e.getMessage())
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build()
            );
        }
    }

    /**
     * Summarize multiple alerts with trend analysis
     *
     * Usage: Alerts.tsx - "AI Summary" button
     *
     * POST /api/ai/summarize-alerts
     * Body: SummarizeAlertsRequest
     * Response: SummarizeAlertsResponse
     */
    @PostMapping("/summarize-alerts")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "AI summarizes alerts",
            description = "Get AI-generated summary of multiple alerts with trend analysis"
    )
    public ResponseEntity<SummarizeAlertsResponse> summarizeAlerts(
            @RequestBody SummarizeAlertsRequest request
    ) {
        log.info("🤖 AI Request: Summarize {} alerts for period: {}",
                request.getAlerts().size(), request.getTimePeriod());

        try {
            SummarizeAlertsResponse response = aiService.summarizeAlerts(request);

            if (response.getSuccess()) {
                log.info("✅ AI Response: Successfully generated alert summary");
                return ResponseEntity.ok(response);
            } else {
                log.warn("⚠️ AI Response: Failed - {}", response.getError());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("❌ AI Controller Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    SummarizeAlertsResponse.builder()
                            .success(false)
                            .error("Lỗi hệ thống: " + e.getMessage())
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build()
            );
        }
    }

    /**
     * AI Chat Assistant
     *
     * Usage: Chat widget component
     *
     * POST /api/ai/chat
     * Body: ChatRequest
     * Response: ChatResponse
     */
    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
            summary = "AI chat assistant",
            description = "Get AI responses to user questions about air quality"
    )
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request
    ) {
        log.info("🤖 AI Request: Chat message: '{}'",
                request.getMessage().substring(0, Math.min(50, request.getMessage().length())));

        try {
            ChatResponse response = aiService.chat(request);

            if (response.getSuccess()) {
                log.info("✅ AI Response: Successfully generated chat response");
                return ResponseEntity.ok(response);
            } else {
                log.warn("⚠️ AI Response: Failed - {}", response.getError());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("❌ AI Controller Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    ChatResponse.builder()
                            .success(false)
                            .error("Lỗi hệ thống: " + e.getMessage())
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build()
            );
        }
    }

    /**
     * Generate suggested reply for support ticket (Admin only)
     *
     * Usage: Admin Support page - "AI Suggest Reply" button
     *
     * POST /api/ai/support-faqs
     * Body: SupportFAQRequest
     * Response: SupportFAQResponse
     */
    @PostMapping("/support-faqs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "AI suggests support ticket reply",
            description = "Generate suggested reply for admin to send to user (Admin only)"
    )
    public ResponseEntity<SupportFAQResponse> generateSupportReply(
            @RequestBody SupportFAQRequest request
    ) {
        log.info("🤖 AI Request: Generate support reply for ticket: {}", request.getTicketId());

        try {
            SupportFAQResponse response = aiService.generateSupportReply(request);

            if (response.getSuccess()) {
                log.info("✅ AI Response: Successfully generated support reply");
                return ResponseEntity.ok(response);
            } else {
                log.warn("⚠️ AI Response: Failed - {}", response.getError());
                return ResponseEntity.status(500).body(response);
            }

        } catch (Exception e) {
            log.error("❌ AI Controller Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(
                    SupportFAQResponse.builder()
                            .success(false)
                            .error("Lỗi hệ thống: " + e.getMessage())
                            .timestamp(java.time.LocalDateTime.now().toString())
                            .build()
            );
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "AI service health check")
    public ResponseEntity<java.util.Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(java.util.Map.of(
                "status", "OK",
                "service", "AI Service",
                "mode", "Mock (No API key required)",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}