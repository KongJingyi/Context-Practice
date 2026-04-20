package com.team13.context.ai.impl;

import com.team13.context.ai.AiReportFacade;
import com.team13.context.entity.TrainingRecord;
import org.springframework.stereotype.Service;

/**
 * 开发阶段占位实现，后续替换为真实 AI 调用。
 */
@Service
public class StubAiReportFacade implements AiReportFacade {

    @Override
    public String generateTrainingReport(TrainingRecord record, String sessionSummary) {
        return "【占位报告】训练记录 ID=" + record.getId()
                + "，会话摘要长度=" + (sessionSummary == null ? 0 : sessionSummary.length());
    }
}
