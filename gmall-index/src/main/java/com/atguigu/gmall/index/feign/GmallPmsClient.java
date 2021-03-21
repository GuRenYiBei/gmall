package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/16/0:28
 * @Description:
 ******************************************/
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
