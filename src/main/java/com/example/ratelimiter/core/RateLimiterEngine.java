package com.example.ratelimiter.core;

import com.example.ratelimiter.api.RateLimitCheckRequest;
import com.example.ratelimiter.api.RateLimitDecisionResponse;
import com.example.ratelimiter.config.Algorithm;
import com.example.ratelimiter.config.FailureMode;
import com.example.ratelimiter.config.KeyType;
import com.example.ratelimiter.config.RateLimitPolicy;
import com.example.ratelimiter.config.RateLimiterProperties;
import jakarta.servlet.http.HttpServletRequest;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterEngine {

  private final RateLimiterProperties props;
  private final TokenBucketRedisRateLimiter tokenBucket;
  private final SlidingWindowRedisRateLimiter slidingWindow;
  private final MeterRegistry registry;

  public RateLimiterEngine(RateLimiterProperties props,
                           TokenBucketRedisRateLimiter tokenBucket,
                           SlidingWindowRedisRateLimiter slidingWindow,
                           MeterRegistry registry) {
    this.props = props;
    this.tokenBucket = tokenBucket;
    this.slidingWindow = slidingWindow;
    this.registry = registry;
  }

  public List<RateLimitPolicy> policies() {
    return new ArrayList<>(props.getPolicies());
  }

  public RateLimitDecisionResponse check(RateLimitCheckRequest req, HttpServletRequest http) {
    long now = System.currentTimeMillis();

    RateLimitPolicy policy = selectPolicy(req.getEndpoint()).orElse(null);
    if (policy == null) {
      // No matching policy => allow by default.
      return new RateLimitDecisionResponse(true, null, safeKey(req, http, KeyType.USER), req.getEndpoint(), 0, 0, 0, 0, props.getDefaultMode().name());
    }

    String resolvedKey = resolveKey(req, http, policy.getKeyType());
    String redisKey = buildRedisKey(props.getKeyPrefix(), policy.getAlgorithm(), policy.getId(), resolvedKey, req.getEndpoint());

    long tokens = Math.max(1, req.getTokens());

    try {
      RateLimitDecision decision = limiter(policy.getAlgorithm()).evaluate(redisKey, policy, tokens, now);
      record(policy, decision.isAllowed());
      return new RateLimitDecisionResponse(
          decision.isAllowed(),
          policy.getId(),
          resolvedKey,
          req.getEndpoint(),
          decision.getLimit(),
          decision.getRemaining(),
          decision.getResetEpochMs(),
          decision.getRetryAfterMs(),
          props.getDefaultMode().name()
      );
    } catch (Exception e) {
      // Redis outage / timeout / script errors.
      boolean allow = props.getDefaultMode() == FailureMode.FAIL_OPEN;
      record(policy, allow);
      long limit = policy.getAlgorithm() == Algorithm.TOKEN_BUCKET ? policy.getCapacity() : policy.getLimit();
      return new RateLimitDecisionResponse(
          allow,
          policy.getId(),
          resolvedKey,
          req.getEndpoint(),
          limit,
          allow ? limit : 0,
          now,
          allow ? 0 : 1000,
          props.getDefaultMode().name()
      );
    }
  }

  private Optional<RateLimitPolicy> selectPolicy(String endpoint) {
    if (props.getPolicies() == null) return Optional.empty();
    return props.getPolicies().stream()
        .filter(RateLimitPolicy::isEnabled)
        .filter(p -> p.getMatch() != null && p.getMatch().matches(endpoint))
        .findFirst();
  }

  private RateLimiter limiter(Algorithm algo) {
    return algo == Algorithm.SLIDING_WINDOW ? slidingWindow : tokenBucket;
  }

  private void record(RateLimitPolicy policy, boolean allowed) {
    String outcome = allowed ? "allowed" : "blocked";
    Counter.builder("ratelimiter_decisions_total")
        .tag("policy", safe(policy.getId()))
        .tag("algorithm", safe(policy.getAlgorithm().name()))
        .tag("outcome", outcome)
        .register(registry)
        .increment();
  }

  private String resolveKey(RateLimitCheckRequest req, HttpServletRequest http, KeyType keyType) {
    if (req.getKey() != null && !req.getKey().isBlank()) {
      return req.getKey().trim();
    }
    return safeKey(req, http, keyType);
  }

  private String safeKey(RateLimitCheckRequest req, HttpServletRequest http, KeyType keyType) {
    return switch (keyType) {
      case USER -> header(http, "X-User-Id").orElse("anonymous");
      case API -> header(http, "X-Api-Key").orElse("unknown-api");
      case IP -> resolveClientIp(req, http);
    };
  }

  private String resolveClientIp(RateLimitCheckRequest req, HttpServletRequest http) {
    if (req.getIp() != null && !req.getIp().isBlank()) {
      return req.getIp().trim();
    }
    String xff = http.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      // First IP in XFF is the original client
      return xff.split(",")[0].trim();
    }
    return http.getRemoteAddr() == null ? "0.0.0.0" : http.getRemoteAddr();
  }

  private Optional<String> header(HttpServletRequest http, String name) {
    String v = http.getHeader(name);
    if (v == null || v.isBlank()) return Optional.empty();
    return Optional.of(v.trim());
  }

  private String buildRedisKey(String prefix, Algorithm algo, String policyId, String key, String endpoint) {
    // Keep keys deterministic + bounded.
    String ep = endpoint.replace("/", "_").replace(":", "_");
    return String.format("%s:%s:%s:%s:%s", prefix, algo.name().toLowerCase(), safe(policyId), safe(key), ep);
  }

  private String safe(String s) {
    return s == null ? "unknown" : s;
  }
}
