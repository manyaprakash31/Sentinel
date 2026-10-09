package com.sentinel.service;

import com.sentinel.dto.DetectionRuleDto;
import com.sentinel.entity.DetectionRule;
import com.sentinel.exception.BadRequestException;
import com.sentinel.exception.ResourceNotFoundException;
import com.sentinel.repository.DetectionRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DetectionRuleService {

    private final DetectionRuleRepository ruleRepository;
    private final AuditLogService auditLogService;

    public List<DetectionRuleDto.Response> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public DetectionRuleDto.Response getRuleById(Long id) {
        DetectionRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found with id: " + id));
        return mapToResponse(rule);
    }

    @Transactional
    public DetectionRuleDto.Response createRule(DetectionRuleDto.CreateRequest request, String actor) {
        if (ruleRepository.existsByName(request.getName())) {
            throw new BadRequestException("Rule with name '" + request.getName() + "' already exists");
        }

        DetectionRule rule = DetectionRule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .eventType(request.getEventType())
                .threshold(request.getThreshold())
                .timeWindowMinutes(request.getTimeWindowMinutes())
                .severity(request.getSeverity())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .build();

        DetectionRule saved = ruleRepository.save(rule);

        auditLogService.log(actor, "RULE_CREATED", "DetectionRule", saved.getId(),
                "Created detection rule: " + saved.getName());

        return mapToResponse(saved);
    }

    @Transactional
    public DetectionRuleDto.Response updateRule(Long id, DetectionRuleDto.UpdateRequest request, String actor) {
        DetectionRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found with id: " + id));

        if (request.getName() != null) rule.setName(request.getName());
        if (request.getDescription() != null) rule.setDescription(request.getDescription());
        if (request.getEventType() != null) rule.setEventType(request.getEventType());
        if (request.getThreshold() != null) rule.setThreshold(request.getThreshold());
        if (request.getTimeWindowMinutes() != null) rule.setTimeWindowMinutes(request.getTimeWindowMinutes());
        if (request.getSeverity() != null) rule.setSeverity(request.getSeverity());
        if (request.getEnabled() != null) rule.setEnabled(request.getEnabled());

        DetectionRule updated = ruleRepository.save(rule);

        auditLogService.log(actor, "RULE_UPDATED", "DetectionRule", updated.getId(),
                "Updated detection rule: " + updated.getName());

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteRule(Long id, String actor) {
        DetectionRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rule not found with id: " + id));

        ruleRepository.delete(rule);

        auditLogService.log(actor, "RULE_DELETED", "DetectionRule", id,
                "Deleted detection rule: " + rule.getName());
    }

    public DetectionRuleDto.Response mapToResponse(DetectionRule rule) {
        return DetectionRuleDto.Response.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .eventType(rule.getEventType())
                .threshold(rule.getThreshold())
                .timeWindowMinutes(rule.getTimeWindowMinutes())
                .severity(rule.getSeverity())
                .enabled(rule.isEnabled())
                .createdAt(rule.getCreatedAt())
                .build();
    }
}
