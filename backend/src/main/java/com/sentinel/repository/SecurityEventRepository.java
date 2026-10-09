package com.sentinel.repository;

import com.sentinel.entity.EventType;
import com.sentinel.entity.SecurityEvent;
import com.sentinel.entity.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long>, JpaSpecificationExecutor<SecurityEvent> {

    @Query("SELECT COUNT(e) FROM SecurityEvent e WHERE e.eventType = :eventType AND e.sourceIp = :sourceIp AND e.timestamp >= :since")
    long countByEventTypeAndSourceIpSince(
        @Param("eventType") EventType eventType,
        @Param("sourceIp") String sourceIp,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT COUNT(e) FROM SecurityEvent e WHERE e.eventType = :eventType AND e.source = :source AND e.timestamp >= :since")
    long countByEventTypeAndSourceSince(
        @Param("eventType") EventType eventType,
        @Param("source") String source,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT COUNT(e) FROM SecurityEvent e WHERE e.eventType = :eventType AND e.timestamp >= :since")
    long countByEventTypeSince(
        @Param("eventType") EventType eventType,
        @Param("since") LocalDateTime since
    );

    long countByTimestampAfter(LocalDateTime since);

    @Query("SELECT e.eventType, COUNT(e) FROM SecurityEvent e GROUP BY e.eventType")
    List<Object[]> countGroupedByEventType();

    @Query("SELECT e.severity, COUNT(e) FROM SecurityEvent e GROUP BY e.severity")
    List<Object[]> countGroupedBySeverity();
}
