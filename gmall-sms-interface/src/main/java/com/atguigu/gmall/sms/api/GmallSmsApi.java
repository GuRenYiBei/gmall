package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/09/19:46
 * @Description:
 ******************************************/
public interface GmallSmsApi {
    @PostMapping("sms/skubounds/sale/save")
    public ResponseVo saveSales(@RequestBody SkuSaleVo skuSaleVo);

    //根据skuid查询sku营销信息
    @GetMapping("sms/skubounds/sales/sku/{skuId}")
    public ResponseVo<List<ItemSaleVo>> querySalesBySkuId(@PathVariable("skuId") Long skuId);
}
