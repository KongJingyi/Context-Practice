package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_user_profile")
public class UserProfile {

    @TableId
    private Long userId;

    private String avatarUrl;

    private String nickname;

    private String trainingGoal;

    private String realName;

    private String identityType;

    private String organization;

    private String contactPhone;

    private Integer verifiedStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
