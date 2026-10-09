package com.sentinel.controller;

import com.sentinel.dto.ApiResponse;
import com.sentinel.dto.IncidentDto;
import com.sentinel.entity.IncidentStatus;
import com.sentinel.entity.Severity;
import com.sentinel.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<IncidentDto.Response>>> getIncidents(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "detectedAt,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<IncidentDto.Response> incidents = incidentService.getIncidents(status, severity, search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(incidents));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentDto.Response>> getIncidentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(incidentService.getIncidentById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<IncidentDto.Response>> createIncident(
            @Valid @RequestBody IncidentDto.CreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "ANALYST";
        IncidentDto.Response incident = incidentService.createIncident(request, username);
        return ResponseEntity.ok(ApiResponse.ok("Incident created successfully", incident));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<IncidentDto.Response>> updateIncident(
            @PathVariable Long id,
            @RequestBody IncidentDto.UpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "ANALYST";
        IncidentDto.Response incident = incidentService.updateIncident(id, request, username);
        return ResponseEntity.ok(ApiResponse.ok("Incident updated successfully", incident));
    }

    @PostMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<IncidentDto.NoteResponse>> addNote(
            @PathVariable Long id,
            @Valid @RequestBody IncidentDto.NoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "ANALYST";
        IncidentDto.NoteResponse note = incidentService.addNote(id, request, username);
        return ResponseEntity.ok(ApiResponse.ok("Note added successfully", note));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<IncidentDto.Response>> resolveIncident(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "ANALYST";
        IncidentDto.Response incident = incidentService.resolveIncident(id, username);
        return ResponseEntity.ok(ApiResponse.ok("Incident marked as resolved", incident));
    }
}
