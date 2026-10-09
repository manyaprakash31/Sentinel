package com.sentinel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    private long totalEvents;
    private long eventsToday;
    private long activeAlerts;
    private long criticalAlerts;
    private long openIncidents;
    private long resolvedIncidents;
    private long servicesDown;
    private long servicesTotal;

    // Charts / Aggregations
    private Map<String, Long> eventsByType;
    private Map<String, Long> alertsBySeverity;
    private Map<String, Long> alertsByStatus;
    private Map<String, Long> incidentsByStatus;
    private Map<String, Long> incidentsBySeverity;

    // Recent items
    private List<AlertDto.Response> recentAlerts;
    private List<IncidentDto.Response> recentIncidents;
    private List<MonitoredServiceDto.Response> services;
}
