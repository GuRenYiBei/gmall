package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/12/13:42
 * @Description:
 ******************************************/
@Data
public class SearchParamVo {

    //查询的关键字
    private String keyword;
    //品牌的过滤条件
    private List<Long> brandId;
    //分类的过滤条件
    private List<Long> categoryId;

    //参数列表
    //JD 的多个参数间使用%5E分隔，且对应一张子表，使用_分隔参数，前边是参数的ID，后边是参数的值
        //keyword=手机&ev=5_121554%5E13519_76036%5E3753_1097%5E
    //我们为了简化操作，使用props=4：8G-128G&props=5：8G-256G；多个参数之间也可以使用，分隔
    private List<String> props;

    //排序，1代表价格升序，2代表价格降序，3代表销量降序，4代表新品降序，0代表默认，使用得分降序
    private Integer sort;

    //是否有货
    private Boolean stock;

    //价格区间参数
    private Double priceFrom;
    private Double priceTo;

    //当前页数，实际中无法设置每页显示的条数，为了保护数据的安全性，防爬虫
    private Integer pageNum = 1;

    private final Integer pageSize = 20;

}
