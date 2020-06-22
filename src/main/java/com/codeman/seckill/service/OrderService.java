package com.codeman.seckill.service;

import com.codeman.seckill.rabbitMq.SeckillMessage;

/**
 *
 * @author Kuzma
 * @date 2020/6/15
 */
public interface OrderService {

    void createOrder(SeckillMessage seckillMessage);

}
