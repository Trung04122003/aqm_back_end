package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.ReportDto;
import com.commander.aqm.aqm_back_end.security.JwtService;
import com.commander.aqm.aqm_back_end.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final JwtService jwtService;

    // ✅ FIX: Add query params support
    @GetMapping
    public ResponseEntity<ReportDto> generate(
            @RequestHeader("Authorization") String auth,
            @RequestParam Long locationId,
            @RequestParam String from, // Format: yyyy-MM-dd
            @RequestParam String to
    ) {
        LocalDateTime start = LocalDate.parse(from).atStartOfDay();
        LocalDateTime end = LocalDate.parse(to).atTime(23, 59, 59);

        return ResponseEntity.ok(
                reportService.generate(locationId, start, end, jwtService.extractUser(auth))
        );
    }
}
