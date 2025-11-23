package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.model.Alert;
import com.commander.aqm.aqm_back_end.security.JwtService;
import com.commander.aqm.aqm_back_end.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final JwtService jwtService;

    @GetMapping
    public List<Alert> getAll(@RequestHeader("Authorization") String auth) {
        return alertService.getAllAlerts(jwtService.extractUser(auth));
    }

    @GetMapping("/unread")
    public List<Alert> getUnread(@RequestHeader("Authorization") String auth) {
        return alertService.getUnreadAlerts(jwtService.extractUser(auth));
    }

    // ✅ ADD THIS: Mark alert as read
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            @RequestHeader("Authorization") String auth
    ) {
        var user = jwtService.extractUser(auth);
        var alert = alertService.markAsRead(id, user);
        return ResponseEntity.ok(alert);
    }
}