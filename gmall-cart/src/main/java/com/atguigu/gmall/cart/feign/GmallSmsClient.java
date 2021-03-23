package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/19/11:45
 * @Description:
 ******************************************/
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
