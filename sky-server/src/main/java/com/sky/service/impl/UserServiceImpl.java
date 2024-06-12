package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import io.swagger.util.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    private final WeChatProperties weChatProperties;
    private final UserMapper userMapper;
    /**
     * 微信登录
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {

        //判断openid是否为空，如果为空返回登录失败，抛出业务异常
        String openid = getOpenid(userLoginDTO.getCode());
        if (Objects.isNull(openid)) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //当前用户是否为新用户，如果是新用户，自动完成注册，返回这个用户对象
        User user = userMapper.getByOpenId(openid);
        if (Objects.isNull(user)) {
            user = User.builder().openid(openid)
                    .createTime(LocalDateTime.now()).build();
            userMapper.insert(user);

        }
        return user;
    }

    private String getOpenid(String code) {
        //调用微信API，获取openid
        Map<String, String> loginMap = new HashMap<>();
        loginMap.put("appid", weChatProperties.getAppid());
        loginMap.put("secret", weChatProperties.getSecret());
        loginMap.put("js_code", code);
        loginMap.put("grant_type", "authorization_code");
        String jsonStr = HttpClientUtil.doGet(WX_LOGIN, loginMap);
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
