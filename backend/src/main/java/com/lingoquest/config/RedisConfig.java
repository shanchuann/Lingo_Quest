package com.lingoquest.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis template for the cache / rate limit / weekly leaderboard.
 * Skipped entirely when `REDIS_ENABLED=false`, in which case the in-memory
 * fallbacks take over.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
        var t = new RedisTemplate<String, Object>();
        t.setConnectionFactory(cf);
        var keySer = new StringRedisSerializer();
        var valSer = new GenericJackson2JsonRedisSerializer();
        t.setKeySerializer(keySer);
        t.setHashKeySerializer(keySer);
        t.setValueSerializer(valSer);
        t.setHashValueSerializer(valSer);
        t.afterPropertiesSet();
        return t;
    }
}
