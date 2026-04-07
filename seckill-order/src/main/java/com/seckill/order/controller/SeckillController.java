package com.seckill.order.controller;

import com.seckill.common.dto.Result;
import com.seckill.common.dto.SeckillRequestDTO;
import com.seckill.common.entity.Order;
import com.seckill.order.service.SeckillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    /** 秒杀下单 */
    @PostMapping("/seckill")
    public Result<Long> seckill(@Valid @RequestBody SeckillRequestDTO dto) {
        return seckillService.doSeckill(dto);
    }

    /** 查询订单 */
    @GetMapping("/{orderId}")
    public Result<Order> getOrder(@PathVariable Long orderId) {
        return seckillService.getOrder(orderId);
    }

    /** 按用户查询订单 */
    @GetMapping("/user/{userId}")
    public Result<List<Order>> getUserOrders(@PathVariable Long userId) {
        return seckillService.getUserOrders(userId);
    }

    /** 模拟支付 */
    @PostMapping("/{orderId}/pay")
    public Result<Void> pay(@PathVariable Long orderId) {
        return seckillService.payOrder(orderId);
    }

    /** 取消订单 */
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancel(@PathVariable Long orderId) {
        return seckillService.cancelOrder(orderId);
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("Order Service is running on port ${server.port}");
    }
}
