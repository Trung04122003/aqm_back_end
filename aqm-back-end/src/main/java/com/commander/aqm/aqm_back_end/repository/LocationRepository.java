package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
