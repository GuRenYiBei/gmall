package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.cart.exception.AsyncExceptionHandler;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/23/13:56
 * @Description:
 ******************************************/
@Configuration
public class AsyncConfigurer implements org.springframework.scheduling.annotation.AsyncConfigurer {

    @Autowired
    private AsyncExceptionHandler asyncExceptionHandler;
    //配置线程池，可以创建ThreadPoolExecutor
    //默认ThreadPoolTaskExecutor，是通过TaskExecutionAutoConfiguration配置出来的
    @Override
    public Executor getAsyncExecutor() {
        return null;
    }

    //配置异步未捕获异常
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return asyncExceptionHandler;
    }
}
