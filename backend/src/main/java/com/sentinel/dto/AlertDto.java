package com.sentinel.dto;

import com.sentinel.entity.AlertStatus;
import com.sentinel.entity.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class AlertDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private Severity severity;
        private AlertStatus status;
        private String source;
        private String sourceIp;
        private LocalDateTime detectedAt;
        private String assignedToUsername;
        private Long assignedToId;
        private String ruleName;
        private Long ruleId;
        private Long relatedEventId;
        private Long incidentId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class AssignRequest {
        private Long userId;
        private String username;
    }

    @Data
    public static class StatusUpdateRequest {
        private AlertStatus status;
        private String comment;
    }
}
