package com.team13.context.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.team13.context.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
