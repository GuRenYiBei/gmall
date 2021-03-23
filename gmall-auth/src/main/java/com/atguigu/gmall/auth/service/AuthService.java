package com.atguigu.gmall.auth.service;

import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.UserException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/21/18:00
 * @Description:
 ******************************************/
@EnableConfigurationProperties(JwtProperties.class)
@Service
public class AuthService {

    @Autowired
    GmallUmsClient umsClient;

    @Autowired
    private JwtProperties jwtProperties;
    
    public void login(String loginName, String password, HttpServletRequest request, HttpServletResponse response) {
        ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUser(loginName, password);
        UserEntity userEntity = userEntityResponseVo.getData();
        if (userEntity == null) {
            throw new UserException("用户名或密码输入有误！");
        }

        try {
            //生成token
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userEntity.getId());
            map.put("userName", userEntity.getUsername());
            map.put("ip", IpUtils.getIpAddressAtService(request));
            String token = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(), jwtProperties.getExpire());

            //将jwt放到cookie中
            CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), token, jwtProperties.getExpire()*60);

            //将用户昵称也放到cookie中用来显示登录后的用户名
            CookieUtils.setCookie(request, response, jwtProperties.getNickName(), userEntity.getNickname(), jwtProperties.getExpire()*60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
