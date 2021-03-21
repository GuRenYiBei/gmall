package com.atguigu.gmall.index.aspect;

import java.lang.annotation.*;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/17/20:13
 * @Description: 自定义注解
 ******************************************/
@Target({ElementType.METHOD}) //支持作用在方法上
@Retention(RetentionPolicy.RUNTIME) //运行时起作用
//@Inherited //是否支持继承
@Documented
public @interface GmallCache {

    //缓存key的前缀 模块名：实例名：
    String prefix() default "gmall:cache:";

    //缓存的过期时间，单位为分钟
    long timeOut() default 5l;

    //防止缓存学崩，给缓存时间设置的随机值，指定随机值的范围
    int random() default 5;

    //为了防止缓存击穿，给缓存添加分布式锁，指定分布式锁的前缀
    String lock() default "lock:";
}
