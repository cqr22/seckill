package com.codeman.seckill.dao;

import com.codeman.seckill.dto.SeckillInfo;
import com.codeman.seckill.entity.SeckillGoods;

import java.util.List;

public interface SeckillGoodsMapper {
    int deleteByPrimaryKey(Long seckillId);

    int insert(SeckillGoods record);

    int insertSelective(SeckillGoods record);

    SeckillGoods selectByPrimaryKey(Long seckillId);

    int updateByPrimaryKeySelective(SeckillGoods record);

    int updateByPrimaryKey(SeckillGoods record);

    List<SeckillInfo> listAllSeckill();
}