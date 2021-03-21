package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/18/13:43
 * @Description:
 ******************************************/
@Data
public class ItemVo {

    //面包屑需要的数据

    //spu信息
    private Long spuId;
    private String spuName;
    //一二三级分类
    private List<CategoryEntity> categorys;
    //品牌
    private Long brandId;
    private String brandName;

    //渲染的sku的详细信息
    private Long skuId;
    private String title;
    private String subTitle;
    private BigDecimal price;
    private String defaultImage;
    private Integer weight;

    //sku图片列表
    private List<SkuImagesEntity> images;
    //促销相关信息
    private List<ItemSaleVo> sales;
    //是否有库存
    private Boolean stock = false;
    //spu下所有sku的销售属性
    private List<SaleAttrValueVo> saleAttrs;

    //一个map用来存放选中后标红的商品，key是attrid，value是attrvalue
    private Map<Long, String> saleAttr;

    //一种选中的销售组合和skuid的映射关系，以便选中后跳转到对应的商品详情页
    private String skusJson;


    //商品介绍
    private List<String> spuImages;

    //spu的规格分组以及规格参数
    private List<GroupVo> groups;

}
