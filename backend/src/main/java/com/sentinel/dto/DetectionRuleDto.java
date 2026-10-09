package com.sentinel.dto;

import com.sentinel.entity.EventType;
import com.sentinel.entity.Severity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class DetectionRuleDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        private String name;

        private String description;

        @NotNull(message = "Event type is required")
        private EventType eventType;

        @NotNull(message = "Threshold is required")
        @Min(value = 1, message = "Threshold must be at least 1")
        private Integer threshold;

        @NotNull(message = "Time window is required")
        @Min(value = 1, message = "Time window must be at least 1 minute")
        private Integer timeWindowMinutes;

        @NotNull(message = "Severity is required")
        private Severity severity;

        private Boolean enabled;
    }

    @Data
    public static class UpdateRequest {
        private String name;
        private String description;
        private EventType eventType;
        private Integer threshold;
        private Integer timeWindowMinutes;
        private Severity severity;
        private Boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private EventType eventType;
        private Integer threshold;
        private Integer timeWindowMinutes;
        private Severity severity;
        private boolean enabled;
        private LocalDateTime createdAt;
    }
}
