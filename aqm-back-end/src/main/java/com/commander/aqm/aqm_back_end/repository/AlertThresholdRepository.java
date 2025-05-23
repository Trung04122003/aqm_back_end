package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.AlertThreshold;
import com.commander.aqm.aqm_back_end.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertThresholdRepository extends JpaRepository<AlertThreshold, Long> {
    Optional<AlertThreshold> findByUser(User user);
}
