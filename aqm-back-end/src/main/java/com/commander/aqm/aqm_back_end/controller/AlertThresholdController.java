package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.model.AlertThreshold;
import com.commander.aqm.aqm_back_end.repository.AlertThresholdRepository;
import com.commander.aqm.aqm_back_end.security.JwtService;
import com.commander.aqm.aqm_back_end.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/thresholds")
@RequiredArgsConstructor
public class AlertThresholdController {

    private final AlertThresholdRepository repo;
    private final JwtService jwtService;

    @PostMapping
    public AlertThreshold setThreshold(@RequestHeader("Authorization") String auth, @RequestBody AlertThreshold body) {
        body.setUser(jwtService.extractUser(auth));
        return repo.save(body);
    }

    @GetMapping
    public AlertThreshold get(@RequestHeader("Authorization") String auth) {
        return repo.findByUser(jwtService.extractUser(auth)).orElse(null);
    }
}
