package com.sentinel.dto;

import com.sentinel.entity.EventType;
import com.sentinel.entity.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class SecurityEventDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "Event type is required")
        private EventType eventType;

        @NotBlank(message = "Source is required")
        private String source;

        @NotBlank(message = "Source IP is required")
        private String sourceIp;

        private String username;

        @NotNull(message = "Severity is required")
        private Severity severity;

        private String message;
        private String metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private EventType eventType;
        private String source;
        private String sourceIp;
        private String username;
        private LocalDateTime timestamp;
        private Severity severity;
        private String status;
        private String message;
        private String metadata;
        private LocalDateTime createdAt;
    }
}
