package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.ReportDto;
import com.commander.aqm.aqm_back_end.security.JwtService;
import com.commander.aqm.aqm_back_end.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final JwtService jwtService;

    @GetMapping
    public ReportDto generate(
            @RequestHeader("Authorization") String auth,
            @RequestParam Long locationId,
            @RequestParam String from,
            @RequestParam String to
    ) {
        LocalDateTime start = LocalDateTime.parse(from);
        LocalDateTime end = LocalDateTime.parse(to);
        return reportService.generate(locationId, start, end, jwtService.extractUser(auth));
    }
}
