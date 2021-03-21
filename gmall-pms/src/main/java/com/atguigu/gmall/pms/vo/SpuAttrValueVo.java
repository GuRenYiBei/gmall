package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/08/22:54
 * @Description: 用于接收页面中的valueSelected
 ******************************************/
public class SpuAttrValueVo extends SpuAttrValueEntity {

    //重写set方法，直接将获取到的valueSelected直接赋值给父类的AttrValue属性
    public void setValueSelected(List<String> valueSelected) {
        this.setAttrValue(StringUtils.join(valueSelected,","));
    }
}
