package com.team13.context.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.team13.context.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
