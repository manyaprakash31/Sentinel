package com.sentinel.service;

import com.sentinel.dto.AuthDto;
import com.sentinel.entity.Role;
import com.sentinel.entity.RoleName;
import com.sentinel.entity.User;
import com.sentinel.exception.BadRequestException;
import com.sentinel.exception.ResourceNotFoundException;
import com.sentinel.repository.RoleRepository;
import com.sentinel.repository.UserRepository;
import com.sentinel.security.JwtUtils;
import com.sentinel.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final AuditLogService auditLogService;
    private final LoginRateLimiterService rateLimiterService;

    @Value("${sentinel.bootstrap.secret:SentinelBootstrap2026!SecureKey}")
    private String configuredBootstrapSecret;

    @Value("${sentinel.bootstrap.enabled:true}")
    private boolean bootstrapEnabled;

    public AuthDto.JwtResponse authenticateUser(AuthDto.LoginRequest loginRequest) {
        String identifier = loginRequest.getUsername().trim();

        // 1. Check rate limiter for brute-force lockouts
        if (rateLimiterService.isBlocked(identifier)) {
            auditLogService.log(
                    identifier,
                    "LOGIN_BLOCKED_RATE_LIMIT",
                    "User",
                    null,
                    "Login attempt temporarily locked out due to consecutive failed attempts"
            );
            throw new BadRequestException("Account or IP temporarily locked out due to multiple failed login attempts. Please try again after 15 minutes.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Clear any previous failed attempts
            rateLimiterService.recordSuccess(identifier);

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            auditLogService.log(
                    userDetails.getUsername(),
                    "USER_LOGIN_SUCCESS",
                    "User",
                    userDetails.getId(),
                    "User authenticated successfully via credentials"
            );

            return AuthDto.JwtResponse.builder()
                    .token(jwt)
                    .id(userDetails.getId())
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .fullName(userDetails.getFullName())
                    .roles(roles)
                    .build();

        } catch (DisabledException ex) {
            auditLogService.log(
                    identifier,
                    "USER_LOGIN_DISABLED",
                    "User",
                    null,
                    "Disabled account attempted to authenticate"
            );
            throw new BadRequestException("User account is disabled. Please contact your system administrator.");
        } catch (BadCredentialsException ex) {
            rateLimiterService.recordFailedAttempt(identifier);
            auditLogService.log(
                    identifier,
                    "USER_LOGIN_FAILED",
                    "User",
                    null,
                    "Failed authentication attempt: Invalid credentials"
            );
            throw new BadCredentialsException("Invalid username/email or password.");
        } catch (Exception ex) {
            rateLimiterService.recordFailedAttempt(identifier);
            auditLogService.log(
                    identifier,
                    "USER_LOGIN_FAILED",
                    "User",
                    null,
                    "Failed authentication attempt: " + ex.getMessage()
            );
            throw ex;
        }
    }

    /**
     * Genuine public signup:
     * - Strictly assigns ROLE_VIEWER.
     * - Ignores or rejects any client-supplied role escalation.
     * - Hashes password using BCrypt.
     */
    @Transactional
    public User registerUser(AuthDto.RegisterRequest signUpRequest) {
        String username = signUpRequest.getUsername().trim().toLowerCase();
        String email = signUpRequest.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Error: Email is already registered!");
        }

        Role viewerRole = roleRepository.findByName(RoleName.VIEWER)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.VIEWER)
                        .description("Role for VIEWER")
                        .build()));

        User user = User.builder()
                .username(username)
                .email(email)
                .password(encoder.encode(signUpRequest.getPassword()))
                .fullName(signUpRequest.getFullName().trim())
                .enabled(true)
                .roles(new HashSet<>(Set.of(viewerRole)))
                .build();

        User savedUser = userRepository.save(user);

        auditLogService.log(
                "PUBLIC_REGISTRATION",
                "USER_REGISTERED",
                "User",
                savedUser.getId(),
                "Registered new account: " + savedUser.getUsername() + " with default VIEWER role"
        );

        return savedUser;
    }

    /**
     * Secure Initial Administrator Bootstrap:
     * - Enabled only when explicitly active and requires a secret token.
     * - Automatically promotes or creates the initial ADMIN.
     */
    @Transactional
    public User bootstrapAdmin(AuthDto.BootstrapAdminRequest request) {
        if (!bootstrapEnabled) {
            throw new BadRequestException("Administrator bootstrap mechanism is disabled on this server.");
        }

        if (!configuredBootstrapSecret.equals(request.getBootstrapSecret())) {
            auditLogService.log(
                    "SYSTEM",
                    "BOOTSTRAP_ADMIN_UNAUTHORIZED",
                    "Security",
                    null,
                    "Unauthorized bootstrap attempt with invalid secret token"
            );
            throw new BadRequestException("Invalid bootstrap authorization secret token.");
        }

        String username = request.getUsername().trim().toLowerCase();
        String email = request.getEmail().trim().toLowerCase();

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.ADMIN)
                        .description("Role for ADMIN")
                        .build()));
        Role analystRole = roleRepository.findByName(RoleName.ANALYST)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.ANALYST)
                        .description("Role for ANALYST")
                        .build()));
        Role viewerRole = roleRepository.findByName(RoleName.VIEWER)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(RoleName.VIEWER)
                        .description("Role for VIEWER")
                        .build()));

        User adminUser = userRepository.findByUsername(username)
                .orElseGet(() -> User.builder()
                        .username(username)
                        .email(email)
                        .build());

        adminUser.setEmail(email);
        adminUser.setFullName(request.getFullName().trim());
        adminUser.setPassword(encoder.encode(request.getPassword()));
        adminUser.setEnabled(true);
        adminUser.setRoles(new HashSet<>(Set.of(adminRole, analystRole, viewerRole)));

        User saved = userRepository.save(adminUser);

        auditLogService.log(
                "BOOTSTRAP_SERVICE",
                "INITIAL_ADMIN_PROVISIONED",
                "User",
                saved.getId(),
                "Initial administrator successfully provisioned: @" + saved.getUsername()
        );

        log.info("Initial Administrator provisioned securely for username: {}", saved.getUsername());
        return saved;
    }

    public List<AuthDto.UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public AuthDto.UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return mapToUserResponse(user);
    }

    @Transactional
    public AuthDto.UserResponse updateUserRole(Long userId, String targetRoleStr, String actor) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        RoleName targetRoleName;
        try {
            targetRoleName = RoleName.valueOf(targetRoleStr.toUpperCase().replace("ROLE_", ""));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role specified: " + targetRoleStr);
        }

        Role targetRole = roleRepository.findByName(targetRoleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + targetRoleName));

        Set<Role> roles = new HashSet<>();
        roles.add(targetRole);

        // Also add VIEWER and ANALYST if ADMIN
        if (targetRoleName == RoleName.ADMIN) {
            roleRepository.findByName(RoleName.ANALYST).ifPresent(roles::add);
            roleRepository.findByName(RoleName.VIEWER).ifPresent(roles::add);
        } else if (targetRoleName == RoleName.ANALYST) {
            roleRepository.findByName(RoleName.VIEWER).ifPresent(roles::add);
        }

        user.setRoles(roles);
        User saved = userRepository.save(user);

        auditLogService.log(
                actor,
                "USER_ROLE_CHANGED",
                "User",
                saved.getId(),
                "Updated roles for @" + saved.getUsername() + " to " + targetRoleName
        );

        return mapToUserResponse(saved);
    }

    @Transactional
    public AuthDto.UserResponse updateUserStatus(Long userId, boolean enabled, String actor) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setEnabled(enabled);
        User saved = userRepository.save(user);

        auditLogService.log(
                actor,
                "USER_STATUS_UPDATED",
                "User",
                saved.getId(),
                "Set enabled=" + enabled + " for @" + saved.getUsername()
        );

        return mapToUserResponse(saved);
    }

    public AuthDto.UserResponse mapToUserResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        return AuthDto.UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .enabled(user.isEnabled())
                .roles(roleNames)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
