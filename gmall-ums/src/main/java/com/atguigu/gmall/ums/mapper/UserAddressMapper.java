package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货地址表
 * 
 * @author Gryb
 * @email Gryb@atguigu.com
 * @date 2021-03-06 00:03:02
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddressEntity> {
	
}
