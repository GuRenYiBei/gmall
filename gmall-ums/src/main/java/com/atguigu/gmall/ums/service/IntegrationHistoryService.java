package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.ums.entity.IntegrationHistoryEntity;

import java.util.Map;

/**
 * 购物积分记录表
 *
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-06 00:03:02
 */
public interface IntegrationHistoryService extends IService<IntegrationHistoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

