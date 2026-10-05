package com.mrsoft.arabicreference.search.infrastructure;

import com.mrsoft.arabicreference.search.domain.SearchThrottle;
import com.mrsoft.arabicreference.shared.kernel.exception.RateLimitedException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemorySearchThrottle implements SearchThrottle {

    private final int limit;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public InMemorySearchThrottle(SearchProperties properties) {
        this.limit = Math.max(properties.getRateLimitPerMinute(), 1);
    }

    @Override
    public void acquire(String clientAddress) {
        String key = clientAddress == null || clientAddress.isBlank() ? "unknown" : clientAddress;
        long minute = System.currentTimeMillis() / 60_000L;
        Window window = windows.compute(key, (ignored, current) -> current == null || current.minute != minute ? new Window(minute, 1) : current.increment());
        if (window.count > limit) {
            throw new RateLimitedException("Search is temporarily limited.");
        }
    }

    private record Window(long minute, int count) {
        private Window increment() {
            return new Window(minute, count + 1);
        }
    }
}
