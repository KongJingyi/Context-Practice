package com.team13.context.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.team13.context.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    @Select("""
            SELECT r.code
            FROM ctx_user_role ur
            INNER JOIN ctx_role r ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
            """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
