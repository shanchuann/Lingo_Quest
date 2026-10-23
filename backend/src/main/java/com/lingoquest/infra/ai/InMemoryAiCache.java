package com.lingoquest.infra.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * In-process AI cache used when Redis is not configured
 * (`REDIS_ENABLED=false`). TTL is honoured, but entries live only as long as
 * the JVM — acceptable for a single-instance course deployment.
 */
@Component
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "false")
public class InMemoryAiCache implements AiCache {

    private static final int MAX_ENTRIES = 512;

    private record Entry(Object value, long expiresAt) {}

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    @Value("${app.ai.cache-ttl-hours:24}")
    private int ttlHours;

    @Override
    public <T> T cached(String namespace, String input, Class<T> clazz, Supplier<T> loader) {
        var key = namespace + '\u0000' + input;
        long now = System.currentTimeMillis();

        var hit = store.get(key);
        if (hit != null) {
            if (hit.expiresAt() > now && clazz.isInstance(hit.value())) {
                return clazz.cast(hit.value());
            }
            store.remove(key);
        }

        T fresh = loader.get();
        if (fresh != null) {
            if (store.size() >= MAX_ENTRIES) evict(now);
            store.put(key, new Entry(fresh, now + Math.max(0, ttlHours) * 3_600_000L));
        }
        return fresh;
    }

    private void evict(long now) {
        store.entrySet().removeIf(e -> e.getValue().expiresAt() <= now);
        if (store.size() >= MAX_ENTRIES) {
            var it = store.keySet().iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
        }
    }
}
