package com.lingoquest.infra.ai;

/** Per-user AI request rate limiter. */
public interface AiRateLimiter {

    /**
     * @throws com.lingoquest.common.exception.BizException when the user exceeds
     *         the per-minute quota or is not authenticated.
     */
    void check(Long userId);
}
