package com.seckill.order.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seckill.common.dto.OrderMessageDTO;
import com.seckill.common.entity.Order;
import com.seckill.common.enums.OrderStatus;
import com.seckill.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;

    /**
     * 消费秒杀消息，异步创建订单（削峰填谷）
     */
    @KafkaListener(topics = "seckill-order-created", groupId = "order-group")
    public void onOrderCreated(String message) {
        try {
            OrderMessageDTO dto = objectMapper.readValue(message, OrderMessageDTO.class);
            log.info("消费订单创建消息: orderId={}", dto.getOrderId());

            // 幂等检查
            Order existing = orderMapper.selectById(dto.getOrderId());
            if (existing != null) {
                log.warn("订单已存在，跳过: orderId={}", dto.getOrderId());
                return;
            }

            Order order = new Order();
            order.setId(dto.getOrderId());
            order.setUserId(dto.getUserId());
            order.setProductId(dto.getProductId());
            order.setProductName(dto.getProductName());
            order.setOrderPrice(dto.getPrice());
            order.setQuantity(dto.getQuantity());
            order.setStatus(OrderStatus.UNPAID.getCode());
            orderMapper.insert(order);

            log.info("订单创建成功: orderId={}", dto.getOrderId());
        } catch (Exception e) {
            log.error("创建订单失败", e);
        }
    }
}
