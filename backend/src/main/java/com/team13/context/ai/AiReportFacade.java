package com.team13.context.ai;

import com.team13.context.entity.TrainingRecord;

/**
 * AI 报告与逻辑纠错门面：对接大模型 / 规则引擎，生成结构化反馈。
 * 实现类中可编排 Prompt、RAG、流式输出等。
 */
public interface AiReportFacade {

    /**
     * 根据训练记录与会话摘要生成报告（占位实现可返回固定文案）。
     */
    String generateTrainingReport(TrainingRecord record, String sessionSummary);
}
