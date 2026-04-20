package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 平台用户。
 */
@Data
@TableName("ctx_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录名 / 展示名 */
    private String username;

    /** 手机号（可选，脱敏存储由业务层处理） */
    private String mobile;

    /** 账号状态：0 正常 1 冻结 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
