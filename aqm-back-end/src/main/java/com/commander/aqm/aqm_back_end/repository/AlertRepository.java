package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.Alert;
import com.commander.aqm.aqm_back_end.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserAndIsReadFalse(User user);
    List<Alert> findByUser(User user);
}
