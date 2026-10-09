package com.sentinel.service;

import com.sentinel.dto.SecurityEventDto;
import com.sentinel.entity.EventType;
import com.sentinel.entity.SecurityEvent;
import com.sentinel.entity.Severity;
import com.sentinel.exception.ResourceNotFoundException;
import com.sentinel.repository.SecurityEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityEventService {

    private final SecurityEventRepository eventRepository;
    private final DetectionRuleEngineService ruleEngine;
    private final RealtimeNotificationService realtimeNotificationService;

    @Transactional
    public SecurityEventDto.Response ingestEvent(SecurityEventDto.CreateRequest request) {
        SecurityEvent event = SecurityEvent.builder()
                .eventType(request.getEventType())
                .source(request.getSource())
                .sourceIp(request.getSourceIp())
                .username(request.getUsername())
                .severity(request.getSeverity())
                .status("PROCESSED")
                .message(request.getMessage())
                .metadata(request.getMetadata())
                .timestamp(LocalDateTime.now())
                .build();

        SecurityEvent saved = eventRepository.save(event);

        // Map response
        SecurityEventDto.Response response = mapToResponse(saved);

        // Broadcast to WebSocket clients
        realtimeNotificationService.broadcastEvent(response);

        // Evaluate against defensive detection rules
        try {
            ruleEngine.evaluateEvent(saved);
        } catch (Exception e) {
            log.error("Error evaluating detection rules for event ID {}: {}", saved.getId(), e.getMessage());
        }

        return response;
    }

    public Page<SecurityEventDto.Response> getEvents(
            EventType eventType,
            Severity severity,
            String sourceIp,
            String username,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        Specification<SecurityEvent> spec = Specification.where(null);

        if (eventType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("eventType"), eventType));
        }

        if (severity != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("severity"), severity));
        }

        if (sourceIp != null && !sourceIp.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(root.get("sourceIp"), "%" + sourceIp + "%"));
        }

        if (username != null && !username.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
        }

        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), startDate));
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), endDate));
        }

        return eventRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public SecurityEventDto.Response getEventById(Long id) {
        SecurityEvent event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Security event not found with id: " + id));
        return mapToResponse(event);
    }

    public SecurityEventDto.Response mapToResponse(SecurityEvent event) {
        return SecurityEventDto.Response.builder()
                .id(event.getId())
                .eventType(event.getEventType())
                .source(event.getSource())
                .sourceIp(event.getSourceIp())
                .username(event.getUsername())
                .timestamp(event.getTimestamp())
                .severity(event.getSeverity())
                .status(event.getStatus())
                .message(event.getMessage())
                .metadata(event.getMetadata())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
