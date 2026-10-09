package com.sentinel.service;

import com.sentinel.dto.MonitoredServiceDto;
import com.sentinel.entity.MonitoredService;
import com.sentinel.entity.ServiceStatus;
import com.sentinel.exception.BadRequestException;
import com.sentinel.exception.ResourceNotFoundException;
import com.sentinel.repository.MonitoredServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoredServiceManagement {

    private final MonitoredServiceRepository serviceRepository;
    private final AuditLogService auditLogService;
    private final RealtimeNotificationService realtimeNotificationService;
    private final Random random = new Random();

    public List<MonitoredServiceDto.Response> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MonitoredServiceDto.Response getServiceById(Long id) {
        MonitoredService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Monitored service not found with id: " + id));
        return mapToResponse(service);
    }

    @Transactional
    public MonitoredServiceDto.Response createService(MonitoredServiceDto.CreateRequest request, String actor) {
        if (serviceRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("Service with name '" + request.getName() + "' already exists");
        }

        MonitoredService service = MonitoredService.builder()
                .name(request.getName())
                .description(request.getDescription())
                .endpoint(request.getEndpoint())
                .status(ServiceStatus.UP)
                .responseTime(45L)
                .lastChecked(LocalDateTime.now())
                .build();

        MonitoredService saved = serviceRepository.save(service);
        auditLogService.log(actor, "SERVICE_ADDED", "MonitoredService", saved.getId(), "Added service: " + saved.getName());

        MonitoredServiceDto.Response resp = mapToResponse(saved);
        realtimeNotificationService.broadcastServiceStatus(resp);
        return resp;
    }

    @Transactional
    public MonitoredServiceDto.Response updateService(Long id, MonitoredServiceDto.UpdateRequest request, String actor) {
        MonitoredService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));

        if (request.getName() != null) service.setName(request.getName());
        if (request.getDescription() != null) service.setDescription(request.getDescription());
        if (request.getEndpoint() != null) service.setEndpoint(request.getEndpoint());
        if (request.getStatus() != null) service.setStatus(request.getStatus());

        MonitoredService saved = serviceRepository.save(service);
        auditLogService.log(actor, "SERVICE_UPDATED", "MonitoredService", id, "Updated service: " + saved.getName());

        MonitoredServiceDto.Response resp = mapToResponse(saved);
        realtimeNotificationService.broadcastServiceStatus(resp);
        return resp;
    }

    /**
     * Periodic safe synthetic heartbeat check on configured monitored services.
     * Generates realistic latency telemetry and updates health.
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    @Transactional
    public void performHealthChecks() {
        List<MonitoredService> services = serviceRepository.findAll();
        for (MonitoredService s : services) {
            long latency = 20 + random.nextInt(90);
            s.setLastChecked(LocalDateTime.now());
            s.setResponseTime(latency);

            // Default status maintains UP unless explicitly changed or degraded
            if (s.getStatus() != ServiceStatus.DOWN) {
                if (latency > 95) {
                    s.setStatus(ServiceStatus.DEGRADED);
                } else {
                    s.setStatus(ServiceStatus.UP);
                }
            }
            serviceRepository.save(s);
            realtimeNotificationService.broadcastServiceStatus(mapToResponse(s));
        }
    }

    public MonitoredServiceDto.Response mapToResponse(MonitoredService service) {
        return MonitoredServiceDto.Response.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .status(service.getStatus())
                .endpoint(service.getEndpoint())
                .lastChecked(service.getLastChecked())
                .responseTime(service.getResponseTime())
                .createdAt(service.getCreatedAt())
                .build();
    }
}
