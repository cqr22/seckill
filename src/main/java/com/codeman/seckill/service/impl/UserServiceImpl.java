package com.codeman.seckill.service.impl;

import com.codeman.seckill.dao.UserMapper;
import com.codeman.seckill.dto.UserInfo;
import com.codeman.seckill.entity.User;
import com.codeman.seckill.exception.GlobalException;
import com.codeman.seckill.result.CommonEnum;
import com.codeman.seckill.service.UserService;
import com.codeman.seckill.utils.MD5Utils;
import com.codeman.seckill.utils.SnowflakeIdUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Created by Kuzma on 2020/6/12.
 */
@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Override
    public User registerOrLogin(User userParam) throws Exception {
        User user = userMapper.selectByUserName(userParam.getUserName());
        if (user != null){
            // 密码不等
            if (!MD5Utils.getMd5Str(userParam.getPassword()).equals(user.getPassword())){
                throw new GlobalException(CommonEnum.USER_LOGIN_FAIL);
            }
        }else {
            // 走注册
            user = new User();
            user.setUserName(userParam.getUserName());
            // TODO 设置默认头像
            user.setFaceImg("");
            user.setPassword(MD5Utils.getMd5Str(userParam.getPassword()));
            // 先免费送你这么多钱
            user.setWallet(10000000d);
            // 使用雪花算法生成userId
            user.setUserId(SnowflakeIdUtil.getSnowflakeId());
            user.setCreateTime(LocalDateTime.now());
            insertUser(user);
        }
        return user;
    }

    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    private void insertUser(User user) {
        userMapper.insert(user);
    }
}
