package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserStatisticsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 统计信息表
 * 
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-06 00:03:02
 */
@Mapper
public interface UserStatisticsMapper extends BaseMapper<UserStatisticsEntity> {
	
}
