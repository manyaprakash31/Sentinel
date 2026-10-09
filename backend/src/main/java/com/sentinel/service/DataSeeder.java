package com.sentinel.service;

import com.sentinel.dto.AuthDto;
import com.sentinel.dto.DetectionRuleDto;
import com.sentinel.dto.IncidentDto;
import com.sentinel.dto.SecurityEventDto;
import com.sentinel.entity.*;
import com.sentinel.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final DetectionRuleRepository ruleRepository;
    private final MonitoredServiceRepository serviceRepository;
    private final SecurityEventService eventService;
    private final IncidentService incidentService;
    private final AlertRepository alertRepository;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedUsers();
        seedRules();
        seedServices();
        seedInitialTelemetry();
    }

    private void seedRoles() {
        for (RoleName roleName : RoleName.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = Role.builder()
                        .name(roleName)
                        .description("Role for " + roleName.name())
                        .build();
                roleRepository.save(role);
                log.info("Created Role: {}", roleName);
            }
        }
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            AuthDto.RegisterRequest adminReq = new AuthDto.RegisterRequest();
            adminReq.setUsername("admin");
            adminReq.setEmail("admin@sentinel.sec");
            adminReq.setPassword("Admin@123");
            adminReq.setFullName("Sentinel Administrator");
            adminReq.setRoles(Set.of("ADMIN", "ANALYST", "VIEWER"));
            authService.registerUser(adminReq);
            log.info("Seeded Admin user (admin / Admin@123)");
        }

        if (!userRepository.existsByUsername("analyst")) {
            AuthDto.RegisterRequest analystReq = new AuthDto.RegisterRequest();
            analystReq.setUsername("analyst");
            analystReq.setEmail("analyst@sentinel.sec");
            analystReq.setPassword("Analyst@123");
            analystReq.setFullName("Sarah Jenkins (Lead SOC Analyst)");
            analystReq.setRoles(Set.of("ANALYST", "VIEWER"));
            authService.registerUser(analystReq);
            log.info("Seeded Analyst user (analyst / Analyst@123)");
        }

        if (!userRepository.existsByUsername("viewer")) {
            AuthDto.RegisterRequest viewerReq = new AuthDto.RegisterRequest();
            viewerReq.setUsername("viewer");
            viewerReq.setEmail("viewer@sentinel.sec");
            viewerReq.setPassword("Viewer@123");
            viewerReq.setFullName("Alex Vance (Security Auditor)");
            viewerReq.setRoles(Set.of("VIEWER"));
            authService.registerUser(viewerReq);
            log.info("Seeded Viewer user (viewer / Viewer@123)");
        }
    }

    private void seedRules() {
        if (ruleRepository.count() == 0) {
            ruleRepository.save(DetectionRule.builder()
                    .name("Multiple Failed Logins")
                    .description("Detects brute-force credential stuffing attempts from identical source IPs")
                    .eventType(EventType.LOGIN_FAILURE)
                    .threshold(5)
                    .timeWindowMinutes(5)
                    .severity(Severity.HIGH)
                    .enabled(true)
                    .build());

            ruleRepository.save(DetectionRule.builder()
                    .name("Repeated Access Denied")
                    .description("Detects unauthorized access sweeps or privilege escalation attempts")
                    .eventType(EventType.ACCESS_DENIED)
                    .threshold(4)
                    .timeWindowMinutes(10)
                    .severity(Severity.HIGH)
                    .enabled(true)
                    .build());

            ruleRepository.save(DetectionRule.builder()
                    .name("Service Failure Spike")
                    .description("Identifies consecutive service availability or downstream endpoint failures")
                    .eventType(EventType.SERVICE_FAILURE)
                    .threshold(3)
                    .timeWindowMinutes(5)
                    .severity(Severity.CRITICAL)
                    .enabled(true)
                    .build());

            ruleRepository.save(DetectionRule.builder()
                    .name("Suspicious Activity Anomaly")
                    .description("Flags behavioral application anomalies detected in session tracking")
                    .eventType(EventType.SUSPICIOUS_ACTIVITY)
                    .threshold(2)
                    .timeWindowMinutes(15)
                    .severity(Severity.MEDIUM)
                    .enabled(true)
                    .build());

            log.info("Seeded default defensive detection rules");
        }
    }

    private void seedServices() {
        if (serviceRepository.count() == 0) {
            serviceRepository.save(MonitoredService.builder()
                    .name("Authentication Gateway")
                    .description("Handles OAuth2 and JWT identity lifecycle verification")
                    .endpoint("http://localhost:8080/api/auth/health")
                    .status(ServiceStatus.UP)
                    .responseTime(24L)
                    .lastChecked(LocalDateTime.now())
                    .build());

            serviceRepository.save(MonitoredService.builder()
                    .name("Core API Gateway")
                    .description("Reverse proxy and rate-limiting perimeter gateway")
                    .endpoint("http://localhost:8080/api/gateway/health")
                    .status(ServiceStatus.UP)
                    .responseTime(38L)
                    .lastChecked(LocalDateTime.now())
                    .build());

            serviceRepository.save(MonitoredService.builder()
                    .name("Payment Processing Hub")
                    .description("PCI-DSS compliant transactional billing service")
                    .endpoint("http://localhost:8080/api/payments/health")
                    .status(ServiceStatus.UP)
                    .responseTime(62L)
                    .lastChecked(LocalDateTime.now())
                    .build());

            serviceRepository.save(MonitoredService.builder()
                    .name("Notification Pipeline")
                    .description("Realtime dispatch cluster for email and SMS alerts")
                    .endpoint("http://localhost:8080/api/notify/health")
                    .status(ServiceStatus.UP)
                    .responseTime(41L)
                    .lastChecked(LocalDateTime.now())
                    .build());

            log.info("Seeded monitored infrastructure services");
        }
    }

    private void seedInitialTelemetry() {
        if (alertRepository.count() == 0) {
            log.info("Generating realistic defensive security telemetry dataset...");

            // 1. Routine legitimate events
            eventService.ingestEvent(SecurityEventDto.CreateRequest.builder()
                    .eventType(EventType.LOGIN_SUCCESS)
                    .source("WebPortal")
                    .sourceIp("192.168.1.105")
                    .username("sarah.soc")
                    .severity(Severity.LOW)
                    .message("User authenticated successfully via MFA")
                    .metadata("{\"auth_method\":\"TOTP\",\"device\":\"Firefox/Win11\"}")
                    .build());

            eventService.ingestEvent(SecurityEventDto.CreateRequest.builder()
                    .eventType(EventType.ADMIN_ACTION)
                    .source("ManagementConsole")
                    .sourceIp("10.0.4.12")
                    .username("admin")
                    .severity(Severity.LOW)
                    .message("Administrator reviewed system telemetry rules")
                    .metadata("{\"action\":\"audit_view\",\"module\":\"rules\"}")
                    .build());

            // 2. Simulate 6 Failed Logins from 198.51.100.42 to trigger "Multiple Failed Logins" rule
            String attackerIp = "198.51.100.42";
            for (int i = 1; i <= 6; i++) {
                eventService.ingestEvent(SecurityEventDto.CreateRequest.builder()
                        .eventType(EventType.LOGIN_FAILURE)
                        .source("AuthEndpoint")
                        .sourceIp(attackerIp)
                        .username("admin_root")
                        .severity(Severity.MEDIUM)
                        .message("Failed authentication attempt " + i + " - Invalid credentials")
                        .metadata("{\"reason\":\"BAD_CREDENTIALS\",\"attempt\":" + i + "}")
                        .build());
            }

            // 3. Simulate 5 Access Denied events to trigger "Repeated Access Denied"
            String internalIp = "10.0.12.88";
            for (int i = 1; i <= 5; i++) {
                eventService.ingestEvent(SecurityEventDto.CreateRequest.builder()
                        .eventType(EventType.ACCESS_DENIED)
                        .source("RestrictedVaultAPI")
                        .sourceIp(internalIp)
                        .username("intern_guest")
                        .severity(Severity.HIGH)
                        .message("Access denied to sensitive resource /api/v1/vault/keys [attempt " + i + "]")
                        .metadata("{\"resource\":\"/api/v1/vault/keys\",\"httpStatus\":403}")
                        .build());
            }

            // 4. Create an incident linking alerts
            List<Alert> alerts = alertRepository.findAll();
            if (!alerts.isEmpty()) {
                IncidentDto.CreateRequest incReq = new IncidentDto.CreateRequest();
                incReq.setTitle("Active Credential Stuffing & Access Sweep from " + attackerIp);
                incReq.setDescription("Automated brute-force pattern matched across AuthEndpoint. Subsequent unauthorized traversal observed.");
                incReq.setSeverity(Severity.HIGH);
                incReq.setAlertIds(List.of(alerts.get(0).getId()));

                IncidentDto.Response createdInc = incidentService.createIncident(incReq, "admin");

                // Add investigation note
                IncidentDto.NoteRequest noteReq = new IncidentDto.NoteRequest();
                noteReq.setContent("Perimeter IP " + attackerIp + " has been added to rate-limiting watch list. Traffic dropped at border firewall.");
                incidentService.addNote(createdInc.getId(), noteReq, "analyst");
            }

            log.info("Seed telemetry completed successfully.");
        }
    }
}
