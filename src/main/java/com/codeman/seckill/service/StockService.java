package com.codeman.seckill.service;

import com.codeman.seckill.entity.Stock;

/**
 *
 * @author Kuzma
 * @date 2020/6/15
 */
public interface StockService {

    boolean reduceStock(Long goodsId);

    boolean insertStock(Stock stock);

}
