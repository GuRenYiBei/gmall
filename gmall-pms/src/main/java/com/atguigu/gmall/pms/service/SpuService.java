package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-05 23:01:30
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo querySpuByCategoryId(Long categoryId, PageParamVo pageParamVo);

    void bigSave(SpuVo spuVo);
}

