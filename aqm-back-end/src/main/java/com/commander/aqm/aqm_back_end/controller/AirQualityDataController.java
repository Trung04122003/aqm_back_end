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