package com.sentinel.repository;

import com.sentinel.entity.Alert;
import com.sentinel.entity.AlertStatus;
import com.sentinel.entity.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long>, JpaSpecificationExecutor<Alert> {
    long countByStatus(AlertStatus status);
    long countBySeverity(Severity severity);
    long countByStatusNot(AlertStatus status);
    long countBySeverityAndStatusNot(Severity severity, AlertStatus status);
    long countByDetectedAtAfter(LocalDateTime since);

    @Query("SELECT a.severity, COUNT(a) FROM Alert a GROUP BY a.severity")
    List<Object[]> countGroupedBySeverity();

    @Query("SELECT a.status, COUNT(a) FROM Alert a GROUP BY a.status")
    List<Object[]> countGroupedByStatus();

    List<Alert> findTop5ByOrderByDetectedAtDesc();
}
