package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_user_auth")
public class UserAuth {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** MOBILE / WECHAT / QQ */
    private String provider;

    private String providerUid;

    private String mobile;

    private String passwordHash;

    private LocalDateTime lastLoginAt;

    private Integer loginFailCount;

    private LocalDateTime lockedUntil;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
