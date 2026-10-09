package com.sentinel.repository;

import com.sentinel.entity.MonitoredService;
import com.sentinel.entity.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonitoredServiceRepository extends JpaRepository<MonitoredService, Long> {
    Optional<MonitoredService> findByName(String name);
    long countByStatus(ServiceStatus status);
}
