package com.seckill.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seckill.common.dto.OrderMessageDTO;
import com.seckill.common.dto.Result;
import com.seckill.common.dto.SeckillRequestDTO;
import com.seckill.common.entity.Order;
import com.seckill.common.enums.OrderStatus;
import com.seckill.common.exception.BizException;
import com.seckill.common.util.SnowflakeIdGenerator;
import com.seckill.order.mapper.OrderMapper;
import com.seckill.order.mq.OrderProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillService {

    private final OrderMapper orderMapper;
    private final StringRedisTemplate redisTemplate;
    private final SnowflakeIdGenerator idGenerator;
    private final OrderProducer orderProducer;

    private static final String STOCK_KEY = "seckill:stock:";
    private static final String SECKILL_USER_KEY = "seckill:user:";

    /**
     * 秒杀下单核心流程:
     * 1. Redis幂等性检查（同一用户同一商品只能秒杀一次）
     * 2. Redis预扣减库存（Lua脚本原子操作）
     * 3. 生成订单ID（雪花算法 + 基因法）
     * 4. 发送Kafka消息，异步创建订单和扣减DB库存（削峰填谷）
     */
    public Result<Long> doSeckill(SeckillRequestDTO dto) {
        Long userId = dto.getUserId();
        Long productId = dto.getProductId();

        // Lua脚本: 幂等检查 + 原子扣减
        String luaScript = """
            local userKey = KEYS[2]
            if redis.call('sismember', userKey, ARGV[1]) == 1 then
                return -2
            end
            local stock = tonumber(redis.call('get', KEYS[1]))
            if stock == nil or stock <= 0 then
                return -1
            end
            redis.call('decr', KEYS[1])
            redis.call('sadd', userKey, ARGV[1])
            return stock - 1
            """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = redisTemplate.execute(script,
                List.of(STOCK_KEY + productId, SECKILL_USER_KEY + productId),
                String.valueOf(userId));

        if (result == null || result == -1) {
            throw new BizException("商品已售罄");
        }
        if (result == -2) {
            throw new BizException("您已参与过该商品的秒杀");
        }

        // 生成订单ID（基因法：嵌入userId低位，便于分库分表路由）
        long orderId = idGenerator.nextIdWithGene(userId);

        // 异步发送Kafka消息
        OrderMessageDTO msg = new OrderMessageDTO();
        msg.setOrderId(orderId);
        msg.setUserId(userId);
        msg.setProductId(productId);
        msg.setProductName("秒杀商品-" + productId); // 实际应查询商品信息
        msg.setPrice(BigDecimal.ZERO); // 实际应查询秒杀价格
        msg.setQuantity(1);
        orderProducer.sendOrderCreated(msg);

        log.info("秒杀成功，订单异步创建中: orderId={}, userId={}, productId={}", orderId, userId, productId);
        return Result.ok(orderId);
    }

    /**
     * 查询订单
     */
    public Result<Order> getOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        return order != null ? Result.ok(order) : Result.fail("订单不存在");
    }

    /**
     * 按用户ID查询订单
     */
    public Result<List<Order>> getUserOrders(Long userId) {
        List<Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreateTime));
        return Result.ok(orders);
    }

    /**
     * 模拟支付
     */
    @Transactional
    public Result<Void> payOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BizException("订单不存在");
        if (order.getStatus() != OrderStatus.UNPAID.getCode()) throw new BizException("订单状态不正确");

        order.setStatus(OrderStatus.PAID.getCode());
        order.setPayTime(java.time.LocalDateTime.now());
        orderMapper.updateById(order);

        // 发送支付成功消息 -> 库存确认扣减
        OrderMessageDTO msg = new OrderMessageDTO();
        msg.setOrderId(orderId);
        msg.setUserId(order.getUserId());
        msg.setProductId(order.getProductId());
        msg.setQuantity(order.getQuantity());
        orderProducer.sendOrderPaid(msg);

        return Result.ok();
    }

    /**
     * 取消订单
     */
    @Transactional
    public Result<Void> cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BizException("订单不存在");
        if (order.getStatus() != OrderStatus.UNPAID.getCode()) throw new BizException("订单状态不正确");

        order.setStatus(OrderStatus.CANCELLED.getCode());
        orderMapper.updateById(order);

        // 发送取消消息 -> 库存回滚
        OrderMessageDTO msg = new OrderMessageDTO();
        msg.setOrderId(orderId);
        msg.setUserId(order.getUserId());
        msg.setProductId(order.getProductId());
        msg.setQuantity(order.getQuantity());
        orderProducer.sendOrderCancelled(msg);

        return Result.ok();
    }
}
