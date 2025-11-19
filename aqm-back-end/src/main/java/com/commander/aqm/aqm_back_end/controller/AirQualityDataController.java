package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.AirQualityDataDto;
import com.commander.aqm.aqm_back_end.model.AirQualityData;
import com.commander.aqm.aqm_back_end.repository.AirQualityDataRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/data")
@Tag(name = "AirQualityData APIs", description = "Manage airqualitydata")
@RequiredArgsConstructor
public class AirQualityDataController {

    private final AirQualityDataRepository dataRepo;

    @GetMapping
    public List<AirQualityDataDto> getData(  // ✅ TRẢ VỀ DTO THAY VÌ ENTITY
                                             @RequestParam Long location,
                                             @RequestParam(defaultValue = "24h") String range
    ) {
        LocalDateTime start = LocalDateTime.now().minusHours(parseHours(range));
        return dataRepo.findAll().stream()
                .filter(data -> data.getLocation().getId().equals(location) &&
                        data.getTimestampUtc().isAfter(start))
                .map(AirQualityDataDto::from)  // ✅ CONVERT TO DTO
                .toList();
    }
//    public List<AirQualityData> getData(
//            @RequestParam Long location,
//            @RequestParam(defaultValue = "24h") String range
//    ) {
//        LocalDateTime start = LocalDateTime.now().minusHours(parseHours(range));
//        return dataRepo.findAll().stream()
//                .filter(data -> data.getLocation().getId().equals(location) &&
//                        data.getTimestampUtc().isAfter(start))
//                .toList();
//    }

    private long parseHours(String range) {
        if (range.endsWith("h")) return Long.parseLong(range.replace("h", ""));
        return 24;
    }

//    @Query("SELECT d FROM AirQualityData d WHERE d.location.id = :locationId AND d.timestampUtc >= :from")
//    List<AirQualityData> findRecentByLocation(@Param("locationId") Long locationId, @Param("from") LocalDateTime from);
}
