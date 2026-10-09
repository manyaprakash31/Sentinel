package com.sentinel.controller;

import com.sentinel.dto.ApiResponse;
import com.sentinel.dto.SecurityEventDto;
import com.sentinel.entity.EventType;
import com.sentinel.entity.Severity;
import com.sentinel.service.SecurityEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class SecurityEventController {

    private final SecurityEventService eventService;

    @PostMapping
    public ResponseEntity<ApiResponse<SecurityEventDto.Response>> createEvent(@Valid @RequestBody SecurityEventDto.CreateRequest request) {
        SecurityEventDto.Response response = eventService.ingestEvent(request);
        return ResponseEntity.ok(ApiResponse.ok("Event processed successfully", response));
    }

    @PostMapping("/ingest")
    public ResponseEntity<ApiResponse<SecurityEventDto.Response>> ingestTelemetry(@Valid @RequestBody SecurityEventDto.CreateRequest request) {
        SecurityEventDto.Response response = eventService.ingestEvent(request);
        return ResponseEntity.ok(ApiResponse.ok("Telemetry ingested", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SecurityEventDto.Response>>> getEvents(
            @RequestParam(required = false) EventType eventType,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) String sourceIp,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<SecurityEventDto.Response> events = eventService.getEvents(eventType, severity, sourceIp, username, startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.ok(events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SecurityEventDto.Response>> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.getEventById(id)));
    }
}
