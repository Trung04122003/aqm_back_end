package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.Alert;
import com.commander.aqm.aqm_back_end.model.User;

import java.util.List;

public interface AlertService {
    void evaluateAndTriggerAlerts(User user, Float pm25, Float pm10, Float aqi, Long locationId);
    List<Alert> getUnreadAlerts(User user);
    List<Alert> getAllAlerts(User user);
    Alert markAsRead(Long alertId, User user); // ✅ ADD THIS
}