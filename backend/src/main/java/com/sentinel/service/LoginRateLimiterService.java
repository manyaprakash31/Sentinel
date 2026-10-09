package com.sentinel.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiterService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION_SECONDS = 15 * 60; // 15 minutes lockout

    private static class Attempt {
        int count;
        long lastAttemptTimestamp;

        Attempt(int count, long timestamp) {
            this.count = count;
            this.lastAttemptTimestamp = timestamp;
        }
    }

    private final ConcurrentHashMap<String, Attempt> attemptsCache = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt attempt = attemptsCache.get(key);
        if (attempt == null) {
            return false;
        }

        long currentTime = Instant.now().getEpochSecond();
        if (attempt.count >= MAX_ATTEMPTS) {
            if (currentTime - attempt.lastAttemptTimestamp < LOCK_TIME_DURATION_SECONDS) {
                return true;
            } else {
                // Lockout expired, reset
                attemptsCache.remove(key);
                return false;
            }
        }
        return false;
    }

    public void recordFailedAttempt(String key) {
        long currentTime = Instant.now().getEpochSecond();
        attemptsCache.compute(key, (k, attempt) -> {
            if (attempt == null) {
                return new Attempt(1, currentTime);
            }
            if (currentTime - attempt.lastAttemptTimestamp >= LOCK_TIME_DURATION_SECONDS) {
                return new Attempt(1, currentTime);
            }
            attempt.count++;
            attempt.lastAttemptTimestamp = currentTime;
            return attempt;
        });
    }

    public void recordSuccess(String key) {
        attemptsCache.remove(key);
    }
}
