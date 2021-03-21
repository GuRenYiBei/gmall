package com.atguigu.gmall.sms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/09/16:44
 * @Description: 根据skuid查询数据封装的对象，以便pms远程调用
 ******************************************/
@Data
public class SkuSaleVo {

    private Long skuId;

    //成长积分
    private BigDecimal growBounds;
    //购物积分
    private BigDecimal buyBounds;
    //优惠生效情况
    private List<Integer> work;

    //满几件
    private Integer fullCount;
    //打几折
    private BigDecimal discount;
    //是否叠加其他优惠
    private Integer ladderAddOther;

    //满多少
    private BigDecimal fullPrice;
    //减多少
    private BigDecimal reducePrice;
    //是否参与其他优惠
    private Integer fullAddOther;
}
