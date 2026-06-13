package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long authorId;

    private String kind;

    private String content;

    private String mediaUrls;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
