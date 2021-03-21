package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/11/16:41
 * @Description:
 ******************************************/
@FeignClient(value = "wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
