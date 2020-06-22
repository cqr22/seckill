package com.codeman.seckill.dao;

import com.codeman.seckill.entity.Stock;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface StockMapper {
    int deleteByPrimaryKey(Long goodsId);

    int insert(Stock record);

    int insertSelective(Stock record);

    Stock selectByPrimaryKey(Long goodsId);

    int updateByPrimaryKeySelective(Stock record);

    int updateByPrimaryKey(Stock record);
    
    int reduceStock(@Param("goodsId") Long goodsId ,@Param("updateTime") LocalDateTime updateTime);
}