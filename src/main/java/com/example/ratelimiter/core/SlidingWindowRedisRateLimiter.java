package com.example.ratelimiter.core;

import org.springframework.beans.factory.annotation.Qualifier;
import com.example.ratelimiter.config.RateLimitPolicy;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class SlidingWindowRedisRateLimiter implements RateLimiter {

  private final RedisTemplate<String, String> redis;
  private final DefaultRedisScript<List> slidingWindowScript;

  public SlidingWindowRedisRateLimiter(
          RedisTemplate<String, String> redis,
          @Qualifier("slidingWindowScript") DefaultRedisScript<List> slidingWindowScript
  ) {
    this.redis = redis;
    this.slidingWindowScript = slidingWindowScript;
  }

  @Override
  public RateLimitDecision evaluate(String redisKey, RateLimitPolicy policy, long tokens, long nowEpochMs) {
    long ttlMs = Math.max(policy.getWindowMs() * 3, 60_000);

    List<?> out = redis.execute(
        slidingWindowScript,
        Arrays.asList(redisKey, redisKey + ":seq"),
        String.valueOf(nowEpochMs),
        String.valueOf(tokens),
        String.valueOf(policy.getLimit()),
        String.valueOf(policy.getWindowMs()),
        String.valueOf(ttlMs)
    );

    if (out == null || out.size() < 5) {
      return new RateLimitDecision(true, policy.getLimit(), policy.getLimit(), nowEpochMs + policy.getWindowMs(), 0);
    }

    boolean allowed = toLong(out.get(0)) == 1L;
    long remaining = toLong(out.get(1));
    long resetEpochMs = toLong(out.get(2));
    long limit = toLong(out.get(3));
    long retryAfterMs = toLong(out.get(4));

    return new RateLimitDecision(allowed, limit, remaining, resetEpochMs, retryAfterMs);
  }

  private long toLong(Object o) {
    if (o == null) return 0;
    if (o instanceof Long l) return l;
    if (o instanceof Integer i) return i.longValue();
    if (o instanceof String s) return Long.parseLong(s);
    return Long.parseLong(o.toString());
  }
}
