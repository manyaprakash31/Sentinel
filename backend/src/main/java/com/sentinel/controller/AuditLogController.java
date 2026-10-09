package com.sentinel.controller;

import com.sentinel.dto.ApiResponse;
import com.sentinel.dto.AuditLogDto;
import com.sentinel.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<AuditLogDto>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLogDto> logs = auditLogService.getAllAuditLogs(pageable).map(log ->
                AuditLogDto.builder()
                        .id(log.getId())
                        .actor(log.getActor())
                        .action(log.getAction())
                        .entityType(log.getEntityType())
                        .entityId(log.getEntityId())
                        .timestamp(log.getTimestamp())
                        .details(log.getDetails())
                        .build());

        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}
