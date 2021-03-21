package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/19/11:46
 * @Description:
 ******************************************/
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
