package com.seckill.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic orderCreatedTopic() {
        return new NewTopic("seckill-order-created", 3, (short) 1);
    }

    @Bean
    public NewTopic orderPaidTopic() {
        return new NewTopic("seckill-order-paid", 3, (short) 1);
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return new NewTopic("seckill-order-cancelled", 3, (short) 1);
    }
}
