package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/08/22:58
 * @Description:
 ******************************************/
@Data
public class SkuVo extends SkuEntity {
    //sku图片对应的数据集合
    private List<String> images;

    //skuAttrValue中的属性值
    private List<SkuAttrValueEntity> saleAttrs;

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

    //map : 将一个集合复制到另一个集合
    //filter：过滤筛选
    //reduce：求和
//    public static void main(String[] args) {
//        List<User> users = Arrays.asList(
//                new User(1, "qqq", true),
//                new User(2, "www", false),
//                new User(3, "eee", true),
//                new User(4, "rrr", false)
//        );
//        System.out.println(users.stream().filter(user -> user.getSex() == true).collect(Collectors.toList()));
//        System.out.println(users.stream().map(user -> {
//            Person person = new Person();
//            person.setId(user.getId());
//            person.setName(user.getName());
//            return person;
//        }).collect(Collectors.toList()));
//        System.out.println(users.stream().map(User::getId).reduce((a, b) -> a + b).get());
//    }
//}
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@ToString
//class User {
//    Integer id;
//    String name;
//    Boolean sex;
//}
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@ToString
//class Person{
//    Integer id;
//    String name;
//}

