package com.team13.context.service;

import com.team13.context.dto.AiTrainingReportRequest;
import com.team13.context.dto.TrainingEndRequest;
import com.team13.context.dto.TrainingStartRequest;

/**
 * 训练会话编排：房间创建、状态流转、触发 AI 报告。
 */
public interface TrainingService {

    String startTraining(TrainingStartRequest request);

    void endTraining(TrainingEndRequest request);

    String generateAiReport(AiTrainingReportRequest request);
}
