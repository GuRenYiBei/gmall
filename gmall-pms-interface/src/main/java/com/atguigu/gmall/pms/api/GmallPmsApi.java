package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/11/15:33
 * @Description:
 ******************************************/
public interface GmallPmsApi {

    //分页查询spu
    @PostMapping("pms/spu/json")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(PageParamVo paramVo);

    //根据spuId查询sku
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkuBySpuId(@PathVariable Long spuId);

    //根据brandId查询品牌信息
    @GetMapping("pms/brand/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    //根据categoryId查询分类信息
    @GetMapping("pms/category/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    //根据父ID查询具体分类详情
    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategroiesByParentId(@PathVariable Long parentId);

    //根据父Id查询二级分类以及三级分类
    @GetMapping("pms/category/parent/sub/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryLvl2CatesWithSubsByPid(@PathVariable("parentId") Long pid);

    //根据skuid结合规格参数表查询索检类型的规格参数
    @GetMapping("pms/skuattrvalue/search/attr/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueBySkuIdAndCid(
            @PathVariable("skuId") Long skuId,
            @RequestParam("cid") Long cid
    );

    //根据spuid结合规格参数表查询索检类型的规格参数
    @GetMapping("pms/spuattrvalue/search/attr/{spuId}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValueBySpuIdAndCid(
            @PathVariable("spuId") Long spuId,
            @RequestParam("cid") Long cid
    );

    //根据spuid查询详情
    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    //12个商品详情页需要的接口

    //根据skuid查询sku详细信息
    @GetMapping("pms/sku/{id}")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    //根据三级分类id查询一二三级分类
    @GetMapping("pms/category/all/{cid}")
    public ResponseVo<List<CategoryEntity>> queryAllCategoriesByCid(@PathVariable("cid") Long cid);

    //根据brandId查询品牌信息
    //根据spuid查询详情

    //根据skuid查询sku图片详细信息
    @GetMapping("pms/skuimages/sku/{id}")
    public ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable("id") Long id);

    //根据skuid查询sku销售属性
    @GetMapping("pms/skuattrvalue/sale/attr/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> queryAttrValuesBySkuId(@PathVariable("skuId") Long skuId);

    //根据spuid查询spudesc
    @GetMapping("pms/spudesc/{spuId}")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    //根据spuid查询spu下所有sku的销售属性
    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrValueBySpuId(@PathVariable("spuId") Long spuId);

    //根据spuid查询spu下所有销售属性和skuid的映射
    @GetMapping("pms/skuattrvalue/sku/mapping/{spuId}")
    public ResponseVo<String> querySalesAttrValueMappingSpuId(@PathVariable("spuId") Long spuId);

    //根据分类id、skuid、spuid查询出分组以及组下规格参数的值
    @GetMapping("pms/attrgroup/cid/spuId/skuId/{cid}")
    public ResponseVo<List<GroupVo>> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid") Long cid,
            @RequestParam("spuId") Long spuId,
            @RequestParam("spuId") Long skuId
    );
}
