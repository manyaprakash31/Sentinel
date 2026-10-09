package com.sentinel.repository;

import com.sentinel.entity.DetectionRule;
import com.sentinel.entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetectionRuleRepository extends JpaRepository<DetectionRule, Long> {
    List<DetectionRule> findByEnabledTrue();
    List<DetectionRule> findByEventTypeAndEnabledTrue(EventType eventType);
    boolean existsByName(String name);
}
