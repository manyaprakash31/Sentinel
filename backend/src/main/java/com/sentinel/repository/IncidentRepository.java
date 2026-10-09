package com.sentinel.repository;

import com.sentinel.entity.Incident;
import com.sentinel.entity.IncidentStatus;
import com.sentinel.entity.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {
    long countByStatus(IncidentStatus status);
    long countByStatusNot(IncidentStatus status);
    long countBySeverity(Severity severity);

    @Query("SELECT i.status, COUNT(i) FROM Incident i GROUP BY i.status")
    List<Object[]> countGroupedByStatus();

    @Query("SELECT i.severity, COUNT(i) FROM Incident i GROUP BY i.severity")
    List<Object[]> countGroupedBySeverity();

    List<Incident> findTop5ByOrderByDetectedAtDesc();
}
