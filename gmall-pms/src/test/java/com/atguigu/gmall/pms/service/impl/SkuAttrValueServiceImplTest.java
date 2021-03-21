package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/18/16:47
 * @Description:
 ******************************************/
@SpringBootTest
class SkuAttrValueServiceImplTest {

    @Autowired
    SkuAttrValueService skuAttrValueService;

    @Test
    void querySalesAttrValueMappingSpuId() {
        System.out.println(this.skuAttrValueService.querySalesAttrValueMappingSpuId(38l));
    }
}