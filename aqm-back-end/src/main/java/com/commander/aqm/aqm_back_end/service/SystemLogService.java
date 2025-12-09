package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.SystemLog;
import com.commander.aqm.aqm_back_end.model.SystemLog.LogLevel;
import com.commander.aqm.aqm_back_end.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemLogService {

    private final SystemLogRepository systemLogRepository;

    public List<SystemLog> getRecentLogs(int limit) {
        return systemLogRepository.findTop200ByOrderByTimestampDesc();
    }

    public void log(LogLevel level, String message, String user) {
        SystemLog log = new SystemLog();
        log.setLevel(level);
        log.setMessage(message);
        log.setUser(user);
        systemLogRepository.save(log);
    }

    // overload Ä‘á»ƒ log nhanh
    public void info(String msg, String user) {
        log(LogLevel.INFO, msg, user);
    }

    public void warn(String msg, String user) {
        log(LogLevel.WARNING, msg, user);
    }

    public void error(String msg, String user) {
        log(LogLevel.ERROR, msg, user);
    }
}