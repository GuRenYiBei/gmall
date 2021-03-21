package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import javax.xml.ws.Response;

/**
 * sku销售属性&值
 *
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-05 23:01:30
 */
@Api(tags = "sku销售属性&值 管理")
@RestController
@RequestMapping("pms/skuattrvalue")
public class SkuAttrValueController {

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    //根据spuid查询spu下所有销售属性和skuid的映射
    @GetMapping("sku/mapping/{spuId}")
    public ResponseVo<String> querySalesAttrValueMappingSpuId(@PathVariable("spuId") Long spuId) {
        String json = this.skuAttrValueService.querySalesAttrValueMappingSpuId(spuId);
        return ResponseVo.ok(json);
    }

    //根据spuid查询spu下所有sku的销售属性
    @GetMapping("spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrValueBySpuId(@PathVariable("spuId") String spuId) {
        List<SaleAttrValueVo> saleAttrValueVos = this.skuAttrValueService.querySaleAttrValueBySpuId(spuId);
        return ResponseVo.ok(saleAttrValueVos);
    }

    //根据skuid查询sku销售属性
    @GetMapping("sale/attr/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> queryAttrValuesBySkuId(@PathVariable("skuId") String skuId) {
        List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueService.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId));
        return ResponseVo.ok(skuAttrValueEntities);
    }

    // 通过skuid和attrid共同查找向es索引库中导入的数据
    @GetMapping("search/attr/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueBySkuIdAndCid(
            @PathVariable("skuId") Long skuId,
            @RequestParam("cid") Long cid
    ) {
        List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueService.querySearchAttrValueBySkuIdAndCid(skuId,cid);
        return ResponseVo.ok(skuAttrValueEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuAttrValueByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = skuAttrValueService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuAttrValueEntity> querySkuAttrValueById(@PathVariable("id") Long id){
		SkuAttrValueEntity skuAttrValue = skuAttrValueService.getById(id);

        return ResponseVo.ok(skuAttrValue);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuAttrValueEntity skuAttrValue){
		skuAttrValueService.save(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuAttrValueEntity skuAttrValue){
		skuAttrValueService.updateById(skuAttrValue);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		skuAttrValueService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
