package com.sentinel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimeNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastEvent(Object event) {
        try {
            messagingTemplate.convertAndSend("/topic/events", event);
        } catch (Exception e) {
            log.error("Failed to broadcast security event via WebSocket: {}", e.getMessage());
        }
    }

    public void broadcastAlert(Object alert) {
        try {
            messagingTemplate.convertAndSend("/topic/alerts", alert);
        } catch (Exception e) {
            log.error("Failed to broadcast alert via WebSocket: {}", e.getMessage());
        }
    }

    public void broadcastIncident(Object incident) {
        try {
            messagingTemplate.convertAndSend("/topic/incidents", incident);
        } catch (Exception e) {
            log.error("Failed to broadcast incident via WebSocket: {}", e.getMessage());
        }
    }

    public void broadcastServiceStatus(Object service) {
        try {
            messagingTemplate.convertAndSend("/topic/services", service);
        } catch (Exception e) {
            log.error("Failed to broadcast service status via WebSocket: {}", e.getMessage());
        }
    }

    public void broadcastDashboardUpdate(Map<String, Object> payload) {
        try {
            messagingTemplate.convertAndSend("/topic/dashboard", payload);
        } catch (Exception e) {
            log.error("Failed to broadcast dashboard update: {}", e.getMessage());
        }
    }
}
