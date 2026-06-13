package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_verification_request")
public class VerificationRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** USER_BASIC / USER_ID_DOC / COACH_REALNAME / COACH_QUALIFICATION */
    private String kind;

    /** 0 待审核 1 通过 2 驳回 */
    private Integer status;

    private String submitPayload;

    private Long reviewerId;

    private LocalDateTime reviewedAt;

    private String reviewNote;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
