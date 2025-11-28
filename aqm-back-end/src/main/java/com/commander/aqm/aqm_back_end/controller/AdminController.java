// aqm-back-end/src/main/java/.../controller/AdminController.java
package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.GenerateReportRequest;
import com.commander.aqm.aqm_back_end.dto.ReportDto;
import com.commander.aqm.aqm_back_end.dto.UserDto;
import com.commander.aqm.aqm_back_end.dto.SensorDto;
import com.commander.aqm.aqm_back_end.model.*;
import com.commander.aqm.aqm_back_end.repository.*;
import com.commander.aqm.aqm_back_end.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin APIs", description = "Admin management endpoints")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // ✅ Require ADMIN role for all endpoints
public class AdminController {

    private final UserRepository userRepo;
    private final SensorRepository sensorRepo;
    private final AlertRepository alertRepo;
    private final AlertThresholdRepository thresholdRepo;
    private final LocationRepository locationRepo;
    private final SensorService sensorService;
    private final PasswordEncoder passwordEncoder;
    private final SystemLogService systemLogService;
    private final ReportRepository reportRepo;
    private final AirQualityDataRepository airRepo; // ✅ ADD THIS LINE

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users/count")
    public ResponseEntity<Map<String, Integer>> getUserCount() {
        int count = (int) userRepo.count();
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userRepo.findAll().stream()
                .map(UserDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        if (userRepo.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .passwordHash(passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "password123"))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .status(request.getStatus() != null ? request.getStatus() : Status.ACTIVE)
                .build();

        userRepo.save(user);
        return ResponseEntity.ok(UserDto.from(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getStatus() != null) user.setStatus(request.getStatus());

        // Only update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        userRepo.save(user);
        return ResponseEntity.ok(UserDto.from(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== SENSOR MANAGEMENT ====================

    @GetMapping("/sensors")
    public ResponseEntity<List<SensorDto>> getAllSensors() {
        List<SensorDto> sensors = sensorRepo.findAll().stream()
                .map(SensorDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(sensors);
    }

    @PostMapping("/sensors")
    public ResponseEntity<?> createSensor(@RequestBody CreateSensorRequest request) {
        Location location = locationRepo.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));

        Sensor sensor = Sensor.builder()
                .serialNumber(request.getSerialNumber())
                .sensorType(request.getSensorType())
                .model(request.getModel())
                .location(location)
                .status(request.getStatus() != null ? request.getStatus() : SensorStatus.ACTIVE)
                .installationDate(request.getInstallationDate())
                .build();

        sensorRepo.save(sensor);
        return ResponseEntity.ok(SensorDto.from(sensor));
    }

    @PutMapping("/sensors/{id}")
    public ResponseEntity<?> updateSensor(@PathVariable Long id, @RequestBody CreateSensorRequest request) {
        Sensor sensor = sensorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Sensor not found"));

        if (request.getSerialNumber() != null) sensor.setSerialNumber(request.getSerialNumber());
        if (request.getSensorType() != null) sensor.setSensorType(request.getSensorType());
        if (request.getModel() != null) sensor.setModel(request.getModel());
        if (request.getStatus() != null) sensor.setStatus(request.getStatus());
        if (request.getInstallationDate() != null) sensor.setInstallationDate(request.getInstallationDate());

        if (request.getLocationId() != null) {
            Location location = locationRepo.findById(request.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Location not found"));
            sensor.setLocation(location);
        }

        sensorRepo.save(sensor);
        return ResponseEntity.ok(SensorDto.from(sensor));
    }

    @DeleteMapping("/sensors/{id}")
    public ResponseEntity<?> deleteSensor(@PathVariable Long id) {
        if (!sensorRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        sensorRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== ALERT MANAGEMENT ====================

    @GetMapping("/alerts/count")
    public ResponseEntity<Map<String, Long>> getAlertCount() {
        long count = alertRepo.count();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> getAllAlerts() {
        return ResponseEntity.ok(alertRepo.findAll());
    }

    @DeleteMapping("/alerts/{id}")
    public ResponseEntity<?> deleteAlert(@PathVariable Long id) {
        if (!alertRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        alertRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== THRESHOLD MANAGEMENT ====================

    @GetMapping("/thresholds")
    public ResponseEntity<List<AlertThreshold>> getAllThresholds() {
        return ResponseEntity.ok(thresholdRepo.findAll());
    }

    @PostMapping("/thresholds")
    public ResponseEntity<?> createThreshold(@RequestBody AlertThreshold threshold) {
        thresholdRepo.save(threshold);
        return ResponseEntity.ok(threshold);
    }

    @PutMapping("/thresholds/{id}")
    public ResponseEntity<?> updateThreshold(@PathVariable Long id, @RequestBody AlertThreshold request) {
        AlertThreshold threshold = thresholdRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Threshold not found"));

        if (request.getPm25Threshold() != null) threshold.setPm25Threshold(request.getPm25Threshold());
        if (request.getPm10Threshold() != null) threshold.setPm10Threshold(request.getPm10Threshold());
        if (request.getAqiThreshold() != null) threshold.setAqiThreshold(request.getAqiThreshold());

        thresholdRepo.save(threshold);
        return ResponseEntity.ok(threshold);
    }

    @DeleteMapping("/thresholds/{id}")
    public ResponseEntity<?> deleteThreshold(@PathVariable Long id) {
        if (!thresholdRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        thresholdRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== STATISTICS ====================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepo.count());
        stats.put("totalSensors", sensorRepo.count());
        stats.put("totalAlerts", alertRepo.count());
        stats.put("activeLocations", locationRepo.count());
        return ResponseEntity.ok(stats);
    }
    // ==================== REPORTS MANAGEMENT ====================
    // ✅ GET all reports - return ReportDto list
    @GetMapping("/reports")
    public ResponseEntity<List<ReportDto>> getAllReports() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🔐 /admin/reports accessed by: " + auth.getName());
        System.out.println("🔐 Authorities: " + auth.getAuthorities());

        List<Report> reports = reportRepo.findAll();
        System.out.println("📊 Found " + reports.size() + " reports");

        // ✅ Convert to DTO
        List<ReportDto> reportDtos = reports.stream()
                .map(ReportDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reportDtos);
    }

    // ✅ Generate report - return ReportDto
    @PostMapping("/reports/generate")
    public ResponseEntity<?> generateReport(@RequestBody GenerateReportRequest request) {
        try {
            // Get current admin user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User admin = userRepo.findByUsername(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            // Get location
            Location location = locationRepo.findById(request.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Location not found"));

            // Parse dates
            LocalDateTime fromDate = LocalDate.parse(request.getFromDate()).atStartOfDay();
            LocalDateTime toDate = LocalDate.parse(request.getToDate()).atTime(23, 59, 59);

            // Get data
            List<AirQualityData> data = airRepo.findByLocationIdAndTimestampUtcBetween(
                    location.getId(), fromDate, toDate
            );

            if (data.isEmpty()) {
                return ResponseEntity.badRequest().body("No data available for selected period");
            }

            // Calculate statistics
            double avgPm25 = data.stream()
                    .filter(d -> d.getPm25() != null)
                    .mapToDouble(AirQualityData::getPm25)
                    .average().orElse(0.0);

            double avgPm10 = data.stream()
                    .filter(d -> d.getPm10() != null)
                    .mapToDouble(AirQualityData::getPm10)
                    .average().orElse(0.0);

            double avgAqi = data.stream()
                    .filter(d -> d.getAqi() != null)
                    .mapToDouble(AirQualityData::getAqi)
                    .average().orElse(0.0);

            OptionalInt maxAqi = data.stream()
                    .filter(d -> d.getAqi() != null)
                    .mapToInt(AirQualityData::getAqi)
                    .max();

            OptionalInt minAqi = data.stream()
                    .filter(d -> d.getAqi() != null)
                    .mapToInt(AirQualityData::getAqi)
                    .min();

            int goodDays = (int) data.stream()
                    .filter(d -> d.getAqi() != null && d.getAqi() <= 50)
                    .count();

            int moderateDays = (int) data.stream()
                    .filter(d -> d.getAqi() != null && d.getAqi() > 50 && d.getAqi() <= 100)
                    .count();

            int unhealthyDays = (int) data.stream()
                    .filter(d -> d.getAqi() != null && d.getAqi() > 100)
                    .count();

            // Create report
            Report report = Report.builder()
                    .user(admin)
                    .location(location)
                    .reportType(Report.ReportType.CUSTOM)
                    .startTimestamp(fromDate)
                    .endTimestamp(toDate)
                    .avgPm25(avgPm25)
                    .avgPm10(avgPm10)
                    .avgAqi(avgAqi)
                    .maxAqi(maxAqi.isPresent() ? maxAqi.getAsInt() : null)
                    .minAqi(minAqi.isPresent() ? minAqi.getAsInt() : null)
                    .goodDays(goodDays)
                    .moderateDays(moderateDays)
                    .unhealthyDays(unhealthyDays)
                    .totalDataPoints(data.size())
                    .build();

            reportRepo.save(report);

            System.out.println("✅ Report generated: ID=" + report.getId());

            // ✅ Return DTO instead of entity
            return ResponseEntity.ok(ReportDto.from(report));

        } catch (Exception e) {
            System.err.println("❌ Generate report error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to generate report: " + e.getMessage());
        }
    }

    // ✅ ADD: Download report as PDF
    @GetMapping("/reports/{id}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id) {
        // Implementation - return PDF bytes
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new byte[0]); // Replace with real PDF generation
    }

    // ==================== SYSTEM LOG ====================
    @GetMapping("/logs")
    public ResponseEntity<List<SystemLog>> getSystemLogs() {
        return ResponseEntity.ok(systemLogService.getRecentLogs(200));
    }
}

// ==================== REQUEST DTOs ====================

@Data
class CreateUserRequest {
    private String username;
    private String email;
    private String fullName;
    private String password;
    private Role role;
    private Status status;
}

@Data
class UpdateUserRequest {
    private String username;
    private String email;
    private String fullName;
    private String password;
    private Role role;
    private Status status;
}

@Data
class CreateSensorRequest {
    private String serialNumber;
    private String sensorType;
    private String model;
    private Long locationId;
    private SensorStatus status;
    private LocalDate installationDate;
}