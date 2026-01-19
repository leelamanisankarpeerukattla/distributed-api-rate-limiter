package com.example.ratelimiter.api;

import com.example.ratelimiter.config.RateLimitPolicy;
import com.example.ratelimiter.core.RateLimiterEngine;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/ratelimit")
public class RateLimiterController {

  private final RateLimiterEngine engine;

  public RateLimiterController(RateLimiterEngine engine) {
    this.engine = engine;
  }

  @PostMapping("/check")
  public ResponseEntity<RateLimitDecisionResponse> check(@Valid @RequestBody RateLimitCheckRequest request,
                                                         HttpServletRequest http) {
    RateLimitDecisionResponse decision = engine.check(request, http);
    return ResponseEntity.ok()
        .header("X-RateLimit-Limit", String.valueOf(decision.getLimit()))
        .header("X-RateLimit-Remaining", String.valueOf(decision.getRemaining()))
        .header("X-RateLimit-Reset", String.valueOf(decision.getResetEpochMs()))
        .header("Retry-After", String.valueOf(Math.max(0, decision.getRetryAfterMs() / 1000)))
        .body(decision);
  }

  @GetMapping("/policies")
  public List<RateLimitPolicy> policies() {
    return engine.policies();
  }
}
