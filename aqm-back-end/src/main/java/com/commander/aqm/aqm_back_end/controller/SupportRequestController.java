package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.model.SupportRequest;
import com.commander.aqm.aqm_back_end.security.JwtService;
import com.commander.aqm.aqm_back_end.service.SupportRequestService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportRequestController {

    private final SupportRequestService supportService;
    private final JwtService jwtService;

    @PostMapping
    public SupportRequest create(@RequestHeader("Authorization") String auth, @RequestBody SupportRequestDTO body) {
        return supportService.submitRequest(
                jwtService.extractUser(auth),
                body.getSubject(),
                body.getMessage()
        );
    }

    @GetMapping("/my")
    public List<SupportRequest> listMine(@RequestHeader("Authorization") String auth) {
        return supportService.getMyRequests(jwtService.extractUser(auth));
    }

    @Data
    public static class SupportRequestDTO {
        private String subject;
        private String message;
    }
}
