package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_complaint")
public class Complaint {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long complainantId;

    private Long targetUserId;

    private String kind;

    private String content;

    private String evidenceUrls;

    private String status;

    private Long handledBy;

    private LocalDateTime handledAt;

    private String resultNote;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
