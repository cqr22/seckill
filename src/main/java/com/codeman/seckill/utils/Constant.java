package com.codeman.seckill.utils;

/**
 * Created by Kuzma on 2020/6/15.
 */
public final class Constant {

     /**
     * 规定不允许魔法值存在
     * 连接userId和goodsId 判断是否抢到
     */
     public static final  String LINKSTRING = "_";

    /**
     * 订单状态 -1：无效单 0：已下单未支付  1：已支付  2：已发货  3：已签收 TODO
     */
    public static final Integer ORDER_UNPAID = 0;
}
