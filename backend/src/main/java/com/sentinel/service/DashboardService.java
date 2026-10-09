package com.sentinel.service;

import com.sentinel.dto.DashboardSummaryDto;
import com.sentinel.entity.*;
import com.sentinel.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SecurityEventRepository eventRepository;
    private final AlertRepository alertRepository;
    private final IncidentRepository incidentRepository;
    private final MonitoredServiceRepository serviceRepository;
    private final AlertService alertService;
    private final IncidentService incidentService;
    private final MonitoredServiceManagement serviceManagement;

    public DashboardSummaryDto getSummary() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        long totalEvents = eventRepository.count();
        long eventsToday = eventRepository.countByTimestampAfter(startOfDay);

        long activeAlerts = alertRepository.countByStatusNot(AlertStatus.RESOLVED);
        long criticalAlerts = alertRepository.countBySeverityAndStatusNot(Severity.CRITICAL, AlertStatus.RESOLVED);

        long openIncidents = incidentRepository.countByStatusNot(IncidentStatus.RESOLVED);
        long resolvedIncidents = incidentRepository.countByStatus(IncidentStatus.RESOLVED);

        long servicesDown = serviceRepository.countByStatus(ServiceStatus.DOWN);
        long servicesTotal = serviceRepository.count();

        // Events by Type
        Map<String, Long> eventsByType = new LinkedHashMap<>();
        for (Object[] row : eventRepository.countGroupedByEventType()) {
            eventsByType.put(row[0].toString(), (Long) row[1]);
        }

        // Alerts by Severity
        Map<String, Long> alertsBySeverity = new LinkedHashMap<>();
        for (Object[] row : alertRepository.countGroupedBySeverity()) {
            alertsBySeverity.put(row[0].toString(), (Long) row[1]);
        }

        // Alerts by Status
        Map<String, Long> alertsByStatus = new LinkedHashMap<>();
        for (Object[] row : alertRepository.countGroupedByStatus()) {
            alertsByStatus.put(row[0].toString(), (Long) row[1]);
        }

        // Incidents by Status
        Map<String, Long> incidentsByStatus = new LinkedHashMap<>();
        for (Object[] row : incidentRepository.countGroupedByStatus()) {
            incidentsByStatus.put(row[0].toString(), (Long) row[1]);
        }

        // Incidents by Severity
        Map<String, Long> incidentsBySeverity = new LinkedHashMap<>();
        for (Object[] row : incidentRepository.countGroupedBySeverity()) {
            incidentsBySeverity.put(row[0].toString(), (Long) row[1]);
        }

        // Recent items
        var recentAlerts = alertRepository.findTop5ByOrderByDetectedAtDesc().stream()
                .map(alertService::mapToResponse)
                .collect(Collectors.toList());

        var recentIncidents = incidentRepository.findTop5ByOrderByDetectedAtDesc().stream()
                .map(incidentService::mapToResponse)
                .collect(Collectors.toList());

        var services = serviceManagement.getAllServices();

        return DashboardSummaryDto.builder()
                .totalEvents(totalEvents)
                .eventsToday(eventsToday)
                .activeAlerts(activeAlerts)
                .criticalAlerts(criticalAlerts)
                .openIncidents(openIncidents)
                .resolvedIncidents(resolvedIncidents)
                .servicesDown(servicesDown)
                .servicesTotal(servicesTotal)
                .eventsByType(eventsByType)
                .alertsBySeverity(alertsBySeverity)
                .alertsByStatus(alertsByStatus)
                .incidentsByStatus(incidentsByStatus)
                .incidentsBySeverity(incidentsBySeverity)
                .recentAlerts(recentAlerts)
                .recentIncidents(recentIncidents)
                .services(services)
                .build();
    }
}
