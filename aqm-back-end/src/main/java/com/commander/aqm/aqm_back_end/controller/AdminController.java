// aqm-back-end/src/main/java/.../controller/AdminController.java
package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.*;
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
    private final PdfReportService pdfReportService;
    private final CsvReportService csvReportService;
    private final ExcelReportService excelReportService;
    private final HtmlReportService htmlReportService;
    private final SupportRequestRepository supportRepo;

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

    // ==================== ALERT THRESHOLD MANAGEMENT ====================

    @PostMapping("/alert-thresholds")
    public ResponseEntity<?> createAlertThreshold(@RequestBody CreateThresholdRequest request) {
        try {
            // Validate user exists
            User user = userRepo.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user already has threshold
            Optional<AlertThreshold> existing = thresholdRepo.findByUser(user);
            if (existing.isPresent()) {
                return ResponseEntity.badRequest()
                        .body("User already has a threshold. Use PUT to update.");
            }

            // Create new threshold
            AlertThreshold threshold = AlertThreshold.builder()
                    .user(user)
                    .pm25Threshold(request.getPm25Threshold())
                    .pm10Threshold(request.getPm10Threshold())
                    .aqiThreshold(request.getAqiThreshold())
                    .build();

            thresholdRepo.save(threshold);
            return ResponseEntity.ok(threshold);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create threshold: " + e.getMessage());
        }
    }

    @GetMapping("/alert-thresholds")
    public ResponseEntity<List<AlertThreshold>> getAllAlertThresholds() {
        return ResponseEntity.ok(thresholdRepo.findAll());
    }

    @GetMapping("/alert-thresholds/{id}")
    public ResponseEntity<?> getAlertThreshold(@PathVariable Long id) {
        return thresholdRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/alert-thresholds/{id}")
    public ResponseEntity<?> updateAlertThreshold(
            @PathVariable Long id,
            @RequestBody UpdateThresholdRequest request
    ) {
        try {
            AlertThreshold threshold = thresholdRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Threshold not found"));

            if (request.getPm25Threshold() != null)
                threshold.setPm25Threshold(request.getPm25Threshold());
            if (request.getPm10Threshold() != null)
                threshold.setPm10Threshold(request.getPm10Threshold());
            if (request.getAqiThreshold() != null)
                threshold.setAqiThreshold(request.getAqiThreshold());

            thresholdRepo.save(threshold);
            return ResponseEntity.ok(threshold);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update: " + e.getMessage());
        }
    }

    @DeleteMapping("/alert-thresholds/{id}")
    public ResponseEntity<?> deleteAlertThreshold(@PathVariable Long id) {
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

    // ✅ 1. CSV Export
    @GetMapping("/reports/{id}/export/csv")
    public ResponseEntity<byte[]> exportReportCsv(@PathVariable Long id) {
        try {
            Report report = reportRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));

            byte[] csvBytes = csvReportService.generateReportCsv(report);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=report_" + id + ".csv")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(csvBytes);

        } catch (Exception e) {
            System.err.println("❌ CSV Export error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ 2. Excel Export
    @GetMapping("/reports/{id}/export/excel")
    public ResponseEntity<byte[]> exportReportExcel(@PathVariable Long id) {
        try {
            Report report = reportRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));

            byte[] excelBytes = excelReportService.generateReportExcel(report);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=report_" + id + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelBytes);

        } catch (Exception e) {
            System.err.println("❌ Excel Export error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ 3. HTML Export
    @GetMapping("/reports/{id}/export/html")
    public ResponseEntity<byte[]> exportReportHtml(@PathVariable Long id) {
        try {
            Report report = reportRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));

            byte[] htmlBytes = htmlReportService.generateReportHtml(report);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=report_" + id + ".html")
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlBytes);

        } catch (Exception e) {
            System.err.println("❌ HTML Export error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ 4. JSON Export (Direct DTO)
    @GetMapping("/reports/{id}/export/json")
    public ResponseEntity<ReportDto> exportReportJson(@PathVariable Long id) {
        try {
            Report report = reportRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=report_" + id + ".json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ReportDto.from(report));

        } catch (Exception e) {
            System.err.println("❌ JSON Export error: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ 5. PDF Export (existing - keep for reference)
    @GetMapping("/reports/{id}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id) {
        try {
            Report report = reportRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Report not found"));

            byte[] pdfBytes = pdfReportService.generateReportPdf(report);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=report_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            System.err.println("❌ PDF Download error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ FIXED: Delete report with proper error handling
    @DeleteMapping("/reports/{id}")
    @PreAuthorize("hasRole('ADMIN')") // ✅ Ensure admin only
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        try {
            // Check if report exists
            if (!reportRepo.existsById(id)) {
                return ResponseEntity.status(404).body("Report not found");
            }

            // Delete the report
            reportRepo.deleteById(id);

            System.out.println("✅ Report deleted: ID=" + id);
            return ResponseEntity.ok().body(Map.of("message", "Report deleted successfully", "id", id));

        } catch (Exception e) {
            System.err.println("❌ Delete error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to delete report: " + e.getMessage());
        }
    }

    // ==================== SYSTEM LOG ====================
    @GetMapping("/logs")
    public ResponseEntity<List<SystemLog>> getSystemLogs() {
        return ResponseEntity.ok(systemLogService.getRecentLogs(200));
    }

    // ==================== SUPPORT TICKET MANAGEMENT ====================
    /**
     * Get all support tickets
     */
    @GetMapping("/support")
    public ResponseEntity<List<SupportTicketDto>> getAllSupportTickets() {
        List<SupportRequest> tickets = supportRepo.findAllByOrderBySubmittedAtDesc();
        List<SupportTicketDto> dtos = tickets.stream()
                .map(SupportTicketDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get support ticket statistics
     */
    @GetMapping("/support/count")
    public ResponseEntity<Map<String, Long>> getSupportCount() {
        long total = supportRepo.count();
        long pending = supportRepo.countByStatus(RequestStatus.PENDING);
        long inProgress = supportRepo.countByStatus(RequestStatus.IN_PROGRESS);
        long resolved = supportRepo.countByStatus(RequestStatus.RESOLVED);

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("inProgress", inProgress);
        stats.put("resolved", resolved);

        return ResponseEntity.ok(stats);
    }

    /**
     * Update support ticket (status and/or admin reply)
     */
    @PutMapping("/support/{id}")
    public ResponseEntity<?> updateSupportTicket(
            @PathVariable Long id,
            @RequestBody UpdateSupportRequest request
    ) {
        try {
            SupportRequest ticket = supportRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            if (request.getStatus() != null) {
                ticket.setStatus(request.getStatus());
            }
            if (request.getAdminReply() != null && !request.getAdminReply().isEmpty()) {
                ticket.setAdminReply(request.getAdminReply());
            }

            supportRepo.save(ticket);
            System.out.println("✅ Support ticket updated: ID=" + id);

            return ResponseEntity.ok(SupportTicketDto.from(ticket));

        } catch (Exception e) {
            System.err.println("❌ Update ticket error: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Failed to update ticket: " + e.getMessage());
        }
    }

    /**
     * Delete support ticket
     */
    @DeleteMapping("/support/{id}")
    public ResponseEntity<?> deleteSupportTicket(@PathVariable Long id) {
        try {
            if (!supportRepo.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            supportRepo.deleteById(id);
            System.out.println("✅ Support ticket deleted: ID=" + id);

            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "Ticket deleted successfully",
                            "id", id
                    ));

        } catch (Exception e) {
            System.err.println("❌ Delete ticket error: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body("Failed to delete ticket: " + e.getMessage());
        }
    }

    // ==================== ALERT THRESHOLD MANAGEMENT ====================

    // Thêm vào AdminController.java

    @PostMapping("/locations")
    public ResponseEntity<?> createLocation(@RequestBody Location location) {
        try {
            Location saved = locationRepo.save(location);
            System.out.println("🎁 Location created!");
            return ResponseEntity.ok(LocationDto.from(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create location");
        }
    }

    @PutMapping("/locations/{id}")
    public ResponseEntity<?> updateLocation(@PathVariable Long id, @RequestBody Location request) {
        try {
            Location location = locationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Location not found"));

            location.setName(request.getName());
            location.setLatitude(request.getLatitude());
            location.setLongitude(request.getLongitude());
            if (request.getTimezone() != null) {
                location.setTimezone(request.getTimezone());
            }

            locationRepo.save(location);
            return ResponseEntity.ok(LocationDto.from(location));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update location");
        }
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<?> deleteLocation(@PathVariable Long id) {
        try {
            if (!locationRepo.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            locationRepo.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete location");
        }
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

@Data
class CreateThresholdRequest {
    private Long userId;
    private Float pm25Threshold;
    private Float pm10Threshold;
    private Float aqiThreshold;
}

@Data
class UpdateThresholdRequest {
    private Float pm25Threshold;
    private Float pm10Threshold;
    private Float aqiThreshold;
}

@Data
class UpdateSupportRequest {
    private RequestStatus status;
    private String adminReply;
}