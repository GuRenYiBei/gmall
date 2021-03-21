package com.atguigu.gmall.ums.service.impl;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import org.springframework.util.CollectionUtils;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    UserMapper userMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                queryWrapper.eq("username", data);
                break;
            case 2:
                queryWrapper.eq("phone", data);
                break;
            case 3:
                queryWrapper.eq("email", data);
                break;
            default:
                return null;
        }
        return this.count(queryWrapper) == 0;
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        //TODO:1、验证短信验证码

        //生成盐
        String random = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        userEntity.setSalt(random);
        //加密加盐
        userEntity.setPassword(DigestUtils.md5Hex(userEntity.getPassword() + random));
        userEntity.setIntegration(1000);
        userEntity.setGrowth(1000);
        userEntity.setStatus(1);
        userEntity.setLevelId(1l);
        userEntity.setNickname(userEntity.getUsername());
        userEntity.setCreateTime(new Date());

        //注册用户
        this.save(userEntity);
        //TODO:删除验证码

    }

    @Override
    public UserEntity queryUser(String loginName, String password) {

        List<UserEntity> userEntities = this.list(new QueryWrapper<UserEntity>().or(wrapper -> wrapper.eq("username", loginName).or().eq("phone", loginName).or().eq("email", loginName)));
        if (CollectionUtils.isEmpty(userEntities)) {
            return null;
        }

        for (UserEntity userEntity : userEntities) {
            String pwd = DigestUtils.md5Hex(password + userEntity.getSalt());
            if (StringUtils.equals(pwd, userEntity.getPassword())) {
                return userEntity;
            }
        }
        return null;
    }

}