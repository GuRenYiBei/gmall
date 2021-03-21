package com.atguigu.gmall.item.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/19/18:54
 * @Description:
 ******************************************/
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(
            @Value("${threadPool.coreSize}") Integer coreSize,
            @Value("${threadPool.maxSize}") Integer maxSize,
            @Value("${threadPool.keepAlive}") Integer keepAlive,
            @Value("${threadPool.blockingSize}") Integer blockingSize
    ) {
        return new ThreadPoolExecutor(coreSize, maxSize, keepAlive, TimeUnit.SECONDS, new ArrayBlockingQueue<>(blockingSize));
    }
}
