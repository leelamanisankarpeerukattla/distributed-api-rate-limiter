package com.example.ratelimiter.api;

public class RateLimitDecisionResponse {
  private boolean allowed;
  private String policyId;
  private String key;
  private String endpoint;
  private long limit;
  private long remaining;
  private long resetEpochMs;
  private long retryAfterMs;
  private String modeUsed;

  public RateLimitDecisionResponse() {}

  public RateLimitDecisionResponse(boolean allowed, String policyId, String key, String endpoint,
                                   long limit, long remaining, long resetEpochMs, long retryAfterMs, String modeUsed) {
    this.allowed = allowed;
    this.policyId = policyId;
    this.key = key;
    this.endpoint = endpoint;
    this.limit = limit;
    this.remaining = remaining;
    this.resetEpochMs = resetEpochMs;
    this.retryAfterMs = retryAfterMs;
    this.modeUsed = modeUsed;
  }

  public boolean isAllowed() { return allowed; }
  public void setAllowed(boolean allowed) { this.allowed = allowed; }

  public String getPolicyId() { return policyId; }
  public void setPolicyId(String policyId) { this.policyId = policyId; }

  public String getKey() { return key; }
  public void setKey(String key) { this.key = key; }

  public String getEndpoint() { return endpoint; }
  public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

  public long getLimit() { return limit; }
  public void setLimit(long limit) { this.limit = limit; }

  public long getRemaining() { return remaining; }
  public void setRemaining(long remaining) { this.remaining = remaining; }

  public long getResetEpochMs() { return resetEpochMs; }
  public void setResetEpochMs(long resetEpochMs) { this.resetEpochMs = resetEpochMs; }

  public long getRetryAfterMs() { return retryAfterMs; }
  public void setRetryAfterMs(long retryAfterMs) { this.retryAfterMs = retryAfterMs; }

  public String getModeUsed() { return modeUsed; }
  public void setModeUsed(String modeUsed) { this.modeUsed = modeUsed; }
}
