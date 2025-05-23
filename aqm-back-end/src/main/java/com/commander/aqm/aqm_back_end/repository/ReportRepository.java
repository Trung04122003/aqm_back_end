package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.Report;
import com.commander.aqm.aqm_back_end.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByUser(User user);
}
