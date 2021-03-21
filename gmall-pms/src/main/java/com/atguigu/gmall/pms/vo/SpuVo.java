package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/08/22:35
 * @Description: 自定义vo类
 ******************************************/
@Data
public class SpuVo extends SpuEntity {

    private List<String> spuImages; //spu图片
    private List<SpuAttrValueVo> baseAttrs; //spu基本属性，对应表spu_attr_value
    private List<SkuVo> skus; //sku基本属性
}
