package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.config.ThreadPoolConfig;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/19/11:42
 * @Description:
 ******************************************/
@Service
public class ItemService {

    @Autowired
    GmallPmsClient pmsClient;

    @Autowired
    GmallSmsClient smsClient;

    @Autowired
    GmallWmsClient wmsClient;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;


    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();

        CompletableFuture<SkuEntity> skuCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                throw new RuntimeException("您查询的商品信息不存在！！！");
            }

            //渲染sku相关信息
            itemVo.setSkuId(skuId);
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            itemVo.setPrice(skuEntity.getPrice());
            return skuEntity;
        }, threadPoolExecutor);


        CompletableFuture<Void> spuCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            //spu信息
            ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuName(spuEntity.getName());
                itemVo.setSpuId(spuEntity.getId());
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> categoryCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            //一二三级分类
            ResponseVo<List<CategoryEntity>> CategoryEntityResponseVo = this.pmsClient.queryAllCategoriesByCid(skuEntity.getCategoryId());
            List<CategoryEntity> categoryEntitys = CategoryEntityResponseVo.getData();
            itemVo.setCategorys(categoryEntitys);
        }, threadPoolExecutor);


        CompletableFuture<Void> brandCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            //品牌
            ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> imagesCompletableFuture = CompletableFuture.runAsync(() -> {
            //sku图片列表
            ResponseVo<List<SkuImagesEntity>> imagesResponseVo = this.pmsClient.queryImagesBySkuId(skuId);
            List<SkuImagesEntity> imagesEntityList = imagesResponseVo.getData();
            itemVo.setImages(imagesEntityList);
        }, threadPoolExecutor);


        CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
            //促销相关信息
            ResponseVo<List<ItemSaleVo>> salesResponseVo = this.smsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            itemVo.setSales(itemSaleVos);
        }, threadPoolExecutor);


        CompletableFuture<Void> stockCompletableFuture = CompletableFuture.runAsync(() -> {
            //是否有库存
            ResponseVo<List<WareSkuEntity>> queryWareSkuBySkuId = this.wmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = queryWareSkuBySkuId.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStock(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> saleAttrValueCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            //规格参数列表
//        private List<SaleAttrValueVo> saleAttrs;
            ResponseVo<List<SaleAttrValueVo>> saleAttrValueBySpuId = this.pmsClient.querySaleAttrValueBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> saleAttrValueVos = saleAttrValueBySpuId.getData();
            itemVo.setSaleAttrs(saleAttrValueVos);
        }, threadPoolExecutor);


        CompletableFuture<Void> attrValuesCompletableFuture = CompletableFuture.runAsync(() -> {
            //当前选种的sku的销售属性
            ResponseVo<List<SkuAttrValueEntity>> attrValuesBySkuId = this.pmsClient.queryAttrValuesBySkuId(skuId);
            List<SkuAttrValueEntity> attrValueEntities = attrValuesBySkuId.getData();
            if (!CollectionUtils.isEmpty(attrValueEntities)) {
                Map<Long, String> collect = attrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
                itemVo.setSaleAttr(collect);
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> mappingCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            //一种选中的销售组合和skuid的映射关系，以便选中后跳转到对应的商品详情页
            ResponseVo<String> stringResponseVo = this.pmsClient.querySalesAttrValueMappingSpuId(skuEntity.getSpuId());
            String data = stringResponseVo.getData();
            itemVo.setSkusJson(data);
        }, threadPoolExecutor);

        CompletableFuture<Void> descCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            //商品介绍
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = this.pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            if (spuDescEntity != null) {
                itemVo.setSpuImages(Arrays.asList(StringUtils.split(spuDescEntity.getDecript(), ",")));
            }
        }, threadPoolExecutor);


        CompletableFuture<Void> groupCompletableFuture = skuCompletableFuture.thenAcceptAsync(skuEntity -> {
            //spu的规格分组以及规格参数
            ResponseVo<List<GroupVo>> listResponseVo = this.pmsClient.queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(skuEntity.getCategoryId(), skuEntity.getSpuId(), skuId);
            List<GroupVo> groupVos = listResponseVo.getData();
            itemVo.setGroups(groupVos);
        }, threadPoolExecutor);

        CompletableFuture.allOf(spuCompletableFuture,categoryCompletableFuture,brandCompletableFuture,imagesCompletableFuture
        ,salesCompletableFuture,stockCompletableFuture,saleAttrValueCompletableFuture,attrValuesCompletableFuture
                ,mappingCompletableFuture,descCompletableFuture,groupCompletableFuture).join();

        return itemVo;
    }
}
