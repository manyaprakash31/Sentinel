package com.sentinel.controller;

import com.sentinel.dto.AlertDto;
import com.sentinel.dto.ApiResponse;
import com.sentinel.entity.AlertStatus;
import com.sentinel.entity.Severity;
import com.sentinel.service.AlertService;
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
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AlertDto.Response>>> getAlerts(
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "detectedAt,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<AlertDto.Response> alerts = alertService.getAlerts(severity, status, search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(alerts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlertDto.Response>> getAlertById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(alertService.getAlertById(id)));
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<AlertDto.Response>> acknowledgeAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String actor = userDetails != null ? userDetails.getUsername() : "ANALYST";
        AlertDto.Response alert = alertService.acknowledgeAlert(id, actor);
        return ResponseEntity.ok(ApiResponse.ok("Alert acknowledged", alert));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<AlertDto.Response>> assignAlert(
            @PathVariable Long id,
            @RequestBody AlertDto.AssignRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String actor = userDetails != null ? userDetails.getUsername() : "ANALYST";
        AlertDto.Response alert = alertService.assignAlert(id, request, actor);
        return ResponseEntity.ok(ApiResponse.ok("Alert assigned successfully", alert));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<AlertDto.Response>> resolveAlert(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String actor = userDetails != null ? userDetails.getUsername() : "ANALYST";
        AlertDto.Response alert = alertService.resolveAlert(id, actor);
        return ResponseEntity.ok(ApiResponse.ok("Alert resolved", alert));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<AlertDto.Response>> updateStatus(
            @PathVariable Long id,
            @RequestBody AlertDto.StatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String actor = userDetails != null ? userDetails.getUsername() : "ANALYST";
        AlertDto.Response alert = alertService.updateStatus(id, request, actor);
        return ResponseEntity.ok(ApiResponse.ok("Alert status updated", alert));
    }
}
