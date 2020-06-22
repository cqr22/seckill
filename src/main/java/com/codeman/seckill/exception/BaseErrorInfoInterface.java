package com.codeman.seckill.exception;

/**
 * Created by Kuzma on 2020/6/13.
 */
public interface BaseErrorInfoInterface {
    /** 错误码*/
    String getResultCode();

    /** 错误描述*/
    String getResultMsg();
}
