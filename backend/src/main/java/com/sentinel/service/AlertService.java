package com.sentinel.service;

import com.sentinel.dto.AlertDto;
import com.sentinel.dto.SecurityEventDto;
import com.sentinel.entity.*;
import com.sentinel.exception.ResourceNotFoundException;
import com.sentinel.repository.AlertRepository;
import com.sentinel.repository.DetectionRuleRepository;
import com.sentinel.repository.SecurityEventRepository;
import com.sentinel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final RealtimeNotificationService realtimeNotificationService;

    public Page<AlertDto.Response> getAlerts(Severity severity, AlertStatus status, String search, Pageable pageable) {
        Specification<Alert> spec = Specification.where(null);

        if (severity != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("severity"), severity));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("source")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("sourceIp")), "%" + search.toLowerCase() + "%")
            ));
        }

        return alertRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public AlertDto.Response getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
        return mapToResponse(alert);
    }

    @Transactional
    public Alert createAlertFromRule(DetectionRule rule, SecurityEvent triggerEvent, long currentCount) {
        Alert alert = Alert.builder()
                .title("Alert: " + rule.getName())
                .description(String.format("Triggered rule '%s'. Detected %d '%s' events within %d minutes from source '%s' (IP: %s).",
                        rule.getName(), currentCount, rule.getEventType(), rule.getTimeWindowMinutes(),
                        triggerEvent.getSource(), triggerEvent.getSourceIp()))
                .severity(rule.getSeverity())
                .status(AlertStatus.NEW)
                .source(triggerEvent.getSource())
                .sourceIp(triggerEvent.getSourceIp())
                .detectedAt(LocalDateTime.now())
                .rule(rule)
                .relatedEvent(triggerEvent)
                .build();

        Alert saved = alertRepository.save(alert);
        log.info("Created alert ID: {} from rule: {}", saved.getId(), rule.getName());

        AlertDto.Response resp = mapToResponse(saved);
        realtimeNotificationService.broadcastAlert(resp);

        auditLogService.log("RULE_ENGINE", "ALERT_GENERATED", "Alert", saved.getId(),
                "Generated alert for rule: " + rule.getName());

        return saved;
    }

    @Transactional
    public AlertDto.Response acknowledgeAlert(Long id, String actor) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        Alert saved = alertRepository.save(alert);

        auditLogService.log(actor, "ALERT_ACKNOWLEDGED", "Alert", id, "Acknowledged alert #" + id);

        AlertDto.Response resp = mapToResponse(saved);
        realtimeNotificationService.broadcastAlert(resp);
        return resp;
    }

    @Transactional
    public AlertDto.Response assignAlert(Long id, AlertDto.AssignRequest request, String actor) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        } else if (request.getUsername() != null) {
            user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + request.getUsername()));
        }

        alert.setAssignedTo(user);
        if (alert.getStatus() == AlertStatus.NEW) {
            alert.setStatus(AlertStatus.INVESTIGATING);
        }
        Alert saved = alertRepository.save(alert);

        auditLogService.log(actor, "ALERT_ASSIGNED", "Alert", id,
                "Assigned alert to " + (user != null ? user.getUsername() : "Unassigned"));

        AlertDto.Response resp = mapToResponse(saved);
        realtimeNotificationService.broadcastAlert(resp);
        return resp;
    }

    @Transactional
    public AlertDto.Response resolveAlert(Long id, String actor) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));

        alert.setStatus(AlertStatus.RESOLVED);
        Alert saved = alertRepository.save(alert);

        auditLogService.log(actor, "ALERT_RESOLVED", "Alert", id, "Resolved alert #" + id);

        AlertDto.Response resp = mapToResponse(saved);
        realtimeNotificationService.broadcastAlert(resp);
        return resp;
    }

    @Transactional
    public AlertDto.Response updateStatus(Long id, AlertDto.StatusUpdateRequest request, String actor) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));

        alert.setStatus(request.getStatus());
        Alert saved = alertRepository.save(alert);

        auditLogService.log(actor, "ALERT_STATUS_UPDATED", "Alert", id,
                "Updated status to " + request.getStatus() + (request.getComment() != null ? " - " + request.getComment() : ""));

        AlertDto.Response resp = mapToResponse(saved);
        realtimeNotificationService.broadcastAlert(resp);
        return resp;
    }

    public AlertDto.Response mapToResponse(Alert alert) {
        return AlertDto.Response.builder()
                .id(alert.getId())
                .title(alert.getTitle())
                .description(alert.getDescription())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .source(alert.getSource())
                .sourceIp(alert.getSourceIp())
                .detectedAt(alert.getDetectedAt())
                .assignedToUsername(alert.getAssignedTo() != null ? alert.getAssignedTo().getUsername() : null)
                .assignedToId(alert.getAssignedTo() != null ? alert.getAssignedTo().getId() : null)
                .ruleName(alert.getRule() != null ? alert.getRule().getName() : null)
                .ruleId(alert.getRule() != null ? alert.getRule().getId() : null)
                .relatedEventId(alert.getRelatedEvent() != null ? alert.getRelatedEvent().getId() : null)
                .incidentId(alert.getIncident() != null ? alert.getIncident().getId() : null)
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .build();
    }
}
