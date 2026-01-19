package com.example.ratelimiter.config;

public class PolicyMatch {
  /**
   * Expected format: "METHOD:/path" (e.g., "POST:/orders").
   * Supports "*" wildcard or simple glob patterns with "*".
   */
  private String endpoint = "*";

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public boolean matches(String candidate) {
    if (endpoint == null || endpoint.isBlank() || "*".equals(endpoint)) {
      return true;
    }
    if (candidate == null) {
      return false;
    }
    // Very small glob support: '*' matches any sequence.
    String regex = endpoint.replace(".", "\\.").replace("*", ".*");
    return candidate.matches(regex);
  }
}
