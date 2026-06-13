package com.team13.context.ai.impl;

import com.team13.context.ai.AiReportFacade;
import com.team13.context.entity.TrainingRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 开发阶段占位实现（app.ai.enabled=false 时生效）。
 */
@Service
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "false", matchIfMissing = true)
public class StubAiReportFacade implements AiReportFacade {

    @Override
    public String generateTrainingReport(TrainingRecord record, String sessionSummary) {
        int len = sessionSummary == null ? 0 : sessionSummary.length();
        return String.format(
                "{\"score\":72,\"suggestions\":[\"【Stub】训练记录 ID=%d，表达文本长度=%d 字，"
                        + "启用 app.ai.enabled=true 并配置 DEEPSEEK_API_KEY 后将调用真实大模型\"]}",
                record.getId(), len);
    }
}
