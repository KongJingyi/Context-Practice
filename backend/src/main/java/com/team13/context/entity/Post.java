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

    private Long coachId;

    /** NOTE / HIGHLIGHT / EXPERIENCE */
    private String kind;

    private String title;

    private String content;

    private Integer hotScore;

    private Integer likeCount;

    private Integer commentCount;

    private Integer collectCount;

    /** JSON 扩展：type, tags, company, role, video_preview, display_name, display_medal */
    private String mediaUrls;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
