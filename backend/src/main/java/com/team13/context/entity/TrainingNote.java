package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_training_note")
public class TrainingNote {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long trainingId;

    private Long coachId;

    private String noteType;

    private String label;

    private String content;

    private Integer startSec;

    private Integer endSec;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
