package com.sentinel.dto;

import com.sentinel.entity.IncidentStatus;
import com.sentinel.entity.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

public class IncidentDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        private String title;

        private String description;

        @NotNull(message = "Severity is required")
        private Severity severity;

        private Long assignedToId;
        private List<Long> alertIds;
    }

    @Data
    public static class UpdateRequest {
        private String title;
        private String description;
        private Severity severity;
        private IncidentStatus status;
        private Long assignedToId;
    }

    @Data
    public static class NoteRequest {
        @NotBlank(message = "Note content is required")
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoteResponse {
        private Long id;
        private String author;
        private String content;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private Severity severity;
        private IncidentStatus status;
        private String assignedToUsername;
        private Long assignedToId;
        private String createdByUsername;
        private LocalDateTime detectedAt;
        private LocalDateTime resolvedAt;
        private List<NoteResponse> notes;
        private List<AlertDto.Response> alerts;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
