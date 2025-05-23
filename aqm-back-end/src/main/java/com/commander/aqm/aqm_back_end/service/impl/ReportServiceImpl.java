package com.commander.aqm.aqm_back_end.service.impl;

import com.commander.aqm.aqm_back_end.model.Report;
import com.commander.aqm.aqm_back_end.repository.ReportRepository;
import com.commander.aqm.aqm_back_end.dto.ReportDto;
import com.commander.aqm.aqm_back_end.model.AirQualityData;
import com.commander.aqm.aqm_back_end.model.Location;
import com.commander.aqm.aqm_back_end.model.User;
import com.commander.aqm.aqm_back_end.repository.AirQualityDataRepository;
import com.commander.aqm.aqm_back_end.repository.LocationRepository;
import com.commander.aqm.aqm_back_end.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final AirQualityDataRepository airRepo;
    private final LocationRepository locationRepo;
    private final ReportRepository reportRepo;

    @Override
    public ReportDto generate(Long locationId, LocalDateTime from, LocalDateTime to, User user) {
        Location location = locationRepo.findById(locationId).orElseThrow();

        List<AirQualityData> data = airRepo.findByLocationIdAndTimestampUtcBetween(locationId, from, to);

        double avgPm25 = data.stream().mapToDouble(d -> d.getPm25() != null ? d.getPm25() : 0).average().orElse(0);
        double avgPm10 = data.stream().mapToDouble(d -> d.getPm10() != null ? d.getPm10() : 0).average().orElse(0);
        double avgAqi  = data.stream().mapToDouble(d -> d.getAqi()  != null ? d.getAqi()  : 0).average().orElse(0);

        Report report = reportRepo.save(Report.builder()
                .user(user)
                .location(location)
                .fromDate(from)
                .toDate(to)
                .avgPm25(avgPm25)
                .avgPm10(avgPm10)
                .avgAqi(avgAqi)
                .generatedAt(LocalDateTime.now())
                .build());

        ReportDto dto = new ReportDto();
        dto.setLocationId(locationId);
        dto.setLocationName(location.getName());
        dto.setFrom(from);
        dto.setTo(to);
        dto.setAvgPm25(avgPm25);
        dto.setAvgPm10(avgPm10);
        dto.setAvgAqi(avgAqi);

        return dto;
    }
}
