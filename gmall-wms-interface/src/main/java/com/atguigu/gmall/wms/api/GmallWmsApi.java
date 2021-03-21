package com.atguigu.gmall.wms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/11/15:43
 * @Description:
 ******************************************/
public interface GmallWmsApi {
    //根据skuId查询库存信息
    @GetMapping("wms/waresku/sku/{skuId}")
    public ResponseVo<List<WareSkuEntity>> queryWareSkuBySkuId(@PathVariable Long skuId);
}
