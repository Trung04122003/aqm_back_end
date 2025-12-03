// src/main/java/com/commander/aqm/aqm_back_end/service/HtmlReportService.java
package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.AirQualityData;
import com.commander.aqm.aqm_back_end.model.Report;
import com.commander.aqm.aqm_back_end.repository.AirQualityDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HtmlReportService {

    private final AirQualityDataRepository airRepo;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generateReportHtml(Report report) throws Exception {
        // Get raw data for chart
        List<AirQualityData> data = airRepo.findByLocationIdAndTimestampUtcBetween(
                report.getLocation().getId(),
                report.getStartTimestamp(),
                report.getEndTimestamp()
        );

        // Build chart data
        String chartLabels = data.stream()
                .map(d -> "'" + formatTime(d.getTimestampUtc()) + "'")
                .collect(Collectors.joining(","));

        String chartData = data.stream()
                .map(d -> String.valueOf(d.getAqi() != null ? d.getAqi() : 0))
                .collect(Collectors.joining(","));

        int totalDays = report.getGoodDays() + report.getModerateDays() + report.getUnhealthyDays();
        double goodPct = (report.getGoodDays() * 100.0 / totalDays);
        double moderatePct = (report.getModerateDays() * 100.0 / totalDays);
        double unhealthyPct = (report.getUnhealthyDays() * 100.0 / totalDays);

        String html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>🎄 Air Quality Report - %s</title>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #E0F7FA 0%%, #B3E5FC 50%%, #FFFAFA 100%%);
            padding: 2rem;
            min-height: 100vh;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 24px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #C41E3A 0%%, #165B33 100%%);
            color: white;
            padding: 2rem;
            text-align: center;
            border-bottom: 4px solid #FFD700;
        }
        .header h1 { font-size: 2rem; margin-bottom: 0.5rem; }
        .header p { opacity: 0.9; font-size: 1.1rem; }
        .content { padding: 2rem; }
        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 1rem;
            margin-bottom: 2rem;
        }
        .info-card {
            background: #f8f9fa;
            padding: 1rem;
            border-radius: 12px;
            border-left: 4px solid #C41E3A;
        }
        .info-card label {
            font-weight: 600;
            color: #666;
            display: block;
            margin-bottom: 0.25rem;
            font-size: 0.9rem;
        }
        .info-card value {
            color: #165B33;
            font-size: 1.1rem;
            font-weight: 700;
        }
        .metrics-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2rem;
        }
        .metric-card {
            background: linear-gradient(135deg, #fff 0%%, #f8f9fa 100%%);
            padding: 1.5rem;
            border-radius: 16px;
            text-align: center;
            border: 2px solid #FFD700;
            transition: transform 0.2s;
        }
        .metric-card:hover { transform: translateY(-5px); }
        .metric-card .icon { font-size: 2rem; margin-bottom: 0.5rem; }
        .metric-card .value {
            font-size: 2rem;
            font-weight: 700;
            color: #C41E3A;
            margin-bottom: 0.25rem;
        }
        .metric-card .label { color: #666; font-weight: 600; }
        .chart-container {
            background: white;
            padding: 2rem;
            border-radius: 16px;
            margin-bottom: 2rem;
            border: 2px solid #FFD700;
        }
        .chart-container h3 {
            color: #165B33;
            margin-bottom: 1rem;
            font-size: 1.5rem;
        }
        .distribution {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 1rem;
            margin-bottom: 2rem;
        }
        .dist-card {
            padding: 1.5rem;
            border-radius: 16px;
            text-align: center;
            color: white;
            border: 3px solid #FFD700;
        }
        .dist-card.good { background: linear-gradient(135deg, #10b981, #059669); }
        .dist-card.moderate { background: linear-gradient(135deg, #f59e0b, #d97706); }
        .dist-card.unhealthy { background: linear-gradient(135deg, #ef4444, #dc2626); }
        .dist-card .emoji { font-size: 3rem; margin-bottom: 0.5rem; }
        .dist-card .value { font-size: 2.5rem; font-weight: 700; margin-bottom: 0.25rem; }
        .dist-card .label { font-size: 1.1rem; font-weight: 600; margin-bottom: 0.5rem; }
        .dist-card .pct { font-size: 0.9rem; opacity: 0.9; }
        .progress-bar {
            height: 30px;
            border-radius: 15px;
            overflow: hidden;
            display: flex;
            border: 2px solid #FFD700;
            margin: 2rem 0;
        }
        .progress-bar div {
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: 600;
            font-size: 0.9rem;
        }
        .progress-good { background: #10b981; }
        .progress-moderate { background: #f59e0b; }
        .progress-unhealthy { background: #ef4444; }
        .footer {
            background: #f8f9fa;
            padding: 1.5rem;
            text-align: center;
            border-top: 2px solid #FFD700;
            color: #666;
        }
        .print-btn {
            background: linear-gradient(135deg, #C41E3A, #165B33);
            color: white;
            border: none;
            padding: 1rem 2rem;
            border-radius: 12px;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            margin: 1rem;
            border: 2px solid #FFD700;
        }
        .print-btn:hover { opacity: 0.9; }
        @media print {
            body { background: white; padding: 0; }
            .print-btn { display: none; }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎄 Air Quality Report 🎄</h1>
            <p>Comprehensive Analysis & Statistics</p>
        </div>

        <div class="content">
            <!-- Report Information -->
            <div class="info-grid">
                <div class="info-card">
                    <label>📋 Report ID</label>
                    <value>%d</value>
                </div>
                <div class="info-card">
                    <label>👤 Generated By</label>
                    <value>%s</value>
                </div>
                <div class="info-card">
                    <label>📍 Location</label>
                    <value>%s</value>
                </div>
                <div class="info-card">
                    <label>📅 Period</label>
                    <value>%s - %s</value>
                </div>
                <div class="info-card">
                    <label>🕐 Generated At</label>
                    <value>%s</value>
                </div>
                <div class="info-card">
                    <label>📊 Data Points</label>
                    <value>%d</value>
                </div>
            </div>

            <!-- Key Metrics -->
            <h3 style="color: #165B33; margin-bottom: 1rem; font-size: 1.5rem;">📊 Key Metrics</h3>
            <div class="metrics-grid">
                <div class="metric-card">
                    <div class="icon">🌫️</div>
                    <div class="value">%.2f</div>
                    <div class="label">Avg PM2.5 (µg/m³)</div>
                </div>
                <div class="metric-card">
                    <div class="icon">💨</div>
                    <div class="value">%.2f</div>
                    <div class="label">Avg PM10 (µg/m³)</div>
                </div>
                <div class="metric-card">
                    <div class="icon">📈</div>
                    <div class="value">%.0f</div>
                    <div class="label">Average AQI</div>
                </div>
                <div class="metric-card">
                    <div class="icon">⚠️</div>
                    <div class="value">%s</div>
                    <div class="label">Max AQI</div>
                </div>
            </div>

            <!-- AQI Trend Chart -->
            <div class="chart-container">
                <h3>📈 AQI Trend (24 Hours)</h3>
                <canvas id="aqiChart"></canvas>
            </div>

            <!-- Distribution Analysis -->
            <h3 style="color: #165B33; margin-bottom: 1rem; font-size: 1.5rem;">📊 Air Quality Distribution</h3>
            <div class="distribution">
                <div class="dist-card good">
                    <div class="emoji">😊</div>
                    <div class="value">%d</div>
                    <div class="label">Good Days</div>
                    <div class="pct">%.1f%% of period</div>
                </div>
                <div class="dist-card moderate">
                    <div class="emoji">😐</div>
                    <div class="value">%d</div>
                    <div class="label">Moderate Days</div>
                    <div class="pct">%.1f%% of period</div>
                </div>
                <div class="dist-card unhealthy">
                    <div class="emoji">😷</div>
                    <div class="value">%d</div>
                    <div class="label">Unhealthy Days</div>
                    <div class="pct">%.1f%% of period</div>
                </div>
            </div>

            <!-- Progress Bar -->
            <div class="progress-bar">
                <div class="progress-good" style="width: %.1f%%">%.1f%%</div>
                <div class="progress-moderate" style="width: %.1f%%">%.1f%%</div>
                <div class="progress-unhealthy" style="width: %.1f%%">%.1f%%</div>
            </div>

            <!-- Print Button -->
            <div style="text-align: center;">
                <button class="print-btn" onclick="window.print()">🖨️ Print Report</button>
            </div>
        </div>

        <div class="footer">
            <p>🎅 North Pole Air Quality Monitoring System 🎄</p>
            <p style="margin-top: 0.5rem; font-size: 0.9rem;">Generated on %s</p>
        </div>
    </div>

    <script>
        // AQI Trend Chart
        const ctx = document.getElementById('aqiChart').getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: [%s],
                datasets: [{
                    label: 'AQI Level',
                    data: [%s],
                    borderColor: '#C41E3A',
                    backgroundColor: 'rgba(196, 30, 58, 0.1)',
                    fill: true,
                    tension: 0.4,
                    pointRadius: 4,
                    pointHoverRadius: 6,
                    pointBackgroundColor: '#FFD700',
                    pointBorderColor: '#C41E3A',
                    pointBorderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        padding: 12,
                        titleFont: { size: 14, weight: 'bold' },
                        bodyFont: { size: 13 }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: 'rgba(196, 30, 58, 0.1)' },
                        ticks: { color: '#165B33', font: { weight: 'bold' } }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { color: '#165B33', font: { weight: 'bold' } }
                    }
                }
            }
        });
    </script>
</body>
</html>
        """.formatted(
                report.getLocation().getName(),
                report.getId(),
                report.getUser().getUsername(),
                report.getLocation().getName(),
                formatDate(report.getStartTimestamp()),
                formatDate(report.getEndTimestamp()),
                formatDate(report.getCreatedAt()),
                report.getTotalDataPoints(),
                report.getAvgPm25(),
                report.getAvgPm10(),
                report.getAvgAqi(),
                report.getMaxAqi() != null ? report.getMaxAqi().toString() : "N/A",
                report.getGoodDays(), goodPct,
                report.getModerateDays(), moderatePct,
                report.getUnhealthyDays(), unhealthyPct,
                goodPct, goodPct,
                moderatePct, moderatePct,
                unhealthyPct, unhealthyPct,
                formatDate(java.time.LocalDateTime.now()),
                chartLabels,
                chartData
        );

        return html.getBytes(StandardCharsets.UTF_8);
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DATE_FORMAT);
    }

    private String formatTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}