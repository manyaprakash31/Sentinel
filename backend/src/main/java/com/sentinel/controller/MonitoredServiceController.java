package com.sentinel.controller;

import com.sentinel.dto.ApiResponse;
import com.sentinel.dto.MonitoredServiceDto;
import com.sentinel.service.MonitoredServiceManagement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class MonitoredServiceController {

    private final MonitoredServiceManagement serviceManagement;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MonitoredServiceDto.Response>>> getAllServices() {
        return ResponseEntity.ok(ApiResponse.ok(serviceManagement.getAllServices()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MonitoredServiceDto.Response>> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(serviceManagement.getServiceById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MonitoredServiceDto.Response>> createService(
            @Valid @RequestBody MonitoredServiceDto.CreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String actor = userDetails != null ? userDetails.getUsername() : "ADMIN";
        MonitoredServiceDto.Response service = serviceManagement.createService(request, actor);
        return ResponseEntity.ok(ApiResponse.ok("Service registered successfully", service));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MonitoredServiceDto.Response>> updateService(
            @PathVariable Long id,
            @RequestBody MonitoredServiceDto.UpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String actor = userDetails != null ? userDetails.getUsername() : "ADMIN";
        MonitoredServiceDto.Response service = serviceManagement.updateService(id, request, actor);
        return ResponseEntity.ok(ApiResponse.ok("Service updated successfully", service));
    }

    @GetMapping("/health-check/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.ok("PONG - Service Healthy", "UP"));
    }
}
