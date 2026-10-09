package com.sentinel.service;

import com.sentinel.dto.AlertDto;
import com.sentinel.dto.IncidentDto;
import com.sentinel.entity.*;
import com.sentinel.exception.BadRequestException;
import com.sentinel.exception.ResourceNotFoundException;
import com.sentinel.repository.AlertRepository;
import com.sentinel.repository.IncidentNoteRepository;
import com.sentinel.repository.IncidentRepository;
import com.sentinel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentNoteRepository noteRepository;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final RealtimeNotificationService realtimeNotificationService;
    private final AlertService alertService;

    public Page<IncidentDto.Response> getIncidents(IncidentStatus status, Severity severity, String search, Pageable pageable) {
        Specification<Incident> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (severity != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("severity"), severity));
        }

        if (search != null && !search.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%")
            ));
        }

        return incidentRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public IncidentDto.Response getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));
        return mapToResponse(incident);
    }

    @Transactional
    public IncidentDto.Response createIncident(IncidentDto.CreateRequest request, String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found: " + request.getAssignedToId()));
        }

        Incident incident = Incident.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .status(IncidentStatus.OPEN)
                .createdBy(creator)
                .assignedTo(assignedTo)
                .detectedAt(LocalDateTime.now())
                .build();

        Incident saved = incidentRepository.save(incident);

        // Associate alerts if specified
        if (request.getAlertIds() != null && !request.getAlertIds().isEmpty()) {
            List<Alert> alerts = alertRepository.findAllById(request.getAlertIds());
            for (Alert a : alerts) {
                a.setIncident(saved);
                a.setStatus(AlertStatus.INVESTIGATING);
                alertRepository.save(a);
            }
            saved.setAlerts(alerts);
        }

        // Add initial note
        IncidentNote initialNote = IncidentNote.builder()
                .incident(saved)
                .author(creator.getUsername())
                .content("Incident created. Severity set to " + saved.getSeverity() + ".")
                .build();
        noteRepository.save(initialNote);
        saved.getNotes().add(initialNote);

        auditLogService.log(username, "INCIDENT_CREATED", "Incident", saved.getId(),
                "Created incident #" + saved.getId() + " - " + saved.getTitle());

        IncidentDto.Response resp = mapToResponse(saved);
        realtimeNotificationService.broadcastIncident(resp);
        return resp;
    }

    @Transactional
    public IncidentDto.Response updateIncident(Long id, IncidentDto.UpdateRequest request, String username) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        StringBuilder changes = new StringBuilder();

        if (request.getTitle() != null) {
            incident.setTitle(request.getTitle());
            changes.append("Title updated. ");
        }
        if (request.getDescription() != null) {
            incident.setDescription(request.getDescription());
        }
        if (request.getSeverity() != null && incident.getSeverity() != request.getSeverity()) {
            changes.append("Severity changed from ").append(incident.getSeverity()).append(" to ").append(request.getSeverity()).append(". ");
            incident.setSeverity(request.getSeverity());
        }
        if (request.getStatus() != null && incident.getStatus() != request.getStatus()) {
            changes.append("Status transitioned from ").append(incident.getStatus()).append(" to ").append(request.getStatus()).append(". ");
            incident.setStatus(request.getStatus());
            if (request.getStatus() == IncidentStatus.RESOLVED || request.getStatus() == IncidentStatus.CLOSED) {
                incident.setResolvedAt(LocalDateTime.now());
            }
        }
        if (request.getAssignedToId() != null) {
            User assigned = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getAssignedToId()));
            changes.append("Assigned to ").append(assigned.getUsername()).append(". ");
            incident.setAssignedTo(assigned);
        }

        Incident saved = incidentRepository.save(incident);

        if (changes.length() > 0) {
            IncidentNote auditNote = IncidentNote.builder()
                    .incident(saved)
                    .author(username)
                    .content("System update: " + changes.toString())
                    .build();
            noteRepository.save(auditNote);
            saved.getNotes().add(auditNote);
        }

        auditLogService.log(username, "INCIDENT_UPDATED", "Incident", id, changes.toString());

        IncidentDto.Response resp = mapToResponse(saved);
        realtimeNotificationService.broadcastIncident(resp);
        return resp;
    }

    @Transactional
    public IncidentDto.NoteResponse addNote(Long id, IncidentDto.NoteRequest request, String username) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        IncidentNote note = IncidentNote.builder()
                .incident(incident)
                .author(username)
                .content(request.getContent())
                .build();

        IncidentNote savedNote = noteRepository.save(note);

        auditLogService.log(username, "INCIDENT_NOTE_ADDED", "Incident", id, "Added analyst investigation note");

        IncidentDto.Response resp = mapToResponse(incident);
        realtimeNotificationService.broadcastIncident(resp);

        return IncidentDto.NoteResponse.builder()
                .id(savedNote.getId())
                .author(savedNote.getAuthor())
                .content(savedNote.getContent())
                .createdAt(savedNote.getCreatedAt())
                .build();
    }

    @Transactional
    public IncidentDto.Response resolveIncident(Long id, String username) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(LocalDateTime.now());

        // Resolve associated alerts
        if (incident.getAlerts() != null) {
            for (Alert a : incident.getAlerts()) {
                a.setStatus(AlertStatus.RESOLVED);
                alertRepository.save(a);
            }
        }

        Incident saved = incidentRepository.save(incident);

        IncidentNote resolveNote = IncidentNote.builder()
                .incident(saved)
                .author(username)
                .content("Incident marked as RESOLVED by " + username + ".")
                .build();
        noteRepository.save(resolveNote);

        auditLogService.log(username, "INCIDENT_RESOLVED", "Incident", id, "Resolved incident #" + id);

        IncidentDto.Response resp = mapToResponse(saved);
        realtimeNotificationService.broadcastIncident(resp);
        return resp;
    }

    public IncidentDto.Response mapToResponse(Incident incident) {
        List<IncidentDto.NoteResponse> notes = incident.getNotes() != null
                ? incident.getNotes().stream()
                .map(n -> IncidentDto.NoteResponse.builder()
                        .id(n.getId())
                        .author(n.getAuthor())
                        .content(n.getContent())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList())
                : List.of();

        List<AlertDto.Response> alerts = incident.getAlerts() != null
                ? incident.getAlerts().stream().map(alertService::mapToResponse).collect(Collectors.toList())
                : List.of();

        return IncidentDto.Response.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .assignedToUsername(incident.getAssignedTo() != null ? incident.getAssignedTo().getUsername() : null)
                .assignedToId(incident.getAssignedTo() != null ? incident.getAssignedTo().getId() : null)
                .createdByUsername(incident.getCreatedBy() != null ? incident.getCreatedBy().getUsername() : "Unknown")
                .detectedAt(incident.getDetectedAt())
                .resolvedAt(incident.getResolvedAt())
                .notes(notes)
                .alerts(alerts)
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }
}
