package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_training_report")
public class TrainingReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long trainingId;

    private String pdfUrl;

    private Integer scoreLogic;

    private Integer scoreFluency;

    private Integer scorePressure;

    private Integer scoreContent;

    private Integer scoreTime;

    /** JSON：改进建议列表等 */
    private String highlights;

    /** 大模型原始 JSON 或结构化反馈全文 */
    private String coachFeedback;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
