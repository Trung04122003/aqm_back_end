// src/main/java/com/commander/aqm/aqm_back_end/service/impl/ReportServiceImpl.java
package com.commander.aqm.aqm_back_end.service.impl;

import com.commander.aqm.aqm_back_end.dto.ReportDto;
import com.commander.aqm.aqm_back_end.model.*;
import com.commander.aqm.aqm_back_end.repository.*;
import com.commander.aqm.aqm_back_end.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalInt;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final AirQualityDataRepository airRepo;
    private final LocationRepository locationRepo;
    private final ReportRepository reportRepo;

    @Override
    @Transactional
    public ReportDto generate(Long locationId, LocalDateTime from, LocalDateTime to, User user) {
        Location location = locationRepo.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found: " + locationId));

        // ✅ Get all data in date range
        List<AirQualityData> data = airRepo.findByLocationIdAndTimestampUtcBetween(locationId, from, to);

        if (data.isEmpty()) {
            throw new RuntimeException("No data available for the selected period");
        }

        // ✅ Calculate statistics
        double avgPm25 = data.stream()
                .filter(d -> d.getPm25() != null)
                .mapToDouble(AirQualityData::getPm25)
                .average()
                .orElse(0.0);

        double avgPm10 = data.stream()
                .filter(d -> d.getPm10() != null)
                .mapToDouble(AirQualityData::getPm10)
                .average()
                .orElse(0.0);

        double avgAqi = data.stream()
                .filter(d -> d.getAqi() != null)
                .mapToDouble(AirQualityData::getAqi)
                .average()
                .orElse(0.0);

        OptionalInt maxAqi = data.stream()
                .filter(d -> d.getAqi() != null)
                .mapToInt(AirQualityData::getAqi)
                .max();

        OptionalInt minAqi = data.stream()
                .filter(d -> d.getAqi() != null)
                .mapToInt(AirQualityData::getAqi)
                .min();

        // ✅ Calculate distribution (Good/Moderate/Unhealthy)
        int goodDays = (int) data.stream()
                .filter(d -> d.getAqi() != null && d.getAqi() <= 50)
                .count();

        int moderateDays = (int) data.stream()
                .filter(d -> d.getAqi() != null && d.getAqi() > 50 && d.getAqi() <= 100)
                .count();

        int unhealthyDays = (int) data.stream()
                .filter(d -> d.getAqi() != null && d.getAqi() > 100)
                .count();

        // ✅ Build and save report
        Report report = Report.builder()
                .user(user)
                .location(location)
                .reportType(Report.ReportType.CUSTOM)
                .startTimestamp(from)
                .endTimestamp(to)
                .avgPm25(avgPm25)
                .avgPm10(avgPm10)
                .avgAqi(avgAqi)
                .maxAqi(maxAqi.isPresent() ? maxAqi.getAsInt() : null)
                .minAqi(minAqi.isPresent() ? minAqi.getAsInt() : null)
                .goodDays(goodDays)
                .moderateDays(moderateDays)
                .unhealthyDays(unhealthyDays)
                .totalDataPoints(data.size())
                .build();

        reportRepo.save(report);

        // ✅ Return DTO
        return ReportDto.from(report);
    }
}