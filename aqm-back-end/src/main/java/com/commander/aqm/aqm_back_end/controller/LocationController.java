package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.LocationDto;
import com.commander.aqm.aqm_back_end.model.Location;
import com.commander.aqm.aqm_back_end.service.LocationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "Location APIs", description = "Manage locations and info")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public List<LocationDto> getAll() {
        return locationService.getAll().stream()
                .map(LocationDto::from)
                .toList();
    }
}
