package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_rating")
public class Rating {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long userId;

    private Long coachId;

    private Integer scoreProfessional;

    private Integer scoreAttitude;

    private Integer scoreQuality;

    private String content;

    private String coachReply;

    private String appealStatus;

    /** 逗号分隔标签 */
    private String tags;

    /** 1 匿名展示 */
    private Integer isAnonymous;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
