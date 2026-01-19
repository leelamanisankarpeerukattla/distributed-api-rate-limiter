package com.example.ratelimiter;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ratelimiter.api.RateLimitCheckRequest;
import com.example.ratelimiter.api.RateLimitDecisionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimiterIntegrationTest {

  @Container
  static GenericContainer<?> redis = new GenericContainer<>("redis:7.2-alpine").withExposedPorts(6379);

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
  }

  @LocalServerPort
  int port;

  @Autowired
  TestRestTemplate rest;

  @Test
  void tokenBucket_allows_then_blocks_after_capacity() {
    // Policy perUserOrders: capacity 20 per minute.
    String baseUrl = "http://localhost:" + port + "/v1/ratelimit/check";

    RateLimitCheckRequest req = new RateLimitCheckRequest();
    req.setKey("user:123");
    req.setEndpoint("POST:/orders");
    req.setTokens(1);

    for (int i = 0; i < 20; i++) {
      RateLimitDecisionResponse res = rest.postForObject(baseUrl, req, RateLimitDecisionResponse.class);
      assertThat(res).isNotNull();
      assertThat(res.isAllowed()).isTrue();
    }

    RateLimitDecisionResponse blocked = rest.postForObject(baseUrl, req, RateLimitDecisionResponse.class);
    assertThat(blocked).isNotNull();
    assertThat(blocked.isAllowed()).isFalse();
    assertThat(blocked.getRemaining()).isEqualTo(0);
  }
}
