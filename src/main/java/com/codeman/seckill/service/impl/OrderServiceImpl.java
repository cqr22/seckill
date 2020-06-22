package com.codeman.seckill.service.impl;

import com.codeman.seckill.dao.OrderInfoMapper;
import com.codeman.seckill.entity.OrderInfo;
import com.codeman.seckill.rabbitMq.SeckillMessage;
import com.codeman.seckill.service.OrderService;
import com.codeman.seckill.utils.Constant;
import com.codeman.seckill.utils.SnowflakeIdUtil;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kuzma on 2020/6/15.
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void createOrder(SeckillMessage seckillMessage) {
        OrderInfo orderInfo = new OrderInfo();
        // 使用雪花算法生成订单id
        orderInfo.setOrderId(SnowflakeIdUtil.getSnowflakeId());
        // 商品id
        orderInfo.setGoodsId(seckillMessage.getGoodsId());
        // 购买者id
        orderInfo.setUserId(seckillMessage.getUserId());
        // 购买数量
        orderInfo.setOrderQuantity(seckillMessage.getOrderQuantity());
        // 单价
        orderInfo.setUnitPrice(seckillMessage.getUnitPrice());
        // 订单状态 为已下单未支付  TODO：用户15分钟内未支付取消 思路：在redis里面用map存放为支付的订单 同时启动定时任务
        orderInfo.setOrderStatus(Constant.ORDER_UNPAID);
        // 收货地址id
        orderInfo.setReceiveId(seckillMessage.getReceiveId());
        // 发货地址id
        orderInfo.setShipId(seckillMessage.getShipId());
        // 每一次对订单的操作都要记录
        orderInfo.setUpdateTime(LocalDateTime.now());
        orderInfo.setCreateTime(LocalDateTime.now());
        orderInfoMapper.insert(orderInfo);
        // 标记已经秒杀
        redisTemplate.opsForValue().set(orderInfo.getUserId() + Constant.LINKSTRING + seckillMessage.getSeckillId(),true);
    }
}
