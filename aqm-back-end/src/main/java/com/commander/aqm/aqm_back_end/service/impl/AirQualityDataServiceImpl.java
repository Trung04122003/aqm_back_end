package com.commander.aqm.aqm_back_end.service.impl;

import com.commander.aqm.aqm_back_end.model.AirQualityData;
import com.commander.aqm.aqm_back_end.repository.AirQualityDataRepository;
import com.commander.aqm.aqm_back_end.service.AirQualityDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AirQualityDataServiceImpl implements AirQualityDataService {

    private final AirQualityDataRepository aqRepo;

    @Override
    public List<AirQualityData> getAll() {
        return aqRepo.findAll();
    }

    @Override
    public AirQualityData getById(Long id) {
        return aqRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Air quality record not found"));
    }

    @Override
    public AirQualityData save(AirQualityData data) {
        return aqRepo.save(data);
    }
}
