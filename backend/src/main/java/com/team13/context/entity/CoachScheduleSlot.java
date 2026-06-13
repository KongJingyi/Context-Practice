package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_coach_schedule_slot")
public class CoachScheduleSlot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long coachId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 1 可预约 0 关闭 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
