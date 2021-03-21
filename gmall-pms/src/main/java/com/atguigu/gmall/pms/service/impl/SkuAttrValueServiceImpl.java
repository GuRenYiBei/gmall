package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    AttrMapper attrMapper;

    @Autowired
    SkuMapper skuMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySearchAttrValueBySkuIdAndCid(Long skuId, Long cid) {
        //获取所有的attr信息，通过attr获取id
        List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("search_type", 1).eq("category_id", cid));
        if (CollectionUtils.isEmpty(attrEntities)) {
            return null;
        }
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

        //通过skuid和attrid共同查询
        return this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId).in("attr_id", attrIds));
    }

    @Override
    public List<SaleAttrValueVo> querySaleAttrValueBySpuId(String spuId) {
        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();

        List<SkuEntity> skuEntities = this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if (CollectionUtils.isEmpty(skuEntities)) {
            return null;
        }
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());

        //查询sku销售属性
        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuIds));
        if (CollectionUtils.isEmpty(skuAttrValueEntities)) {
            return null;
        }

        //对所有销售属性以attrid分组
        //分组后的map以attrId作为key，以List<SkuAttrValueEntity>为value
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));

        map.forEach((attrId,attrValueEntities)->{
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(attrId);
            if (!CollectionUtils.isEmpty(attrValueEntities)) {
                saleAttrValueVo.setAttrName(attrValueEntities.get(0).getAttrName());
                Set<String> attrValues = attrValueEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet());
                saleAttrValueVo.setAttrValues(attrValues);
            }
            saleAttrValueVos.add(saleAttrValueVo);
        });
        return saleAttrValueVos;
    }

    @Override
    public String querySalesAttrValueMappingSpuId(Long spuId) {
        List<Map<String, Object>> maps = this.skuAttrValueMapper.querySalesAttrValueMappingSpuId(spuId);
        if (CollectionUtils.isEmpty(maps)) {
            return null;
        }
        Map<String, Long> collect = maps.stream().collect(Collectors.toMap(map -> map.get("attr_values").toString(), map ->(Long) map.get("sku_id")));
        return JSON.toJSONString(collect);
    }

}