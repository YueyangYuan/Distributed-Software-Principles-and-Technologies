package com.seckill.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seckill.common.dto.Result;
import com.seckill.common.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Set<String> WHITE_LIST = Set.of(
            "/api/user/login",
            "/api/user/register",
            "/actuator/health",
            "/fallback/"
    );

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isWhitelisted(path) || HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                Long userId = JwtUtil.getUserId(token);
                return chain.filter(exchange.mutate()
                        .request(builder -> builder.header("X-User-Id", String.valueOf(userId)))
                        .build());
            } catch (Exception ignored) {
                return unauthorized(exchange);
            }
        }

        return unauthorized(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isWhitelisted(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body;
        try {
            body = objectMapper.writeValueAsString(Result.fail(401, "Unauthorized or token expired"))
                    .getBytes(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            body = "{\"code\":401,\"msg\":\"Unauthorized or token expired\"}".getBytes(StandardCharsets.UTF_8);
        }
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }
}
