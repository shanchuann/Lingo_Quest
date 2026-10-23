package com.lingoquest.infra.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.function.Supplier;

/** Redis-backed AI cache (default when `app.redis.enabled` is true). */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisAiCache implements AiCache {
    private final RedisTemplate<String, Object> redis;

    @Value("${app.ai.cache-ttl-hours}")
    private int ttlHours;

    @Override
    public <T> T cached(String namespace, String input, Class<T> clazz, Supplier<T> loader) {
        var key = "lingo:ai:cache:" + namespace + ":" + sha(input);
        Object hit = redis.opsForValue().get(key);
        if (clazz.isInstance(hit)) return clazz.cast(hit);
        T fresh = loader.get();
        if (fresh != null) {
            redis.opsForValue().set(key, fresh, Duration.ofHours(ttlHours));
        }
        return fresh;
    }

    private static String sha(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return Integer.toHexString(s.hashCode());
        }
    }
}
