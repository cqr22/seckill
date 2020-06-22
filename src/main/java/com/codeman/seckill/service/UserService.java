package com.codeman.seckill.service;

import com.codeman.seckill.entity.User;


/**
 * Created by Kuzma on 2020/6/12.
 */
public interface UserService {
    /**
     * 登录注册接口 账号名有则登录 无则注册
     * @param user
     * @return
     */
    User registerOrLogin(User user) throws Exception;
}
