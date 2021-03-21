package com.atguigu.gmall.sms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.sms.entity.CouponSpuEntity;

import java.util.Map;

/**
 * 优惠券与产品关联
 *
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-06 09:01:55
 */
public interface CouponSpuService extends IService<CouponSpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

