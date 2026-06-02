package com.seckill.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    @Test
    void allowsWhitelistedPathWithoutToken() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/user/login").build());
        AtomicBoolean invoked = new AtomicBoolean(false);

        filter.filter(exchange, chainMarking(invoked)).block();

        assertThat(invoked).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void rejectsProtectedPathWithoutToken() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/product/list").build());
        AtomicBoolean invoked = new AtomicBoolean(false);

        filter.filter(exchange, chainMarking(invoked)).block();

        assertThat(invoked).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private GatewayFilterChain chainMarking(AtomicBoolean invoked) {
        return exchange -> {
            invoked.set(true);
            return Mono.empty();
        };
    }
}
