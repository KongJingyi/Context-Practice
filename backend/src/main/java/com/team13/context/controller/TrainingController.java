package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.dto.AiTrainingReportRequest;
import com.team13.context.dto.TrainingEndRequest;
import com.team13.context.dto.TrainingStartRequest;
import com.team13.context.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 训练会话相关 HTTP 接口。
 */
@RestController
@RequestMapping("/api/v1/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    /**
     * 开始训练：创建会话 / 房间占位。
     */
    @PostMapping("/start")
    public ApiResult<Map<String, String>> start(@Valid @RequestBody TrainingStartRequest request) {
        String roomId = trainingService.startTraining(request);
        return ApiResult.ok(Map.of("roomId", roomId));
    }

    /**
     * 结束训练：收尾状态、持久化时长等占位。
     */
    @PostMapping("/end")
    public ApiResult<Void> end(@Valid @RequestBody TrainingEndRequest request) {
        trainingService.endTraining(request);
        return ApiResult.ok(null);
    }

    /**
     * 调用 AI 生成训练报告（逻辑纠错与结构化反馈在 {@link com.team13.context.ai.AiReportFacade} 中扩展）。
     */
    @PostMapping("/report")
    public ApiResult<Map<String, String>> report(@Valid @RequestBody AiTrainingReportRequest request) {
        String content = trainingService.generateAiReport(request);
        return ApiResult.ok(Map.of("report", content));
    }
}
