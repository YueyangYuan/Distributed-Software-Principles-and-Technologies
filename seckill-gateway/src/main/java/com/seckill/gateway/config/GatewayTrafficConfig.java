package com.seckill.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayTrafficConfig {

    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(resolveClientKey(exchange));
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("X-User-Id"))
                .switchIfEmpty(Mono.just(resolveClientKey(exchange)));
    }

    private String resolveClientKey(ServerWebExchange exchange) {
        if (exchange.getRequest().getRemoteAddress() == null) {
            return "unknown-client";
        }
        return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }
}
