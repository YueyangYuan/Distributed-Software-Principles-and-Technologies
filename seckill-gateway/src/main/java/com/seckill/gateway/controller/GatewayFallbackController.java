package com.seckill.gateway.controller;

import com.seckill.common.dto.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class GatewayFallbackController {

    @GetMapping("/fallback/product")
    public Mono<Result<String>> productFallback() {
        return Mono.just(Result.fail(503, "Product service is temporarily degraded. Please retry later."));
    }

    @GetMapping("/fallback/seckill")
    public Mono<Result<String>> seckillFallback() {
        return Mono.just(Result.fail(429, "Seckill traffic is too high. Please retry later."));
    }
}
