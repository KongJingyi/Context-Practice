package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_question_bank")
public class QuestionBank {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sceneId;

    private String name;

    private String category;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
