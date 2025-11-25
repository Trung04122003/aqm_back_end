// src/main/java/com/commander/aqm/aqm_back_end/controller/AlertController.java
package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.AlertDto;
import com.commander.aqm.aqm_back_end.security.JwtService;
import com.commander.aqm.aqm_back_end.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts", description = "Alert management endpoints")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final JwtService jwtService;

    @Operation(summary = "Get all alerts for current user")
    @GetMapping
    public ResponseEntity<List<AlertDto>> getAll(@RequestHeader("Authorization") String auth) {
        var user = jwtService.extractUser(auth);

        // ✅ Convert to DTO before returning
        List<AlertDto> alerts = alertService.getAllAlerts(user).stream()
                .map(AlertDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(alerts);
    }

    @Operation(summary = "Get unread alerts for current user")
    @GetMapping("/unread")
    public ResponseEntity<List<AlertDto>> getUnread(@RequestHeader("Authorization") String auth) {
        var user = jwtService.extractUser(auth);

        // ✅ Convert to DTO
        List<AlertDto> alerts = alertService.getUnreadAlerts(user).stream()
                .map(AlertDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(alerts);
    }

    @Operation(summary = "Mark alert as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<AlertDto> markAsRead(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth
    ) {
        var user = jwtService.extractUser(auth);
        var alert = alertService.markAsRead(id, user);

        // ✅ Return DTO
        return ResponseEntity.ok(AlertDto.from(alert));
    }
}