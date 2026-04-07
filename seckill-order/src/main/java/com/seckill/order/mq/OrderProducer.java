package com.seckill.order.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seckill.common.dto.OrderMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendOrderCreated(OrderMessageDTO dto) {
        try {
            String msg = objectMapper.writeValueAsString(dto);
            kafkaTemplate.send("seckill-order-created", String.valueOf(dto.getUserId()), msg);
            log.info("发送订单创建消息: orderId={}", dto.getOrderId());
        } catch (Exception e) {
            log.error("发送订单创建消息失败", e);
        }
    }

    public void sendOrderPaid(OrderMessageDTO dto) {
        try {
            kafkaTemplate.send("seckill-order-paid", String.valueOf(dto.getUserId()),
                    objectMapper.writeValueAsString(dto));
        } catch (Exception e) {
            log.error("发送订单支付消息失败", e);
        }
    }

    public void sendOrderCancelled(OrderMessageDTO dto) {
        try {
            kafkaTemplate.send("seckill-order-cancelled", String.valueOf(dto.getUserId()),
                    objectMapper.writeValueAsString(dto));
        } catch (Exception e) {
            log.error("发送订单取消消息失败", e);
        }
    }
}
