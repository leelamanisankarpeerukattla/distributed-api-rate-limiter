package com.example.ratelimiter.config;

public class RateLimitPolicy {
  private String id;
  private boolean enabled = true;
  private PolicyMatch match = new PolicyMatch();
  private KeyType keyType = KeyType.USER;
  private Algorithm algorithm = Algorithm.TOKEN_BUCKET;

  // TOKEN_BUCKET fields
  private long capacity;
  private long refillTokens;
  private long refillPeriodMs;

  // SLIDING_WINDOW fields
  private long limit;
  private long windowMs;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public PolicyMatch getMatch() {
    return match;
  }

  public void setMatch(PolicyMatch match) {
    this.match = match;
  }

  public KeyType getKeyType() {
    return keyType;
  }

  public void setKeyType(KeyType keyType) {
    this.keyType = keyType;
  }

  public Algorithm getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(Algorithm algorithm) {
    this.algorithm = algorithm;
  }

  public long getCapacity() {
    return capacity;
  }

  public void setCapacity(long capacity) {
    this.capacity = capacity;
  }

  public long getRefillTokens() {
    return refillTokens;
  }

  public void setRefillTokens(long refillTokens) {
    this.refillTokens = refillTokens;
  }

  public long getRefillPeriodMs() {
    return refillPeriodMs;
  }

  public void setRefillPeriodMs(long refillPeriodMs) {
    this.refillPeriodMs = refillPeriodMs;
  }

  public long getLimit() {
    return limit;
  }

  public void setLimit(long limit) {
    this.limit = limit;
  }

  public long getWindowMs() {
    return windowMs;
  }

  public void setWindowMs(long windowMs) {
    this.windowMs = windowMs;
  }
}
