package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Test
    void contextLoads() {
        //初始化索引库和映射
        this.restTemplate.createIndex(Goods.class);
        this.restTemplate.putMapping(Goods.class);

        //分页查询spu
        Integer pageNum = 1;
        Integer pageSize = 100;
        do {
            PageParamVo pageParamVo = new PageParamVo();
            pageParamVo.setPageNum(pageNum);
            pageParamVo.setPageSize(pageSize);
            ResponseVo<List<SpuEntity>> listResponseVo = this.pmsClient.querySpuByPageJson(pageParamVo);
            List<SpuEntity> spuEntitys = listResponseVo.getData();
            if (CollectionUtils.isEmpty(spuEntitys)) {
                break;
            }
            //遍历spu查询sku，将sku转化为goods
            spuEntitys.forEach(spuEntity -> {
                ResponseVo<List<SkuEntity>> skuResponseVo = this.pmsClient.querySkuBySpuId(spuEntity.getId());
                List<SkuEntity> skuEntities = skuResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuEntities)) {
                    List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                        Goods goods = new Goods();
                        //商品列表需要的字段
                        goods.setSkuId(skuEntity.getId());
                        goods.setTitle(skuEntity.getTitle());
                        goods.setSubTitle(skuEntity.getSubtitle());
                        goods.setDefaultImage(skuEntity.getDefaultImage());
                        goods.setPrice(skuEntity.getPrice().doubleValue());

                        //新品排序，本质就是创建时间
                        goods.setCreateTime(spuEntity.getCreateTime());
                        //设置库存和销量
                        ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = this.wmsClient.queryWareSkuBySkuId(skuEntity.getId());
                        List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                            goods.setStock(wareSkuEntities.stream().allMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                            goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get());
                        }
                        //查询品牌
                        ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
                        BrandEntity brandEntity = brandEntityResponseVo.getData();
                        if (brandEntity != null) {
                            goods.setBrandId(brandEntity.getId());
                            goods.setBrandName(brandEntity.getName());
                            goods.setLogo(brandEntity.getLogo());
                        }
                        //查询分类
                        ResponseVo<CategoryEntity> categoryEntityResponseVo = this.pmsClient.queryCategoryById(skuEntity.getCategoryId());
                        CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                        if (categoryEntityResponseVo != null) {
                            goods.setCategoryId(categoryEntity.getId());
                            goods.setCategoryName(categoryEntity.getName());
                        }

                        //查询嵌套的规格参数
                        List<SearchAttrValue> searchAttrValues = new ArrayList<>();

                        //销售类型的规格参数以及值
                        ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = this.pmsClient.querySearchAttrValueBySkuIdAndCid(skuEntity.getId(), skuEntity.getCategoryId());
                        List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
                        if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                            searchAttrValues.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValue);
                                return searchAttrValue;
                            }).collect(Collectors.toList()));
                        }

                        //基本类型的规格参数以及值
                        ResponseVo<List<SpuAttrValueEntity>> spuAttrValueResponseVo = this.pmsClient.querySearchAttrValueBySpuIdAndCid(skuEntity.getSpuId(), skuEntity.getCategoryId());
                        List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueResponseVo.getData();
                        if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {

                            searchAttrValues.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValue);
                                return searchAttrValue;
                            }).collect(Collectors.toList()));
                        }

                        goods.setSearchAttrs(searchAttrValues);
                        return goods;

                    }).collect(Collectors.toList());

                    this.goodsRepository.saveAll(goodsList);
                }
            });
            pageNum++;
            pageSize = spuEntitys.size();
        } while (pageSize == 100);

    }

}
