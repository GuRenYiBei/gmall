package com.atguigu.gmall.pms.controller;

import java.util.List;

import com.atguigu.gmall.pms.vo.GroupVo;
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

import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 属性分组
 *
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-05 23:01:30
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {

    @Autowired
    private AttrGroupService attrGroupService;

    //根据分类id、skuid、spuid查询出分组以及组下规格参数的值
    @GetMapping("cid/spuId/skuId/{cid}")
    public ResponseVo<List<GroupVo>> queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(
            @PathVariable("cid")Long cid,
            @RequestParam("spuId")Long spuId,
            @RequestParam("spuId")Long skuId
    ) {
        List<GroupVo> groupVos = this.attrGroupService.queryGroupWithAttrValuesByCidAndSpuIdAndSkuId(cid, spuId, skuId);
        return ResponseVo.ok(groupVos);
    }

    //查询分类下的组及规格参，用于录入spu基本信息时第二个页面的查询
    @GetMapping("/withattrs/{catId}")
    public ResponseVo<List<AttrGroupEntity>> queryAttrGroupWithattrsByCatId(@PathVariable Long catId) {
        List<AttrGroupEntity> attrGroupEntities = this.attrGroupService.queryAttrGroupWithattrsByCatId(catId);
        return ResponseVo.ok(attrGroupEntities);
    }

    //查询三级分类的分组
    @GetMapping("/category/{cid}")
    public ResponseVo<List<AttrGroupEntity>> queryAttrGroupByCategoryId(@PathVariable Long cid) {
        List<AttrGroupEntity> groupEntities = this.attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));
        return ResponseVo.ok(groupEntities);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryAttrGroupByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = attrGroupService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<AttrGroupEntity> queryAttrGroupById(@PathVariable("id") Long id){
		AttrGroupEntity attrGroup = attrGroupService.getById(id);

        return ResponseVo.ok(attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		attrGroupService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
