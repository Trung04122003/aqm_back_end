package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findTop200ByOrderByTimestampDesc();
}
