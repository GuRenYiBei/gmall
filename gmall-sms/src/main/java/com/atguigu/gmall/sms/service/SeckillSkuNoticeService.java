package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.sms.entity.SeckillSkuNoticeEntity;

import java.util.Map;

/**
 * 秒杀商品通知订阅
 *
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-06 09:01:55
 */
public interface SeckillSkuNoticeService extends IService<SeckillSkuNoticeEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

