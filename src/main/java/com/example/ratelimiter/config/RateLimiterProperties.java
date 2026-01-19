package com.example.ratelimiter.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ratelimiter")
public class RateLimiterProperties {

  private FailureMode defaultMode = FailureMode.FAIL_CLOSED;
  private String keyPrefix = "rl";
  private List<RateLimitPolicy> policies = new ArrayList<>();

  public FailureMode getDefaultMode() {
    return defaultMode;
  }

  public void setDefaultMode(FailureMode defaultMode) {
    this.defaultMode = defaultMode;
  }

  public String getKeyPrefix() {
    return keyPrefix;
  }

  public void setKeyPrefix(String keyPrefix) {
    this.keyPrefix = keyPrefix;
  }

  public List<RateLimitPolicy> getPolicies() {
    return policies;
  }

  public void setPolicies(List<RateLimitPolicy> policies) {
    this.policies = policies;
  }
}
