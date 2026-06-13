package com.team13.context.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.team13.context.entity.CoachScene;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CoachSceneMapper extends BaseMapper<CoachScene> {

    @Select("""
            SELECT cs.coach_id
            FROM ctx_coach_scene cs
            WHERE cs.scene_key = #{sceneId}
               OR cs.scene_key = #{categoryId}
            GROUP BY cs.coach_id
            ORDER BY MIN(cs.sort_order) ASC, cs.coach_id ASC
            """)
    List<Long> findCoachIdsByScene(@Param("sceneId") String sceneId, @Param("categoryId") String categoryId);
}
