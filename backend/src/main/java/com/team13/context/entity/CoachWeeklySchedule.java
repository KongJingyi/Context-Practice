package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("ctx_coach_weekly_schedule")
public class CoachWeeklySchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long coachId;

    private Integer dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
