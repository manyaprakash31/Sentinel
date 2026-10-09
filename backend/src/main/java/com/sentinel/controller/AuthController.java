package com.sentinel.controller;

import com.sentinel.dto.ApiResponse;
import com.sentinel.dto.AuthDto;
import com.sentinel.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.JwtResponse>> authenticateUser(@Valid @RequestBody AuthDto.LoginRequest loginRequest) {
        AuthDto.JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", jwtResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> registerUser(@Valid @RequestBody AuthDto.RegisterRequest signUpRequest) {
        authService.registerUser(signUpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Account created successfully. You can now login with your credentials.", null));
    }

    @PostMapping("/bootstrap-admin")
    public ResponseEntity<ApiResponse<Object>> bootstrapAdmin(@Valid @RequestBody AuthDto.BootstrapAdminRequest request) {
        authService.bootstrapAdmin(request);
        return ResponseEntity.ok(ApiResponse.ok("Administrator account successfully bootstrapped.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthDto.UserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }
        AuthDto.UserResponse user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuthDto.UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok(authService.getAllUsers()));
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuthDto.UserResponse>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody AuthDto.RoleUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String actor = userDetails != null ? userDetails.getUsername() : "ADMIN";
        AuthDto.UserResponse updated = authService.updateUserRole(id, request.getRole(), actor);
        return ResponseEntity.ok(ApiResponse.ok("User role updated successfully", updated));
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuthDto.UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody AuthDto.UserStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String actor = userDetails != null ? userDetails.getUsername() : "ADMIN";
        AuthDto.UserResponse updated = authService.updateUserStatus(id, request.isEnabled(), actor);
        return ResponseEntity.ok(ApiResponse.ok("User status updated successfully", updated));
    }
}
