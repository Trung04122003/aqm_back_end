package com.commander.aqm.aqm_back_end.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());

        // Check DB connection
        try {
            dataSource.getConnection().close();
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
            return ResponseEntity.status(503).body(health);
        }

        return ResponseEntity.ok(health);
    }
}
