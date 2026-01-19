package com.example.ratelimiter.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

//  @Bean
//  public RedisTemplate<String, String> stringRedisTemplate(RedisConnectionFactory factory) {
//    RedisTemplate<String, String> template = new RedisTemplate<>();
//    template.setConnectionFactory(factory);
//    StringRedisSerializer ser = new StringRedisSerializer();
//    template.setKeySerializer(ser);
//    template.setValueSerializer(ser);
//    template.setHashKeySerializer(ser);
//    template.setHashValueSerializer(ser);
//    template.afterPropertiesSet();
//    return template;
//  }
}
