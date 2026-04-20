package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 单次训练会话记录（对应实时房间生命周期）。
 */
@Data
@TableName("ctx_training_record")
public class TrainingRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 关联订单（可选） */
    private Long orderId;

    /** 房间或会话唯一标识 */
    private String roomId;

    /** 进行中、已结束、已生成报告等 */
    private String status;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
