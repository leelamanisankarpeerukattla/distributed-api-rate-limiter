package com.example.ratelimiter.api;

public class RateLimitCheckResponse {
  private boolean allowed;
  private String policyId;
  private long limit;
  private long remaining;
  private long resetEpochMs;
  private long retryAfterMs;
  private String modeUsed;

  public RateLimitCheckResponse() {}

  public RateLimitCheckResponse(boolean allowed, String policyId, long limit, long remaining, long resetEpochMs, long retryAfterMs, String modeUsed) {
    this.allowed = allowed;
    this.policyId = policyId;
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
