package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/19/11:44
 * @Description:
 ******************************************/
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
