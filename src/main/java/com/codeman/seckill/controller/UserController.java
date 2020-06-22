package com.codeman.seckill.controller;

import com.codeman.seckill.dto.UserInfo;
import com.codeman.seckill.entity.User;
import com.codeman.seckill.exception.GlobalException;
import com.codeman.seckill.result.CommonEnum;
import com.codeman.seckill.result.ResultBody;
import com.codeman.seckill.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * Created by Kuzma on 2020/6/12.
 */
@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 登录注册接口 账号名有则登录 无则注册
     * @param user
     * @return
     */
    @PostMapping("/register_or_login")
    public ResultBody registerOrLogin(@RequestBody User user, HttpServletRequest request) throws Exception {
        if (Objects.isNull(user)|| Objects.isNull(user.getUserName())||Objects.isNull(user.getPassword())){
            throw new GlobalException(CommonEnum.PARAMETER_VALIDATION_ERROR);
        }

        User result = userService.registerOrLogin(user);

        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(result,userInfo);

        // 使用spring-session
        request.getSession().setAttribute("user",result);
        return ResultBody.success(userInfo);
    }


}
