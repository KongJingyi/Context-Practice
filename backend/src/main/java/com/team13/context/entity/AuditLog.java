package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_audit_log")
public class AuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long actorId;

    private String action;

    private String targetType;

    private Long targetId;

    private String detail;

    private String ip;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
