package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_coach_certificate")
public class CoachCertificate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long coachId;

    private String kind;

    private String verifyCode;

    private String title;

    private String fileUrl;

    /** 0 待审 1 通过 2 驳回 */
    private Integer status;

    private String rejectReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
