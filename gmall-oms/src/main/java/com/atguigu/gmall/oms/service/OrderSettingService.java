package com.atguigu.gmall.oms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.oms.entity.OrderSettingEntity;

import java.util.Map;

/**
 * 订单配置信息
 *
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-06 00:39:03
 */
public interface OrderSettingService extends IService<OrderSettingEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

