package com.example.ratelimiter.api;

import jakarta.validation.constraints.NotBlank;

public class RateLimitCheckRequest {

  /**
   * Optional. If omitted, the service attempts to derive a key from headers / IP based on policy keyType.
   */
  private String key;

  /**
   * Endpoint identifier in "METHOD:/path" format.
   */
  @NotBlank
  private String endpoint;

  /**
   * Number of tokens to consume. Defaults to 1.
   */
  private long tokens = 1;

  /**
   * Optional IP override (e.g., if you call this service from a gateway and want to pass original client IP).
   */
  private String ip;

  public String getKey() { return key; }
  public void setKey(String key) { this.key = key; }

  public String getEndpoint() { return endpoint; }
  public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

  public long getTokens() { return tokens; }
  public void setTokens(long tokens) { this.tokens = tokens; }

  public String getIp() { return ip; }
  public void setIp(String ip) { this.ip = ip; }
}
