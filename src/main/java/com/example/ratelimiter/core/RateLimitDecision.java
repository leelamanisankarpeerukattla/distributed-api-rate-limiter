package com.example.ratelimiter.core;

public class RateLimitDecision {
  private final boolean allowed;
  private final long limit;
  private final long remaining;
  private final long resetEpochMs;
  private final long retryAfterMs;

  public RateLimitDecision(boolean allowed, long limit, long remaining, long resetEpochMs, long retryAfterMs) {
    this.allowed = allowed;
    this.limit = limit;
    this.remaining = remaining;
    this.resetEpochMs = resetEpochMs;
    this.retryAfterMs = retryAfterMs;
  }

  public boolean isAllowed() { return allowed; }
  public long getLimit() { return limit; }
  public long getRemaining() { return remaining; }
  public long getResetEpochMs() { return resetEpochMs; }
  public long getRetryAfterMs() { return retryAfterMs; }
}
