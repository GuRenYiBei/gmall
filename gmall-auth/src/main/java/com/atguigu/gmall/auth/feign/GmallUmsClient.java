package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/21/18:19
 * @Description:
 ******************************************/
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
