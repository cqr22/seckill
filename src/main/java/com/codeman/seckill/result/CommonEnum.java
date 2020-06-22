package com.codeman.seckill.result;

import com.codeman.seckill.exception.BaseErrorInfoInterface;

/**
 * Created by Kuzma on 2020/6/13.
 */
public enum CommonEnum implements BaseErrorInfoInterface {

    /**
     * 通用错误类型10001
     */
    PARAMETER_VALIDATION_ERROR("10001", "参数不合法"),
    UNKNOWN_ERROR("10002","未知错误"),

    USER_LOGIN_FAIL("20002","用户名或密码不正确"),
    STOCK_NOT_ENOUGH("30001","您要的宝贝都被抢光拉~"),
    REPEATE_SECKILL("30002", "您已经秒杀到您要的宝贝了~"),
    RATELIMIT("30003","活动太火爆，请稍后再试"),
    SECKILLING("30004","秒杀中...."),
    NOT_IN_SECKILLING("30005","不在秒杀时间内...."),

    SUCCESS("200", "成功!"),
    INTERNAL_SERVER_ERROR("500", "服务器内部错误!"),
    BODY_NOT_MATCH("400","请求的数据格式不符!"),

    ;

    /** 错误码 */
    private String resultCode;

    /** 错误描述 */
    private String resultMsg;

    CommonEnum(String resultCode, String resultMsg) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    @Override
    public String getResultCode() {
        return resultCode;
    }

    @Override
    public String getResultMsg() {
        return resultMsg;
    }

}
