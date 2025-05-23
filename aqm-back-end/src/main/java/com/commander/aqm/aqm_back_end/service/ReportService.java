package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.dto.ReportDto;
import com.commander.aqm.aqm_back_end.model.User;

import java.time.LocalDateTime;

public interface ReportService {
    ReportDto generate(Long locationId, LocalDateTime from, LocalDateTime to, User user);
}
