package com.lingoquest.infra.ai;

import java.util.function.Supplier;

/**
 * AI response cache abstraction.
 *
 * <p>When Redis is available the Redis-backed implementation is used;
 * when the deployment has no Redis (e.g. MySQL-only hosting) the
 * in-process implementation keeps the same behaviour for a single instance.</p>
 */
public interface AiCache {

    /**
     * Return the cached value for {@code namespace + input}, or compute it.
     */
    <T> T cached(String namespace, String input, Class<T> clazz, Supplier<T> loader);
}
