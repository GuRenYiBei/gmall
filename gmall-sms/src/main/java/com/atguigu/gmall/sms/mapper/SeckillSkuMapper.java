package com.atguigu.gmall.sms.mapper;

import com.atguigu.gmall.sms.entity.SeckillSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动商品关联
 * 
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-06 09:01:55
 */
@Mapper
public interface SeckillSkuMapper extends BaseMapper<SeckillSkuEntity> {
	
}
