package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.Location;
import java.util.List;

public interface LocationService {
    List<Location> getAll();
    Location getById(Long id);
}
