package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuFullReductionMapper skuFullReductionMapper;

    @Autowired
    private SkuLadderMapper skuLadderMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Transactional
    @Override
    public void saveSales(SkuSaleVo skuSaleVo) {
        //3、保存sku营销信息
        //保存sms_sku_bounds
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVo, skuBoundsEntity);
        List<Integer> works = skuSaleVo.getWork();
        if (!CollectionUtils.isEmpty(works)) {
            skuBoundsEntity.setWork(works.get(3) * 8 + works.get(2) * 4 + works.get(1) * 2 + works.get(0));
        }
        this.save(skuBoundsEntity);

        //保存sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVo, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuSaleVo.getFullAddOther());
        skuFullReductionMapper.insert(skuFullReductionEntity);

        //保存sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVo, skuLadderEntity);
        skuLadderEntity.setAddOther(skuSaleVo.getLadderAddOther());
        skuLadderMapper.insert(skuLadderEntity);
    }

    @Override
    public List<ItemSaleVo> querySalesBySkuId(String skuId) {

        List<ItemSaleVo> itemSaleVos = new ArrayList<>();

        //查询积分情况
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if (skuBoundsEntity != null) {
            ItemSaleVo boundsSaleVo = new ItemSaleVo();
            boundsSaleVo.setType("积分");
            boundsSaleVo.setDesc("送"+skuBoundsEntity.getBuyBounds()+"购物积分，送"+skuBoundsEntity.getGrowBounds()+"成长积分");
            itemSaleVos.add(boundsSaleVo);
        }

        //查询满减情况
        SkuFullReductionEntity skuFullReductionEntity = this.skuFullReductionMapper.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
        if (skuFullReductionEntity != null) {
            ItemSaleVo reductionSaleVo = new ItemSaleVo();
            reductionSaleVo.setType("满减");
            reductionSaleVo.setDesc("满"+skuFullReductionEntity.getFullPrice()+"减" + skuFullReductionEntity.getReducePrice());
            itemSaleVos.add(reductionSaleVo);
        }

        //查询打折情况
        SkuLadderEntity skuLadderEntity = this.skuLadderMapper.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if (skuLadderEntity != null) {
            ItemSaleVo ladderSaleVo = new ItemSaleVo();
            ladderSaleVo.setType("打折");
            ladderSaleVo.setDesc("买"+skuLadderEntity.getFullCount()+"件打"+skuLadderEntity.getDiscount().divide(new BigDecimal(10))+"折");
            itemSaleVos.add(ladderSaleVo);
        }
        return itemSaleVos;
    }

}