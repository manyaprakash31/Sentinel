package com.sentinel.service;

import com.sentinel.entity.DetectionRule;
import com.sentinel.entity.SecurityEvent;
import com.sentinel.repository.DetectionRuleRepository;
import com.sentinel.repository.SecurityEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DetectionRuleEngineService {

    private final DetectionRuleRepository ruleRepository;
    private final SecurityEventRepository eventRepository;
    private final AlertService alertService;

    /**
     * Evaluates active defensive detection rules against a newly ingested security event.
     */
    public void evaluateEvent(SecurityEvent event) {
        List<DetectionRule> matchingRules = ruleRepository.findByEventTypeAndEnabledTrue(event.getEventType());

        for (DetectionRule rule : matchingRules) {
            LocalDateTime since = LocalDateTime.now().minusMinutes(rule.getTimeWindowMinutes());

            // Count occurrences from the same source IP (or fallback to source)
            long count = eventRepository.countByEventTypeAndSourceIpSince(
                    event.getEventType(),
                    event.getSourceIp(),
                    since
            );

            // Also check by source entity (e.g. for service failure rules)
            if (count < rule.getThreshold() && event.getSource() != null) {
                long sourceCount = eventRepository.countByEventTypeAndSourceSince(
                        event.getEventType(),
                        event.getSource(),
                        since
                );
                if (sourceCount > count) {
                    count = sourceCount;
                }
            }

            log.debug("Rule '{}' evaluation: count = {}, threshold = {}", rule.getName(), count, rule.getThreshold());

            if (count >= rule.getThreshold()) {
                log.info("Detection Rule MATCHED: '{}'! Threshold {} reached with {} events in {} mins.",
                        rule.getName(), rule.getThreshold(), count, rule.getTimeWindowMinutes());
                alertService.createAlertFromRule(rule, event, count);
            }
        }
    }
}
