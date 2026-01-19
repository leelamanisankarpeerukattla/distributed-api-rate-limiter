package com.example.ratelimiter.core;

import com.example.ratelimiter.config.RateLimitPolicy;

public interface RateLimiter {
  RateLimitDecision evaluate(String redisKey, RateLimitPolicy policy, long tokens, long nowEpochMs);
}
