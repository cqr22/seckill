package com.codeman.seckill.dto;

import java.time.LocalDateTime;

/**
 * Created by Kuzma on 2020/6/13.
 */
public class SeckillInfo {
    private Long seckillGoodsId;

    private String title;

    private Long price;

    private String description;

    private String imgUrl;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer stock;

    /**
     * 秒杀活动的状态 0：已过期 1：进行中 2：未开始
     */
    private Integer status;

    public Long getSeckillGoodsId() {
        return seckillGoodsId;
    }

    public void setSeckillGoodsId(Long seckillGoodsId) {
        this.seckillGoodsId = seckillGoodsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "SeckillInfo{" +
                "seckillGoodsId=" + seckillGoodsId +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", stock=" + stock +
                ", status=" + status +
                '}';
    }
}
