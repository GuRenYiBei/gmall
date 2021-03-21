package com.atguigu.gmall.pms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableFeignClients //远程接口调用feign
//如果不指定mapper这一层的路径，会将service扫描进去，serviceImpl上使用了注解的话，就会和service接口的名字发生冲突，产生bean名字冲突异常
@MapperScan("com.atguigu.gmall.pms.mapper") //mapper包路径
@EnableSwagger2 //swagger注解
@RefreshScope //从nacos配置中心动态读取配置文件的注解

public class GmallPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPmsApplication.class, args);
    }

}
