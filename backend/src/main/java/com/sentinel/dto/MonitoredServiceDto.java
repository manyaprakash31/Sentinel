package com.sentinel.dto;

import com.sentinel.entity.ServiceStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class MonitoredServiceDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Service name is required")
        private String name;

        private String description;

        @NotBlank(message = "Endpoint is required")
        private String endpoint;
    }

    @Data
    public static class UpdateRequest {
        private String name;
        private String description;
        private String endpoint;
        private ServiceStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private ServiceStatus status;
        private String endpoint;
        private LocalDateTime lastChecked;
        private Long responseTime;
        private LocalDateTime createdAt;
    }
}
