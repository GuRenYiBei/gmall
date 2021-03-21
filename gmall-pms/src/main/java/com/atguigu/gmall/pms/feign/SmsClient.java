package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/09/18:48
 * @Description:
 ******************************************/
@FeignClient("sms-service")
public interface SmsClient extends GmallSmsApi {

}
