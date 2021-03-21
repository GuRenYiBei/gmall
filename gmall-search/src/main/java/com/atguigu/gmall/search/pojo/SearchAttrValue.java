package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/11/14:52
 * @Description:
 ******************************************/
@Data
public class SearchAttrValue {
    @Field(type = FieldType.Long)
    private Long attrId;
    @Field(type = FieldType.Keyword)
    private String attrName;
    @Field(type = FieldType.Keyword)
    private String attrValue;
}
