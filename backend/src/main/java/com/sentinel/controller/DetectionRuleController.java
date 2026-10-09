package com.sentinel.controller;

import com.sentinel.dto.ApiResponse;
import com.sentinel.dto.DetectionRuleDto;
import com.sentinel.service.DetectionRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class DetectionRuleController {

    private final DetectionRuleService ruleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DetectionRuleDto.Response>>> getAllRules() {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.getAllRules()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DetectionRuleDto.Response>> getRuleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(ruleService.getRuleById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DetectionRuleDto.Response>> createRule(
            @Valid @RequestBody DetectionRuleDto.CreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String actor = userDetails != null ? userDetails.getUsername() : "ADMIN";
        DetectionRuleDto.Response rule = ruleService.createRule(request, actor);
        return ResponseEntity.ok(ApiResponse.ok("Rule created successfully", rule));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DetectionRuleDto.Response>> updateRule(
            @PathVariable Long id,
            @RequestBody DetectionRuleDto.UpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String actor = userDetails != null ? userDetails.getUsername() : "ADMIN";
        DetectionRuleDto.Response rule = ruleService.updateRule(id, request, actor);
        return ResponseEntity.ok(ApiResponse.ok("Rule updated successfully", rule));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deleteRule(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String actor = userDetails != null ? userDetails.getUsername() : "ADMIN";
        ruleService.deleteRule(id, actor);
        return ResponseEntity.ok(ApiResponse.ok("Rule deleted successfully", null));
    }
}
