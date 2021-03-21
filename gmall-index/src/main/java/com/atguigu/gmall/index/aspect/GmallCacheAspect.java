package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.config.BloomFilterConfig;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/17/21:21
 * @Description:
 ******************************************/
@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter bloomFilter;

    //前置通知
    @Before("execution(* com.atguigu.gmall.index.service.*.*(..))")
    public void before(JoinPoint joinPoint) {
        joinPoint.getArgs();
    }

    //方法返回通知
    @AfterReturning(value = "execution(* com.atguigu.gmall.index.service.*.*(..))",returning = "result")
    public void afterReturning(JoinPoint joinPoint,Object result) {
        joinPoint.getArgs();
    }

    //异常返回通知
    @AfterThrowing(value = "execution(* com.atguigu.gmall.index.service.*.*(..))",throwing = "ex")
    public void afterReturning(JoinPoint joinPoint,Throwable ex) {
        joinPoint.getArgs();
    }

    //后置通知
    @After(value = "execution(* com.atguigu.gmall.index.service.*.*(..))")
    public void afterReturning(JoinPoint joinPoint) {
        joinPoint.getArgs();
    }

    /*
    *   joinPoint.getArgs：获取方法参数
    *   joinPoint.getTarget.getclass：获取目标类
    *
    * */
    //环绕通知
    @Around("@annotation(GmallCache)") //只切入注解
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        //获取切点方法的签名
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        //获取方法对象
        Method method = signature.getMethod();
        //获取注解对象
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);
        //获取注解中的前缀
        String prefix = gmallCache.prefix();
        //获取方法列表的参数
        List<Object> args = Arrays.asList(joinPoint.getArgs());
        String key = prefix + args;

        //查询缓存之前，判断key在布隆过滤器中也有
        boolean contains = this.bloomFilter.contains(key);
        if (!contains) {
            return null;
        }

        //获取方法的返回值类型
        Class<?> returnType = method.getReturnType();
        //拦截前的方法，判断缓存中有没有数据，如果有，直接反序列化，然后返回
        String json = this.redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(json)) {
            return JSON.parseObject(json,returnType);
        }
        //如果没有数据，为了防止缓存击穿 就加分布式锁
        RLock fairLock = this.redissonClient.getFairLock(gmallCache.lock() + args);
        fairLock.lock();

        try {
            String json2 = this.redisTemplate.opsForValue().get(prefix + args);
            if (StringUtils.isNotBlank(json2)) {
                return JSON.parseObject(json2,returnType);
            }
            //执行目标方法，获取数据库中的数据
            Object result = joinPoint.proceed(joinPoint.getArgs());
            //如果result为null，防止缓存穿透，依然放入缓存，但时间极短
            if (result == null) {
//                this.redisTemplate.opsForValue().set(key, null, 1, TimeUnit.MINUTES);
                //因为在布隆过滤器中进行了判空，就不需要在这里判空
            } else {
                //防止缓存雪崩，设置随机失效时间
                long time = gmallCache.timeOut() + new Random().nextInt(gmallCache.random());
                //序列化数据到缓存
                this.redisTemplate.opsForValue().set(key,JSON.toJSONString(result) , time, TimeUnit.MINUTES);
            }
            return result;
        } finally {
            fairLock.unlock();
        }
    }
}
