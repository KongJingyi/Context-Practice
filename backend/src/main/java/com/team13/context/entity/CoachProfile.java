package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ctx_coach_profile")
public class CoachProfile {

    @TableId
    private Long userId;

    private String avatarUrl;

    private String nickname;

    private String bio;

    private Long levelId;

    private Integer status;

    @TableField("price_per_30m")
    private BigDecimal pricePer30m;

    private String specialties;

    private String serviceTags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
