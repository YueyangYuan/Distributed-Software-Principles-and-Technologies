package com.seckill.inventory.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seckill.common.dto.OrderMessageDTO;
import com.seckill.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryConsumer {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    /**
     * 监听订单创建消息，执行DB库存扣减
     */
    @KafkaListener(topics = "seckill-order-created", groupId = "inventory-group")
    public void onOrderCreated(String message) {
        try {
            OrderMessageDTO dto = objectMapper.readValue(message, OrderMessageDTO.class);
            log.info("收到订单创建消息: orderId={}, productId={}", dto.getOrderId(), dto.getProductId());

            boolean success = inventoryService.deductStockInDB(dto.getProductId(), dto.getQuantity());
            if (success) {
                log.info("DB库存扣减成功: productId={}", dto.getProductId());
            } else {
                log.error("DB库存扣减失败: productId={}", dto.getProductId());
                // 此处可发送补偿消息
            }
        } catch (Exception e) {
            log.error("处理库存扣减消息失败", e);
        }
    }

    /**
     * 监听订单支付成功，确认库存扣减
     */
    @KafkaListener(topics = "seckill-order-paid", groupId = "inventory-group")
    public void onOrderPaid(String message) {
        try {
            OrderMessageDTO dto = objectMapper.readValue(message, OrderMessageDTO.class);
            inventoryService.confirmDeduct(dto.getProductId(), dto.getQuantity());
            log.info("库存确认扣减: productId={}", dto.getProductId());
        } catch (Exception e) {
            log.error("处理库存确认消息失败", e);
        }
    }

    /**
     * 监听订单取消，回滚库存
     */
    @KafkaListener(topics = "seckill-order-cancelled", groupId = "inventory-group")
    public void onOrderCancelled(String message) {
        try {
            OrderMessageDTO dto = objectMapper.readValue(message, OrderMessageDTO.class);
            inventoryService.rollbackStock(dto.getProductId(), dto.getQuantity());
            log.info("库存回滚成功: productId={}", dto.getProductId());
        } catch (Exception e) {
            log.error("处理库存回滚消息失败", e);
        }
    }
}
