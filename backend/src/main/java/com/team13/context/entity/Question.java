package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bankId;

    private String title;

    private Integer difficulty;

    private String tags;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
