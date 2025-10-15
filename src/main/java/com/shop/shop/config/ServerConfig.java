package com.shop.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ServerConfig {
    @Bean
    public ConcurrentHashMap<String, Boolean> idempotencyTokenCache() {
        return new ConcurrentHashMap<>();
    }
}
