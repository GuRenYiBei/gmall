package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-05 23:01:30
 */
public interface BrandService extends IService<BrandEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

