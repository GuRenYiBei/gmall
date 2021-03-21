package com.atguigu.gmall.search.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/12/23:02
 * @Description:响应结果集
 ******************************************/
@Data
public class SearchResponseVo {
    //品牌过滤集合
    private List<BrandEntity> brands;
    //分类过滤集合
    private List<CategoryEntity> categories;
    //规格参数过滤条件，每个元素是一行过滤条件
    private List<SearchResponseAttrVo> filters;

    //分页需要的数据
    private Integer pageNum;
    private Integer pageSize;
    private Long total;

    //当前页的记录
    private List<Goods> goodsList;
}
