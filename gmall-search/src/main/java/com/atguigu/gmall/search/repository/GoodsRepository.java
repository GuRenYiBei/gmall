package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/11/16:42
 * @Description:
 ******************************************/
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
