package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.model.Location;
import com.commander.aqm.aqm_back_end.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public List<Location> getAll() {
        return locationService.getAll(); // Consider DTO if exposing to public
    }
}
