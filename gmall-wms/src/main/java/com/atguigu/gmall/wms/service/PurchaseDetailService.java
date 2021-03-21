package com.atguigu.gmall.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.wms.entity.PurchaseDetailEntity;

import java.util.Map;

/**
 * 
 *
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-06 00:16:09
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

