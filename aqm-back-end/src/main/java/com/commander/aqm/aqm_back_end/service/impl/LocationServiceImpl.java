package com.commander.aqm.aqm_back_end.service.impl;

import com.commander.aqm.aqm_back_end.model.Location;
import com.commander.aqm.aqm_back_end.repository.LocationRepository;
import com.commander.aqm.aqm_back_end.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepo;

    @Override
    public List<Location> getAll() {
        return locationRepo.findAll();
    }

    @Override
    public Location getById(Long id) {
        return locationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
    }
}
