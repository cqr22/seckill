package com.codeman.seckill.rabbitMq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Kuzma
 * @date 2020/6/15
 */
@Service
public class RabbitMqSender {

    private static Logger logger = LoggerFactory.getLogger(RabbitMqSender.class);

    @Autowired
    AmqpTemplate amqpTemplate;

    public void sendSeckillMessage(SeckillMessage  seckillMessage){
        amqpTemplate.convertAndSend(RabbitMqConfig.SECKILL_QUEQUE, seckillMessage);
    }
}
