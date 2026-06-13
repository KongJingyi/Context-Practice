package com.team13.context.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.team13.context.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
