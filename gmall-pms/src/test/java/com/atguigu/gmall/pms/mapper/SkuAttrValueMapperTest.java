package com.atguigu.gmall.pms.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/18/16:31
 * @Description:
 ******************************************/
@SpringBootTest
class SkuAttrValueMapperTest {

    @Autowired
    SkuAttrValueMapper mapper;

    @Test
    void querySalesAttrValueMappingSkuId() {
        System.out.println(this.mapper.querySalesAttrValueMappingSpuId(38l));
    }
}