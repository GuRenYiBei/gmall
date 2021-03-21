package com.atguigu.gmall.index.config;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.util.List;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/18/11:02
 * @Description:
 ******************************************/
@Configuration
public class BloomFilterConfig {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private GmallPmsClient pmsClient;

    private static final String KEY_PREFIX = "index:cates:[";

    @Bean
    public RBloomFilter bloomFilter() {
        RBloomFilter<Object> bloomFilter = this.redissonClient.getBloomFilter("index:cates:bloom");
        bloomFilter.tryInit(10000, 0.003);
        ResponseVo<List<CategoryEntity>> categroiesByParentId = this.pmsClient.queryCategroiesByParentId(0l);
        List<CategoryEntity> categoryEntities = categroiesByParentId.getData();
        if (!CollectionUtils.isEmpty(categoryEntities)) {
            categoryEntities.forEach(categoryEntity -> {
                bloomFilter.add(KEY_PREFIX + categoryEntity.getId() + "]");
            });
        }
        return bloomFilter;
    }

    //通过定时任务，定时设置一个新的布隆过滤器
//    @Scheduled
//    public void flushBloomFilter() {
//        RBloomFilter<Object> bloomFilter = this.redissonClient.getBloomFilter("index:cates:bloom");
//        bloomFilter.tryInit(10000, 0.003);
//        ResponseVo<List<CategoryEntity>> categroiesByParentId = this.pmsClient.queryCategroiesByParentId(0l);
//        List<CategoryEntity> categoryEntities = categroiesByParentId.getData();
//        if (!CollectionUtils.isEmpty(categoryEntities)) {
//            categoryEntities.forEach(categoryEntity -> {
//                bloomFilter.add(KEY_PREFIX + categoryEntity.getId() + "]");
//            });
//        }
//    }
}
