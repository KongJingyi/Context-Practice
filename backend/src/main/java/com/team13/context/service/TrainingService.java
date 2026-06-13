package com.team13.context.service;

import com.team13.context.dto.TrainingStartResponse;

/**
 * 训练会话编排：房间创建、状态流转、触发 AI 报告。
 */
public interface TrainingService {

    TrainingStartResponse startSessionByOrderId(Long orderId);

    TrainingStartResponse startTraining(com.team13.context.dto.TrainingStartRequest request);

    String endTraining(com.team13.context.dto.TrainingEndRequest request);

    String generateAiReport(com.team13.context.dto.AiTrainingReportRequest request);
}
