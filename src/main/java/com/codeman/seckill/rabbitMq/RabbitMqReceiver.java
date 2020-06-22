package com.codeman.seckill.rabbitMq;

import com.codeman.seckill.dao.StockMapper;
import com.codeman.seckill.entity.Stock;
import com.codeman.seckill.exception.GlobalException;
import com.codeman.seckill.result.CommonEnum;
import com.codeman.seckill.service.SeckillService;
import com.codeman.seckill.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * @author Kuzma
 * @date 2020/6/15
 */
@Service
public class RabbitMqReceiver {

    private static Logger logger = LoggerFactory.getLogger(RabbitMqReceiver.class);

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @RabbitListener(queues = RabbitMqConfig.SECKILL_QUEQUE)
    public void receive(SeckillMessage message){
        Long userId = message.getUserId();
        Long goodsId = message.getGoodsId();

        // 再去判断一次是否抢到了 抢到了就不给他继续抢了
        if (redisTemplate.hasKey(userId + Constant.LINKSTRING + goodsId)){
            throw new GlobalException(CommonEnum.REPEATE_SECKILL);
        }

        // 去数据库判断库存
        Stock stock = stockMapper.selectByPrimaryKey(message.getGoodsId());
        if (stock.getStock() <= 0){
            throw new GlobalException(CommonEnum.STOCK_NOT_ENOUGH);
        }

        // 事务执行 减库存 下订单
        seckillService.seckillGoods(message);
    }
}
