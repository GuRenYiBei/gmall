package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.aspect.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.lock.DistributedLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang.StringUtils;

import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/16/0:26
 * @Description:
 ******************************************/
@Service
public class IndexService {

    @Autowired
    GmallPmsClient pmsClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    DistributedLock distributedLock;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private static final String KEY_PREFIX = "index:cates:";

    public List<CategoryEntity> queryLvl1Categories() {
        ResponseVo<List<CategoryEntity>> categroiesByParentId = this.pmsClient.queryCategroiesByParentId(0L);
        return categroiesByParentId.getData();
    }

    //将所有有关缓存的代码抽取成一个自定义的注解，只关注业务逻辑
    @GmallCache(prefix = KEY_PREFIX,timeOut = 129600l,random = 14400,lock = "lock:cates:")
    public List<CategoryEntity> queryLvl2CategoriesWithSubsByPid(Long pid) {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryLvl2CatesWithSubsByPid(pid);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        return categoryEntities;
    }


    public List<CategoryEntity> queryLvl2CategoriesWithSubsByPid2(Long pid) {
        //先查询缓存，如果缓存中不为空则直接返回
        String json = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json)) {
            //反序列化，通过fastjson将json字符串转换为对象数组
            List<CategoryEntity> categoryEntities = JSON.parseArray(json, CategoryEntity.class);
            return categoryEntities;
        }

        //添加分布式锁
        RLock lock = this.redissonClient.getLock("lock: " + pid);
        //通过拼接pid只锁住自己
        lock.lock();

        try {
            //此时只能保证只有一个请求去查询数据库，但是第一个请求查询到数据库将数据放到redis中，后续的请求还是会去数据库中查找，所以要再次判断redis中是否有数据
            String json2 = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
            if (StringUtils.isNotBlank(json2)) {
                List<CategoryEntity> categoryEntities = JSON.parseArray(json2, CategoryEntity.class);
                return categoryEntities;
            }

            //如果缓存中为空再执行业务逻辑并将数据添加到缓存中去
            ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryLvl2CatesWithSubsByPid(pid);
            List<CategoryEntity> categoryEntities = listResponseVo.getData();
            //防止缓存击穿，如果同时有多个redis中不存在的数据，将第一个请求记录并在redis中存为null，但是缓存设置的时间极短，其他请求就不会到达数据库，防止了缓存击穿
            if (CollectionUtils.isEmpty(categoryEntities)) {
                this.stringRedisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), 1, TimeUnit.MINUTES);
            } else {
                //将数据转换为json字符串存到redis中
                //设置一个随机的过期时间，为了防止缓存雪崩
                this.stringRedisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), 2160 + new Random().nextInt(360), TimeUnit.HOURS);
            }
            return categoryEntities;
        } finally {
            lock.unlock();
        }
    }

    //通过redisson客户端使用分布式锁
    public synchronized void jvmLockTest() {
        //可重入锁
        RLock lock = this.redissonClient.getLock("lock");
        lock.lock();
        try {
            String s = this.stringRedisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(s)) {
                return;
            }
            int num = Integer.parseInt(s);
            this.stringRedisTemplate.opsForValue().set("num", String.valueOf(++num));
        } finally {
            lock.unlock();
        }

    }


    //二次改进
    public synchronized void jvmLockTest3() {

        String uuid = UUID.randomUUID().toString();
        this.distributedLock.tryLock("lock", uuid, 30);

        String s = this.stringRedisTemplate.opsForValue().get("num");
        if (StringUtils.isBlank(s)) {
            return;
        }
        int num = Integer.parseInt(s);
        this.stringRedisTemplate.opsForValue().set("num", String.valueOf(++num));

        try {
            TimeUnit.SECONDS.sleep(180);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.testLock("lock", uuid);

        this.distributedLock.unlock("lock", uuid);
    }

    //初步设置的方法
    public synchronized void jvmLockTest2() {
        String uuid = UUID.randomUUID().toString();
        //使用分布式锁解决多线程访问的问题
        //setIfAbsent,相当于redis中的setnx，并设置过期时间
        Boolean b = this.stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
        //如果此时获取到了锁但是服务宕机，就可能会发生死锁，有两种方案
        //1、在else中通过expire设置，但是无法保证原子性，还是会发生死锁
        //2、set key value ex 3 nx
        //尝试获取锁，如果失败就重复获取
        //此时还有误删，和自动续期的问题
        if (!b) {
            try {
                Thread.sleep(50);
                this.jvmLockTest();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            String s = this.stringRedisTemplate.opsForValue().get("num");
            if (StringUtils.isBlank(s)) {
                return;
            }
            int num = Integer.parseInt(s);
            this.stringRedisTemplate.opsForValue().set("num", String.valueOf(++num));

            //删除锁之前判断是不是自己的锁，防止误删
            //但是设置了判断，判断和删除之间没有原子性，有可能刚判断完线程就超时了，下一个线程直接就删除了
            //要想判断和删除之间有原子性，要使用lua脚本
            //通过lua脚本解决原子性问题
            String script = "if(redis.call('get',KEYS[1])==ARGV[1]) then return redis.call('del',KEYS[1]) else return 0 end";
            this.stringRedisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock"), uuid);
//            if (StringUtils.equals(uuid, this.stringRedisTemplate.opsForValue().get("lock"))) {
//                this.stringRedisTemplate.delete("lock");
//            }
            //清空锁，否则会造成死锁
        }
    }

    public void testLock(String lockName,String uuid) {
        this.distributedLock.tryLock(lockName, uuid, 30);
        System.out.println("测试分布式锁");
        this.distributedLock.unlock(lockName, uuid);
    }

    //写锁与写锁可并发，读与读可并发，读与写要等待10秒释放锁
    public void writeTest() {
        RReadWriteLock wrLock = this.redissonClient.getReadWriteLock("wrLock");
        wrLock.writeLock().lock(10, TimeUnit.SECONDS);
        System.out.println("测试写锁");
    }

    public void readTest() {
        RReadWriteLock wrLock = this.redissonClient.getReadWriteLock("wrLock");
        wrLock.readLock().lock(10, TimeUnit.SECONDS);
        System.out.println("测试读锁");
    }

    //latch方法会一直锁定，知道countDown方法执行6次之后才能执行成功
    public void latch() {
        try {
            RCountDownLatch latchLock = this.redissonClient.getCountDownLatch("latchLock");
            latchLock.trySetCount(6);
            latchLock.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void countDown() {
        RCountDownLatch latchLock = this.redissonClient.getCountDownLatch("latchLock");
        latchLock.countDown();
    }
}
