package com.codeman.seckill.rabbitMq;

import com.codeman.seckill.entity.OrderInfo;

import java.io.Serializable;

/**
 *
 * @author Kuzma
 * @date 2020/6/15
 */
public class SeckillMessage implements Serializable{
    private Long seckillId;

    private Long userId;

    private Long goodsId;

    private Long shipId;

    private Long receiveId;

    private Long unitPrice;

    private Integer orderQuantity;

    public SeckillMessage() {
    }

    public SeckillMessage (OrderInfo orderInfo) {
        this.seckillId = orderInfo.getSeckillId();
        this.userId = orderInfo.getUserId();
        this.goodsId = orderInfo.getGoodsId();
        this.shipId = orderInfo.getShipId();
        this.receiveId = orderInfo.getReceiveId();
        this.unitPrice = orderInfo.getUnitPrice();
        this.orderQuantity = orderInfo.getOrderQuantity();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Long getShipId() {
        return shipId;
    }

    public void setShipId(Long shipId) {
        this.shipId = shipId;
    }

    public Long getReceiveId() {
        return receiveId;
    }

    public void setReceiveId(Long receiveId) {
        this.receiveId = receiveId;
    }

    public Long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Long unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getOrderQuantity() {
        return orderQuantity;
    }

    public void setOrderQuantity(Integer orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public Long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(Long seckillId) {
        this.seckillId = seckillId;
    }

    @Override
    public String toString() {
        return "SeckillMessage{" +
                "seckillId=" + seckillId +
                ", userId=" + userId +
                ", goodsId=" + goodsId +
                ", shipId=" + shipId +
                ", receiveId=" + receiveId +
                ", unitPrice=" + unitPrice +
                ", orderQuantity=" + orderQuantity +
                '}';
    }
}
