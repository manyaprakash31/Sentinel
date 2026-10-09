package com.sentinel.service;

import com.sentinel.dto.AuthDto;
import com.sentinel.entity.Role;
import com.sentinel.entity.RoleName;
import com.sentinel.entity.User;
import com.sentinel.exception.BadRequestException;
import com.sentinel.repository.RoleRepository;
import com.sentinel.repository.UserRepository;
import com.sentinel.security.JwtUtils;
import com.sentinel.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
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
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final AuditLogService auditLogService;

    public AuthDto.JwtResponse authenticateUser(AuthDto.LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        auditLogService.log(
                userDetails.getUsername(),
                "USER_LOGIN",
                "User",
                userDetails.getId(),
                "User logged in successfully"
        );

        return AuthDto.JwtResponse.builder()
                .token(jwt)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .fullName(userDetails.getFullName())
                .roles(roles)
                .build();
    }

    @Transactional
    public User registerUser(AuthDto.RegisterRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new BadRequestException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new BadRequestException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .fullName(signUpRequest.getFullName())
                .enabled(true)
                .build();

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role userRole = roleRepository.findByName(RoleName.VIEWER)
                    .orElseThrow(() -> new RuntimeException("Error: Default Role VIEWER is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                String normalized = role.toUpperCase().replace("ROLE_", "");
                switch (normalized) {
                    case "ADMIN":
                        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found."));
                        roles.add(adminRole);
                        break;
                    case "ANALYST":
                        Role analystRole = roleRepository.findByName(RoleName.ANALYST)
                                .orElseThrow(() -> new RuntimeException("Error: Role ANALYST is not found."));
                        roles.add(analystRole);
                        break;
                    default:
                        Role viewerRole = roleRepository.findByName(RoleName.VIEWER)
                                .orElseThrow(() -> new RuntimeException("Error: Role VIEWER is not found."));
                        roles.add(viewerRole);
                }
            });
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        auditLogService.log(
                "SYSTEM",
                "USER_REGISTERED",
                "User",
                savedUser.getId(),
                "Registered new user: " + savedUser.getUsername()
        );

        return savedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
