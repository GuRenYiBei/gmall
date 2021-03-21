package com.atguigu.gmall.index;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.Charset;

@SpringBootTest
class GmallIndexApplicationTests {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    void contextLoads() {
    }

    //使用google的guava
    @Test
    void testBloomFilter() {
        BloomFilter bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), 10, 0.3);
        //一般不适用构造器，使用create方法获取一个过滤器对象，第一个函数是一个接口，第二个是预期容量，第三个是误判率
        bloomFilter.put("1");
        bloomFilter.put("2");
        bloomFilter.put("3");
        bloomFilter.put("4");
        bloomFilter.put("5");
        bloomFilter.put("6");
        bloomFilter.put("7");

        System.out.println(bloomFilter.mightContain("1"));
        System.out.println(bloomFilter.mightContain("3"));
        System.out.println(bloomFilter.mightContain("5"));
        System.out.println(bloomFilter.mightContain("7"));
        System.out.println(bloomFilter.mightContain("11"));
        System.out.println(bloomFilter.mightContain("12"));
        System.out.println(bloomFilter.mightContain("13"));
        System.out.println(bloomFilter.mightContain("14"));
        System.out.println(bloomFilter.mightContain("15"));
        System.out.println(bloomFilter.mightContain("16"));
        System.out.println(bloomFilter.mightContain("17"));
        System.out.println(bloomFilter.mightContain("18"));
        System.out.println(bloomFilter.mightContain("19"));
        System.out.println(bloomFilter.mightContain("10"));
    }

    //使用redisson
    @Test
    void testBloomFilter2() {
        RBloomFilter<Object> bloomFilter = this.redissonClient.getBloomFilter("bloom:test");
        //设置初始容量以及误判率
        bloomFilter.tryInit(10, 0.3);
        bloomFilter.add("1");
        bloomFilter.add("2");
        bloomFilter.add("3");
        bloomFilter.add("4");
        bloomFilter.add("5");
        bloomFilter.add("6");
        bloomFilter.add("7");

        System.out.println(bloomFilter.contains("1"));
        System.out.println(bloomFilter.contains("3"));
        System.out.println(bloomFilter.contains("5"));
        System.out.println(bloomFilter.contains("7"));
        System.out.println(bloomFilter.contains("11"));
        System.out.println(bloomFilter.contains("12"));
        System.out.println(bloomFilter.contains("13"));
        System.out.println(bloomFilter.contains("14"));
        System.out.println(bloomFilter.contains("15"));
        System.out.println(bloomFilter.contains("16"));
        System.out.println(bloomFilter.contains("17"));
        System.out.println(bloomFilter.contains("18"));
        System.out.println(bloomFilter.contains("19"));
        System.out.println(bloomFilter.contains("10"));
    }

}
