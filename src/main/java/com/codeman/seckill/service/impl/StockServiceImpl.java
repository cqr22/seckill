package com.codeman.seckill.service.impl;

import com.codeman.seckill.dao.StockMapper;
import com.codeman.seckill.entity.Stock;
import com.codeman.seckill.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Created by Kuzma on 2020/6/15.
 */
@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockMapper stockMapper;

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public boolean reduceStock(Long goodsId) {
        return stockMapper.reduceStock(goodsId, LocalDateTime.now()) == 1;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public boolean insertStock(Stock stock) {
        return stockMapper.insert(stock)==1;
    }
}
