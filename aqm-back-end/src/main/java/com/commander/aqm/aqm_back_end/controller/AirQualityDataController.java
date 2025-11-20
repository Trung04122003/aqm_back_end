package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.AirQualityDataDto;
import com.commander.aqm.aqm_back_end.model.AirQualityData;
import com.commander.aqm.aqm_back_end.repository.AirQualityDataRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class AirQualityDataController {

    private final AirQualityDataRepository dataRepo;

    @GetMapping
    public ResponseEntity<?> getData(
            @RequestParam Long locationId,
            @RequestParam(defaultValue = "24h") String range
    ) {
        LocalDateTime start = LocalDateTime.now().minusHours(parseHours(range));
        List<AirQualityData> data = dataRepo.findByLocationIdAndTimestampUtcAfter(locationId, start);

        // Lấy data mới nhất
        AirQualityData latest = data.isEmpty() ? null : data.get(data.size() - 1);

        Map<String, Object> current = new HashMap<>();
        if (latest != null) {
            current.put("aqi", latest.getAqi());
            current.put("pm25", latest.getPm25());
        }

        List<Map<String, Object>> history = data.stream().map(d -> {
            Map<String, Object> item = new HashMap<>();
            item.put("ts", d.getTimestampUtc().toString());
            item.put("value", d.getAqi());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("current", current);
        result.put("history", history);

        return ResponseEntity.ok(result);
    }

    private long parseHours(String range) {
        return Long.parseLong(range.replace("h", ""));
    }
}
//@RestController
//@RequestMapping("/api/data")
//@Tag(name = "AirQualityData APIs", description = "Manage airqualitydata")
//@RequiredArgsConstructor
//public class AirQualityDataController {
//
//    private final AirQualityDataRepository dataRepo;
//
//    @GetMapping
//    public List<AirQualityDataDto> getData(  // ✅ TRẢ VỀ DTO THAY VÌ ENTITY
//                                             @RequestParam Long location,
//                                             @RequestParam(defaultValue = "24h") String range,
//                                             Pageable pageable  // ✅ THÊM PAGINATION
//    ) {
//        LocalDateTime start = LocalDateTime.now().minusHours(parseHours(range));
//        return dataRepo.findAll().stream()
//                .filter(data -> data.getLocation().getId().equals(location) &&
//                        data.getTimestampUtc().isAfter(start))
//                .map(AirQualityDataDto::from)  // ✅ CONVERT TO DTO
//                .toList();
//    }
//
//    private long parseHours(String range) {
//        if (range.endsWith("h")) return Long.parseLong(range.replace("h", ""));
//        return 24;
//    }
//}


//    @Query("SELECT d FROM AirQualityData d WHERE d.location.id = :locationId AND d.timestampUtc >= :from")
//    List<AirQualityData> findRecentByLocation(@Param("locationId") Long locationId, @Param("from") LocalDateTime from);
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