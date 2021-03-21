package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.sms.entity.CouponHistoryEntity;

import java.util.Map;

/**
 * 优惠券领取历史记录
 *
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-06 09:01:55
 */
public interface CouponHistoryService extends IService<CouponHistoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

