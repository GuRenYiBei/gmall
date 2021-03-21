package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.GroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-05 23:01:30
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrGroupEntity> queryAttrGroupWithattrsByCatId(Long catId);

    List<GroupVo> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId);
}

