package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_training_participant")
public class TrainingParticipant {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long trainingId;

    private Long userId;

    /** USER / COACH */
    private String role;

    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
