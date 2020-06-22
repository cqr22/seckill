package com.codeman.seckill.service;

import com.codeman.seckill.dto.SeckillInfo;
import com.codeman.seckill.entity.SeckillGoods;
import com.codeman.seckill.rabbitMq.SeckillMessage;

import java.util.List;

/**
 * Created by Kuzma on 2020/6/13.
 */
public interface SeckillService {

    List<SeckillInfo> listAllSeckill();

    void seckillGoods(SeckillMessage seckillMessage);

    String getSeckillToken(Long seckillId, Long goodsId, Long userId);

    void publishSeckillPromo(SeckillInfo seckillInfo, String userId);

    boolean insertSeckillGoods(SeckillGoods seckillGoods);
}
