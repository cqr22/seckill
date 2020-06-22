package com.codeman.seckill.entity;

import java.time.LocalDateTime;

public class Stock {
    private Long goodsId;

    private Integer stock;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "goodsId=" + goodsId +
                ", stock=" + stock +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}