package com.example.ratelimiter.core;

import org.springframework.beans.factory.annotation.Qualifier;
import com.example.ratelimiter.config.RateLimitPolicy;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class TokenBucketRedisRateLimiter implements RateLimiter {

  private final RedisTemplate<String, String> redis;
  private final DefaultRedisScript<List> tokenBucketScript;

  public TokenBucketRedisRateLimiter(
          RedisTemplate<String, String> redis,
          @Qualifier("tokenBucketScript") DefaultRedisScript<List> tokenBucketScript
  ) {
    this.redis = redis;
    this.tokenBucketScript = tokenBucketScript;
  }

  @Override
  public RateLimitDecision evaluate(String redisKey, RateLimitPolicy policy, long tokens, long nowEpochMs) {
    long ttlMs = Math.max(policy.getRefillPeriodMs() * 3, 60_000);

    List<?> out = redis.execute(
        tokenBucketScript,
        Arrays.asList(redisKey),
        String.valueOf(nowEpochMs),
        String.valueOf(tokens),
        String.valueOf(policy.getCapacity()),
        String.valueOf(policy.getRefillTokens()),
        String.valueOf(policy.getRefillPeriodMs()),
        String.valueOf(ttlMs)
    );

    if (out == null || out.size() < 5) {
      // Defensive default if script result shape is unexpected.
      return new RateLimitDecision(true, policy.getCapacity(), policy.getCapacity(), nowEpochMs + policy.getRefillPeriodMs(), 0);
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
