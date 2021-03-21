package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.sms.entity.SeckillSessionEntity;

import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-06 09:01:55
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

