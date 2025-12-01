// src/main/java/com/commander/aqm/aqm_back_end/config/AsyncConfig.java
package com.commander.aqm.aqm_back_end.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // This enables @Async annotation in EmailService
}