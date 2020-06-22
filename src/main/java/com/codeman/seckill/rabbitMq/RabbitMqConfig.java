package com.codeman.seckill.rabbitMq;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitMq配置类
 * @author Kuzma
 * @date 2020/6/15
 */
@Configuration
public class RabbitMqConfig {

    public static final String SECKILL_QUEQUE = "seckill.queue";

    @Bean
    public Queue queue() {
        return new Queue(SECKILL_QUEQUE, true);
    }
}
