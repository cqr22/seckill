package com.codeman.seckill.service.impl;

import com.codeman.seckill.dao.SeckillGoodsMapper;
import com.codeman.seckill.dto.SeckillInfo;
import com.codeman.seckill.entity.SeckillGoods;
import com.codeman.seckill.entity.Stock;
import com.codeman.seckill.exception.GlobalException;
import com.codeman.seckill.rabbitMq.SeckillMessage;
import com.codeman.seckill.result.CommonEnum;
import com.codeman.seckill.service.OrderService;
import com.codeman.seckill.service.SeckillService;
import com.codeman.seckill.service.StockService;
import com.codeman.seckill.utils.SnowflakeIdUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kuzma on 2020/6/13.
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;


    @Override
    public List<SeckillInfo> listAllSeckill() {
        List<SeckillInfo> seckillInfoList =  seckillGoodsMapper.listAllSeckill();
        for (SeckillInfo s:seckillInfoList
             ) {
            LocalDateTime now = LocalDateTime.now();
            if(s.getStartDate().isAfter(now)){
                s.setStatus(2);
            }else if (s.getEndDate().isBefore(now)){
                s.setStatus(0);
            }else {
                s.setStatus(1);
            }
        }
        return seckillInfoList;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void seckillGoods(SeckillMessage seckillMessage) {
        try {
            // 减库存成功 去下订单
            if(stockService.reduceStock(seckillMessage.getGoodsId())){
                orderService.createOrder(seckillMessage);
            }
        }catch (Exception e){
            // 手动回滚异常 加回库存
            redisTemplate.opsForValue().increment(String.valueOf(seckillMessage.getGoodsId()),1);
            System.out.println("进来了回滚");
        }
    }

    @Override
    public String getSeckillToken(Long seckillId, Long goodsId, Long userId) {

        // 判断对应的秒杀商品是否已卖完
        if ((int)redisTemplate.opsForValue().get(goodsId) < 0){
            throw new GlobalException(CommonEnum.STOCK_NOT_ENOUGH);
        }

        SeckillInfo seckillInfo = (SeckillInfo) redisTemplate.opsForValue().get(goodsId+"_info");
        if (seckillInfo == null){
            throw new GlobalException(CommonEnum.PARAMETER_VALIDATION_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();
        // 判断秒杀时间是否合理
        if (seckillInfo.getStartDate().isAfter(now) || seckillInfo.getEndDate().isBefore(now)){
            throw new GlobalException(CommonEnum.NOT_IN_SECKILLING);
        }

        // 获取该商品秒杀大闸的数量
        long num = redisTemplate.opsForValue().increment("seckill_door_"+seckillId,-1);
        if (num < 0){
            // 实际上已经拿不到令牌了 但是友好提示一下 不要让用户知道他被淘汰了
            throw  new GlobalException(CommonEnum.SECKILLING);
        }

        // 生成秒杀Token，设置有效时间5分钟
        String token = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set("seckill_token_"+seckillId+"_"+userId,token,5, TimeUnit.MINUTES);

        return token;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void publishSeckillPromo(SeckillInfo seckillInfo, String userId) {
        SeckillGoods seckillGoods = new SeckillGoods();
        seckillGoods.setSeckillId(SnowflakeIdUtil.getSnowflakeId());
        seckillGoods.setCreateTime(LocalDateTime.now());
        seckillGoods.setUpdateTime(LocalDateTime.now());
        seckillGoods.setSeckillGoodsId(seckillInfo.getSeckillGoodsId());
        seckillGoods.setStartDate(seckillInfo.getStartDate());
        seckillGoods.setEndDate(seckillInfo.getEndDate());
        insertSeckillGoods(seckillGoods);

        Stock stock = new Stock();
        stock.setGoodsId(seckillInfo.getSeckillGoodsId());
        stock.setStock(seckillInfo.getStock());
        stock.setCreateTime(LocalDateTime.now());
        stock.setUpdateTime(LocalDateTime.now());

        // 不立即插入到redis 由脚本处理 另外由脚本载入秒杀大闸的限制数

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean insertSeckillGoods(SeckillGoods seckillGoods) {
        return seckillGoodsMapper.insert(seckillGoods)==1;
    }
}
