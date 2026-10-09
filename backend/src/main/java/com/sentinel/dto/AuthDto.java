package com.sentinel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

public class AuthDto {

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Username or email is required")
        private String username;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username can only contain alphanumeric characters, underscores, hyphens and dots")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters long")
        @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._-]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
        )
        private String password;

        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        private String fullName;

        // Note: roles field is ignored/not accepted for public registration to prevent privilege escalation
    }

    @Data
    public static class BootstrapAdminRequest {
        @NotBlank(message = "Bootstrap secret token is required")
        private String bootstrapSecret;

        @NotBlank(message = "Admin username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @NotBlank(message = "Admin email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Admin password is required")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters long")
        private String password;

        @NotBlank(message = "Admin full name is required")
        private String fullName;
    }

    @Data
    public static class RoleUpdateRequest {
        @NotBlank(message = "Target role is required")
        private String role;
    }

    @Data
    public static class UserStatusUpdateRequest {
        private boolean enabled;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JwtResponse {
        private String token;
        @Builder.Default
        private String type = "Bearer";
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private Set<String> roles;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private boolean enabled;
        private Set<String> roles;
        private LocalDateTime createdAt;
    }
}
