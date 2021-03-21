package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.Set;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/18/13:52
 * @Description:
 ******************************************/
@Data
public class SaleAttrValueVo {

    private Long attrId;
    private String attrName;
    private Set<String> attrValues;
}
