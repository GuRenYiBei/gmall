package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.SmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;

import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {



    @Autowired
    private SpuAttrValueService spuAttrValueService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private SmsClient smsClient;

    @Autowired
    private SpuDescService spuDescService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuByCategoryId(Long categoryId, PageParamVo pageParamVo) {
        //判断是否选择了三级分类，如果没有选择则查全站，如果选择了就加上查询条件
        QueryWrapper<SpuEntity> queryWrapper = new QueryWrapper<>();
        if (categoryId != 0) {
            queryWrapper.eq("category_id", categoryId);
        }
        //判断用户是否输入了查询条件
        String key = pageParamVo.getKey();
        //如果使用isEmpty（）则不会识别空格；而使用isNotBlank则还会判断是否是空格
        if (StringUtils.isNotBlank(key)) {
            //and方法的作用就是在进行下一次查询的条件前拼接了一个（）
            //t 就是queryWrapper对象，因为queryWrapper.eq("id", key).or().like("name", key)是consumer的泛型
            // 即T ，所以t就是queryWrapper
            // t -> t.eq("id", key).or().like("name", key)
            queryWrapper.and(t -> t.eq("id", key).or().like("name", key));
//            queryWrapper.and();

        }

        //查询返需要的是一个Ipage对象，getPage（）就是将查询条件封装为一个Ipage对象
        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                queryWrapper
        );
        //查询返回的结果是一个Ipage对象，PageResultVo的构造器可以将其解析为我们需要的PageResultVo对象
        return new PageResultVo(page);
    }

    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spuVo) {
        //1、保存spu信息
        Long spuVoId = saveSpu(spuVo);

        //1.1保存spu_desc信息
        this.spuDescService.saveSpuDesc(spuVo, spuVoId);
        //1.2保存spu_attr_value信息
        saveBaseAttr(spuVo, spuVoId);

        //2、保存sku信息
        saveSkus(spuVo, spuVoId);

//        int i = 1 / 0;
        this.rabbitTemplate.convertSendAndReceive("PMS_SPU_EXCHANGE","item.insert",spuVoId);

    }


    public void saveSkus(SpuVo spuVo, Long spuVoId) {
        List<SkuVo> skuVos = spuVo.getSkus();
        if (CollectionUtils.isEmpty(skuVos)) {
            return;
        }

        skuVos.forEach(skuVo -> {
            //表中需要的CategoryId、BrandId页面没有携带，所以需要自己从spu中获取
            skuVo.setCategoryId(spuVo.getCategoryId());
            skuVo.setBrandId(spuVo.getBrandId());
            skuVo.setSpuId(spuVoId);
            List<String> images = skuVo.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                skuVo.setDefaultImage(StringUtils.isNotBlank(skuVo.getDefaultImage()) ? skuVo.getDefaultImage() : images.get(0));
            }
            this.skuMapper.insert(skuVo);
            Long skuVoId = skuVo.getId();
            //2.1保存sku_images信息
            if (!CollectionUtils.isEmpty(images)) {

                skuImagesService.saveBatch(
                        images.stream().map(image -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setUrl(image);
                            skuImagesEntity.setSkuId(skuVoId);
                            skuImagesEntity.setDefaultStatus(StringUtils.equals(image, skuVo.getDefaultImage()) ? 1 : 0);
                            return skuImagesEntity;
                        }).collect(Collectors.toList()));
            }

            //2.2保存sku_attr_value信息
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)) {
                saleAttrs.forEach(SkuAttrValueEntity -> {
                    SkuAttrValueEntity.setSkuId(skuVoId);
                });
                skuAttrValueService.saveBatch(saleAttrs);
            }


            //3、保存sku营销信息
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo, skuSaleVo);
            skuSaleVo.setSkuId(skuVoId);
            this.smsClient.saveSales(skuSaleVo);
        });
    }


    public void saveBaseAttr(SpuVo spuVo, Long spuVoId) {
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(spuAttrValueVo -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                spuAttrValueEntity.setSpuId(spuVoId);
                return spuAttrValueEntity;
            }).collect(Collectors.toList());
            this.spuAttrValueService.saveBatch(spuAttrValueEntities);
        }
    }


    public Long saveSpu(SpuVo spuVo) {
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime());
        this.save(spuVo);
        return spuVo.getId();
    }

}