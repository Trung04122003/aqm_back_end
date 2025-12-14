// src/main/java/com/commander/aqm/aqm_back_end/config/AsyncConfiguration.java
package com.commander.aqm.aqm_back_end.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ⚙️ Enable Async Processing and Scheduled Tasks
 * Required for:
 * - Alert monitoring (async)
 * - Auto-fetch AQI/Weather (scheduled)
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfiguration {
    // Spring will automatically handle async method execution
}