package com.example.TriageIQ.Security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MINUTES = 15;

    private record Attempt(AtomicInteger count, Instant windowStart) {}

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void recordFailure(String email) {
        attempts.compute(email, (k, existing) -> {
            if (existing == null || windowExpired(existing.windowStart())) {
                return new Attempt(new AtomicInteger(1), Instant.now());
            }
            existing.count().incrementAndGet();
            return existing;
        });
    }

    public void recordSuccess(String email) {
        attempts.remove(email);
    }

    public boolean isBlocked(String email) {
        Attempt attempt = attempts.get(email);
        if (attempt == null) return false;
        if (windowExpired(attempt.windowStart())) {
            attempts.remove(email);
            return false;
        }
        return attempt.count().get() >= MAX_ATTEMPTS;
    }

    private boolean windowExpired(Instant windowStart) {
        return Instant.now().isAfter(windowStart.plusSeconds(WINDOW_MINUTES * 60));
    }
}
