// src/main/java/com/commander/aqm/aqm_back_end/service/AIService.java
package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AI Service - Provides AI-powered insights for Air Quality Monitoring
 *
 * Architecture: Decision Support Layer
 * - AI does NOT replace logic or make decisions
 * - AI interprets, suggests, and supports user understanding
 *
 * Current Implementation: Mock responses (no API key required)
 * Future: Can be easily switched to real Claude API
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AIService {

    @Value("${ai.claude.api-key:NOT_SET}")
    private String claudeApiKey;

    @Value("${ai.mock.enabled:true}")
    private boolean mockEnabled;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Explain current AQI with health recommendations
     * Endpoint: POST /api/ai/explain-current-aqi
     */
    public ExplainAQIResponse explainCurrentAQI(ExplainAQIRequest request) {
        log.info("🤖 AI: Explaining AQI for location: {}", request.getLocationName());

        try {
            if (mockEnabled) {
                return generateMockExplanation(request);
            } else {
                // TODO: Implement real Claude API call
                return callClaudeAPIForExplanation(request);
            }
        } catch (Exception e) {
            log.error("❌ AI Error: {}", e.getMessage());
            return ExplainAQIResponse.builder()
                    .success(false)
                    .error("Không thể tạo giải thích AI: " + e.getMessage())
                    .timestamp(LocalDateTime.now().format(formatter))
                    .build();
        }
    }

    /**
     * Summarize multiple alerts with trend analysis
     * Endpoint: POST /api/ai/summarize-alerts
     */
    public SummarizeAlertsResponse summarizeAlerts(SummarizeAlertsRequest request) {
        log.info("🤖 AI: Summarizing {} alerts", request.getAlerts().size());

        try {
            if (mockEnabled) {
                return generateMockAlertSummary(request);
            } else {
                // TODO: Implement real Claude API call
                return callClaudeAPIForAlertSummary(request);
            }
        } catch (Exception e) {
            log.error("❌ AI Error: {}", e.getMessage());
            return SummarizeAlertsResponse.builder()
                    .success(false)
                    .error("Không thể tạo tóm tắt AI: " + e.getMessage())
                    .timestamp(LocalDateTime.now().format(formatter))
                    .build();
        }
    }

    /**
     * AI Chat Assistant
     * Endpoint: POST /api/ai/chat
     */
    public ChatResponse chat(ChatRequest request) {
        log.info("🤖 AI: Processing chat message: {}", request.getMessage());

        try {
            if (mockEnabled) {
                return generateMockChatResponse(request);
            } else {
                // TODO: Implement real Claude API call
                return callClaudeAPIForChat(request);
            }
        } catch (Exception e) {
            log.error("❌ AI Error: {}", e.getMessage());
            return ChatResponse.builder()
                    .success(false)
                    .error("Không thể xử lý tin nhắn: " + e.getMessage())
                    .timestamp(LocalDateTime.now().format(formatter))
                    .build();
        }
    }

    /**
     * Generate suggested reply for support ticket
     * Endpoint: POST /api/ai/support-faqs
     */
    public SupportFAQResponse generateSupportReply(SupportFAQRequest request) {
        log.info("🤖 AI: Generating support reply for ticket: {}", request.getTicketId());

        try {
            if (mockEnabled) {
                return generateMockSupportReply(request);
            } else {
                // TODO: Implement real Claude API call
                return callClaudeAPIForSupportReply(request);
            }
        } catch (Exception e) {
            log.error("❌ AI Error: {}", e.getMessage());
            return SupportFAQResponse.builder()
                    .success(false)
                    .error("Không thể tạo câu trả lời: " + e.getMessage())
                    .timestamp(LocalDateTime.now().format(formatter))
                    .build();
        }
    }

    // ==================== MOCK RESPONSE GENERATORS ====================

    private ExplainAQIResponse generateMockExplanation(ExplainAQIRequest req) {
        String status = getAQIStatus(req.getAqi());
        String explanation = buildExplanation(req);
        String recommendations = buildRecommendations(req.getAqi());

        return ExplainAQIResponse.builder()
                .explanation(explanation)
                .recommendations(recommendations)
                .status(status)
                .success(true)
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    private String buildExplanation(ExplainAQIRequest req) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("🌍 **Chất lượng không khí tại %s**\n\n", req.getLocationName()));
        sb.append(String.format("Chỉ số AQI hiện tại là **%d** - %s.\n\n",
                req.getAqi(), getAQIStatus(req.getAqi())));

        sb.append("📊 **Phân tích chi tiết:**\n");
        sb.append(String.format("- PM2.5: %.1f µg/m³ %s\n",
                req.getPm25(), analyzePollutant("PM2.5", req.getPm25())));
        sb.append(String.format("- PM10: %.1f µg/m³ %s\n",
                req.getPm10(), analyzePollutant("PM10", req.getPm10())));

        if (req.getNo2() != null && req.getNo2() > 0) {
            sb.append(String.format("- NO₂: %.3f mg/m³ %s\n",
                    req.getNo2(), analyzePollutant("NO2", req.getNo2())));
        }

        sb.append("\n💡 **Diễn giải:**\n");
        if (req.getAqi() <= 50) {
            sb.append("Không khí trong lành, rất tốt cho sức khỏe. Đây là điều kiện lý tưởng cho mọi hoạt động ngoài trời.");
        } else if (req.getAqi() <= 100) {
            sb.append("Chất lượng không khí ở mức chấp nhận được. Nhóm nhạy cảm nên hạn chế hoạt động ngoài trời kéo dài.");
        } else if (req.getAqi() <= 150) {
            sb.append("Không khí có hại cho nhóm nhạy cảm. Mọi người có thể bắt đầu cảm nhận tác động sức khỏe.");
        } else {
            sb.append("Chất lượng không khí kém, có hại cho sức khỏe. Mọi người nên hạn chế ra ngoài.");
        }

        return sb.toString();
    }

    private String buildRecommendations(Integer aqi) {
        if (aqi <= 50) {
            return "✅ Hoạt động bình thường\n" +
                    "✅ Mở cửa sổ để lưu thông không khí\n" +
                    "✅ Tốt cho tập thể dục ngoài trời";
        } else if (aqi <= 100) {
            return "⚠️ Nhóm nhạy cảm nên hạn chế hoạt động ngoài trời kéo dài\n" +
                    "✅ Người khỏe mạnh có thể hoạt động bình thường\n" +
                    "💡 Theo dõi tình trạng sức khỏe";
        } else if (aqi <= 150) {
            return "⚠️ Giảm hoạt động ngoài trời\n" +
                    "😷 Đeo khẩu trang khi ra ngoài\n" +
                    "🏠 Sử dụng máy lọc không khí trong nhà\n" +
                    "🚫 Tránh tập thể dục ngoài trời";
        } else {
            return "🚨 Tránh ra ngoài nếu không cần thiết\n" +
                    "😷 Bắt buộc đeo khẩu trang N95\n" +
                    "🏠 Đóng cửa sổ, sử dụng máy lọc không khí\n" +
                    "🚫 Hủy mọi hoạt động ngoài trời\n" +
                    "👨‍⚕️ Người có bệnh nền cần đặc biệt chú ý";
        }
    }

    private SummarizeAlertsResponse generateMockAlertSummary(SummarizeAlertsRequest req) {
        int totalAlerts = req.getAlerts().size();
        long unreadCount = req.getAlerts().stream().filter(a -> !a.getIsRead()).count();

        String summary = String.format(
                "📊 **Tổng quan cảnh báo %s**\n\n" +
                        "Hệ thống ghi nhận **%d cảnh báo** chất lượng không khí, " +
                        "trong đó có **%d cảnh báo chưa đọc**.",
                req.getTimePeriod(), totalAlerts, unreadCount
        );

        String trend = analyzeTrend(req.getAlerts());
        String keyFindings = analyzeKeyFindings(req.getAlerts());
        String recommendations = generateAlertRecommendations(req.getAlerts());

        return SummarizeAlertsResponse.builder()
                .summary(summary)
                .trend(trend)
                .keyFindings(keyFindings)
                .recommendations(recommendations)
                .success(true)
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    private ChatResponse generateMockChatResponse(ChatRequest req) {
        String message = req.getMessage().toLowerCase();
        String response;

        if (message.contains("aqi") || message.contains("chất lượng không khí")) {
            response = "Chỉ số AQI (Air Quality Index) là thước đo chất lượng không khí. " +
                    "Thang đo từ 0-500, càng thấp càng tốt. Bạn có muốn biết AQI hiện tại ở đâu không?";
        } else if (message.contains("pm2.5") || message.contains("pm25")) {
            response = "PM2.5 là các hạt bụi siêu mịn có đường kính nhỏ hơn 2.5 micromet. " +
                    "Chúng rất nguy hiểm vì có thể xâm nhập sâu vào phổi và gây hại sức khỏe.";
        } else if (message.contains("khẩu trang") || message.contains("mask")) {
            response = "Khẩu trang N95 hoặc KF94 là lựa chọn tốt nhất khi AQI cao. " +
                    "Chúng lọc được 95% hạt bụi PM2.5. Nhớ thay mới sau 8 giờ sử dụng nhé!";
        } else if (message.contains("máy lọc") || message.contains("air purifier")) {
            response = "Máy lọc không khí với bộ lọc HEPA hiệu quả với PM2.5. " +
                    "Nên đặt trong phòng kín, chạy liên tục khi AQI cao.";
        } else {
            response = "Xin chào! Tôi là trợ lý AI về chất lượng không khí. " +
                    "Tôi có thể giúp bạn hiểu về AQI, PM2.5, và cách bảo vệ sức khỏe. " +
                    "Bạn muốn hỏi gì?";
        }

        String[] suggestions = {
                "AQI hiện tại là bao nhiêu?",
                "PM2.5 nguy hiểm như thế nào?",
                "Tôi nên đeo khẩu trang gì?"
        };

        return ChatResponse.builder()
                .message(response)
                .suggestions(suggestions)
                .success(true)
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    private SupportFAQResponse generateMockSupportReply(SupportFAQRequest req) {
        String suggestedReply = buildSupportReply(req);
        String category = categorizeTicket(req.getSubject(), req.getUserMessage());

        return SupportFAQResponse.builder()
                .suggestedReply(suggestedReply)
                .category(category)
                .confidence("High")
                .adminNotes("Câu trả lời được tạo bởi AI. Admin nên xem xét trước khi gửi.")
                .success(true)
                .timestamp(LocalDateTime.now().format(formatter))
                .build();
    }

    // ==================== HELPER METHODS ====================

    private String getAQIStatus(Integer aqi) {
        if (aqi <= 50) return "Tốt";
        if (aqi <= 100) return "Trung bình";
        if (aqi <= 150) return "Không lành mạnh cho nhóm nhạy cảm";
        if (aqi <= 200) return "Không lành mạnh";
        if (aqi <= 300) return "Rất không lành mạnh";
        return "Nguy hại";
    }

    private String analyzePollutant(String type, Float value) {
        if (type.equals("PM2.5")) {
            if (value <= 12) return "(Tốt)";
            if (value <= 35.4) return "(Trung bình)";
            if (value <= 55.4) return "(Kém cho nhóm nhạy cảm)";
            return "(Nguy hại)";
        } else if (type.equals("PM10")) {
            if (value <= 54) return "(Tốt)";
            if (value <= 154) return "(Trung bình)";
            return "(Kém)";
        }
        return "";
    }

    private String analyzeTrend(java.util.List<SummarizeAlertsRequest.AlertSummaryItem> alerts) {
        // Simple trend analysis based on values
        double avgValue = alerts.stream()
                .mapToDouble(SummarizeAlertsRequest.AlertSummaryItem::getValue)
                .average().orElse(0);

        if (avgValue > 100) {
            return "📈 **Xu hướng tăng** - Chất lượng không khí đang xấu đi";
        } else if (avgValue > 50) {
            return "➡️ **Ổn định** - Chất lượng không khí dao động ở mức trung bình";
        } else {
            return "📉 **Xu hướng giảm** - Chất lượng không khí đang cải thiện";
        }
    }

    private String analyzeKeyFindings(java.util.List<SummarizeAlertsRequest.AlertSummaryItem> alerts) {
        java.util.Map<String, Long> pollutantCount = alerts.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        SummarizeAlertsRequest.AlertSummaryItem::getPollutant,
                        java.util.stream.Collectors.counting()
                ));

        StringBuilder sb = new StringBuilder("🔍 **Phát hiện chính:**\n");
        pollutantCount.forEach((pollutant, count) -> {
            sb.append(String.format("- %s: %d cảnh báo\n", pollutant, count));
        });

        return sb.toString();
    }

    private String generateAlertRecommendations(java.util.List<SummarizeAlertsRequest.AlertSummaryItem> alerts) {
        return "💡 **Khuyến nghị:**\n" +
                "- Theo dõi chất lượng không khí thường xuyên\n" +
                "- Hạn chế hoạt động ngoài trời khi có cảnh báo\n" +
                "- Sử dụng thiết bị bảo vệ khi cần thiết";
    }

    private String buildSupportReply(SupportFAQRequest req) {
        return String.format(
                "Kính gửi %s,\n\n" +
                        "Cảm ơn bạn đã liên hệ với chúng tôi về vấn đề: \"%s\".\n\n" +
                        "Chúng tôi đã xem xét yêu cầu của bạn và xin phản hồi như sau:\n\n" +
                        "%s\n\n" +
                        "Nếu bạn cần thêm thông tin, vui lòng liên hệ lại.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ hỗ trợ AQM System",
                req.getUserName(),
                req.getSubject(),
                generateSpecificReply(req.getUserMessage())
        );
    }

    private String generateSpecificReply(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("không nhận được") || lower.contains("cảnh báo")) {
            return "Vui lòng kiểm tra:\n" +
                    "1. Thiết lập ngưỡng cảnh báo trong Settings\n" +
                    "2. Email notifications đã được bật\n" +
                    "3. Kiểm tra thư mục spam";
        } else if (lower.contains("dữ liệu") || lower.contains("không chính xác")) {
            return "Dữ liệu được cập nhật mỗi 30 phút từ OpenWeatherMap. " +
                    "Nếu bạn thấy sai lệch, vui lòng thử refresh hoặc fetch new data.";
        } else {
            return "Chúng tôi đang xem xét vấn đề của bạn và sẽ phản hồi chi tiết trong thời gian sớm nhất.";
        }
    }

    private String categorizeTicket(String subject, String message) {
        String combined = (subject + " " + message).toLowerCase();

        if (combined.contains("cảnh báo") || combined.contains("alert")) return "Alerts";
        if (combined.contains("dữ liệu") || combined.contains("data")) return "Data Issues";
        if (combined.contains("đăng nhập") || combined.contains("login")) return "Account";
        if (combined.contains("báo cáo") || combined.contains("report")) return "Reports";

        return "General";
    }

    // ==================== REAL CLAUDE API METHODS (TODO) ====================

    private ExplainAQIResponse callClaudeAPIForExplanation(ExplainAQIRequest request) {
        // TODO: Implement when you have Claude API key
        // Use the prompt strategy from the document
        throw new UnsupportedOperationException("Claude API integration pending");
    }

    private SummarizeAlertsResponse callClaudeAPIForAlertSummary(SummarizeAlertsRequest request) {
        // TODO: Implement when you have Claude API key
        throw new UnsupportedOperationException("Claude API integration pending");
    }

    private ChatResponse callClaudeAPIForChat(ChatRequest request) {
        // TODO: Implement when you have Claude API key
        throw new UnsupportedOperationException("Claude API integration pending");
    }

    private SupportFAQResponse callClaudeAPIForSupportReply(SupportFAQRequest request) {
        // TODO: Implement when you have Claude API key
        throw new UnsupportedOperationException("Claude API integration pending");
    }
}