package com.example.Bankomat.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private final int MAX_ATTEMPTS = 3;
    private final long BLOCK_DURATION_MINUTES = 60;
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> blockExpiryCache = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = 60000)
    public void cleanUpExpiredBlocks() {
        LocalDateTime now = LocalDateTime.now();
        blockExpiryCache.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }

    public synchronized void loginFailed(String key) {
        if (isBlocked(key)) return;

        int attempts = attemptsCache.getOrDefault(key, 0) + 1;
        attemptsCache.put(key, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            blockExpiryCache.put(key, LocalDateTime.now().plusMinutes(BLOCK_DURATION_MINUTES));
        }
    }

    public synchronized void loginSucceeded(String key) {
        attemptsCache.remove(key);
        blockExpiryCache.remove(key);
    }

    public synchronized boolean isBlocked(String key) {
        LocalDateTime expiry = blockExpiryCache.get(key);
        if (expiry != null && expiry.isAfter(LocalDateTime.now())) {
            return true;
        }
        if (expiry != null && expiry.isBefore(LocalDateTime.now())) {
            blockExpiryCache.remove(key);
            attemptsCache.remove(key);
        }
        return false;
    }

    public synchronized int getAttempts(String key) {
        return attemptsCache.getOrDefault(key, 0);
    }

    public synchronized int getRemainingAttempts(String key) {
        return MAX_ATTEMPTS - getAttempts(key);
    }
}
