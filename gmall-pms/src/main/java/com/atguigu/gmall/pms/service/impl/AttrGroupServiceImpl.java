package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.GroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrMapper attrMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SpuAttrValueMapper spuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrGroupEntity> queryAttrGroupWithattrsByCatId(Long catId) {
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        // 根据分类的Id查询Attr组
        queryWrapper.eq("category_id", catId);
        List<AttrGroupEntity> groupEntities = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(groupEntities)) {
            return null;
        }
        //遍历attr组查询具体的attr
        groupEntities.forEach(group -> {
            List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", group.getId())
                    .eq("type", 1));
            group.setAttrEntities(attrEntities);
        });
        return groupEntities;
    }

    @Override
    public List<GroupVo> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId) {

        //1、根据分类ID查询分组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        if (CollectionUtils.isEmpty(attrGroupEntities)) {
            return null;
        }
        //2、遍历分组，查询分组下的规格参数
        return attrGroupEntities.stream().map(attrGroupEntity -> {

            GroupVo groupVo = new GroupVo();
            groupVo.setGroupId(attrGroupEntity.getId());
            groupVo.setGroupName(attrGroupEntity.getName());

            List<AttrValueVo> attrValueVos = new ArrayList<>();
            List<AttrEntity> attrEntities = this.attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()));
            if (!CollectionUtils.isEmpty(attrEntities)) {
                List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
                //3、从sku_attr_value中查询特殊规格参数的值
                List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().in("attr_id", attrIds).eq("sku_id", skuId));
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                    attrValueVos.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        BeanUtils.copyProperties(skuAttrValueEntity, attrValueVo);
                        return attrValueVo;
                    }).collect(Collectors.toList()));
                }
                //4、从spu_attr_value中查询特殊规格参数的值
                List<SpuAttrValueEntity> spuAttrValueEntities = this.spuAttrValueMapper.selectList(new QueryWrapper<SpuAttrValueEntity>().in("attr_id", attrIds).eq("spu_id", spuId));
                if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                    attrValueVos.addAll(skuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        AttrValueVo attrValueVo = new AttrValueVo();
                        BeanUtils.copyProperties(spuAttrValueEntity, attrValueVo);
                        return attrValueVo;
                    }).collect(Collectors.toList()));
                }
            }
            groupVo.setAttrs(attrValueVos);
            return groupVo;
        }).collect(Collectors.toList());
    }

}