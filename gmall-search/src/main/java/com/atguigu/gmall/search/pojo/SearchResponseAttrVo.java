package com.atguigu.gmall.search.pojo;


import lombok.Data;

import java.util.List;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/12/23:04
 * @Description:
 ******************************************/
@Data
public class SearchResponseAttrVo {

    private Long attrId;
    private String attrName;
    private List<String> attrValues;

}
