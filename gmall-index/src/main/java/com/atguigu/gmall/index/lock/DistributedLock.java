package com.atguigu.gmall.index.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/17/0:01
 * @Description:
 ******************************************/
@Component
public class DistributedLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Thread thread;

    //通过hash + lua实现可重入锁
    public Boolean tryLock(String keyName,String uuid,Integer expireTime) {
        String script = "if(redis.call('exists',KEYS[1])==0 or redis.call('exists',KEYS[1],ARGV[1],1)==1) then redis.call('hincrby',KEYS[1],ARGV[1],1);redis.call('expire',KEYS[1],ARGV[2]);return 1 else return 0 end ";
        Boolean flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(keyName), uuid, expireTime.toString());
        if (!flag) {
            try {
                Thread.sleep(50);
                this.tryLock(keyName, uuid, expireTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.renewTime(keyName, uuid, expireTime);
        return true;
    }

    //释放锁
    public void unlock(String keyName,String uuid) {
        String script = "if(redis.call('hexists',KEYS[1],ARGV[1])==0)  then return nil ; elseif(redis.call('hincrby',KEYS[1],ARGV[1],-1)==0) then redis.call('del',KEYS[1]);return 1 else return 0 end;";
        Boolean flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(keyName), uuid);
        if (flag == null) {
            throw new RuntimeException("你将要释放的是别人的锁");
        }
        thread.interrupt();
    }

    //通过看门狗子线程实现自动续期
    private void renewTime(String keyName,String uuid,Integer expireTime) {
        String script = "if(redis.call('hexists',KEYS[1],ARGV[1])==1) then redis.call('expire',KEYS[1],ARGV[2]) return 1 ; else return 0 end;";
        thread = new Thread(()->{
            while (true) {
                try {
                    Thread.sleep(expireTime * 2000 / 3);

                    this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(keyName), uuid,expireTime.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"");
        thread.start();
    }
}

