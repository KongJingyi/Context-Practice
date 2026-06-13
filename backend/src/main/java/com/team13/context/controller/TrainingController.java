package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.dto.AiTrainingReportRequest;
import com.team13.context.dto.TrainingEndRequest;
import com.team13.context.dto.TrainingStartRequest;
import com.team13.context.dto.TrainingStartResponse;
import com.team13.context.common.UserContext;
import com.team13.context.service.TrainingRecordingService;
import com.team13.context.service.TrainingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;
    private final TrainingRecordingService trainingRecordingService;

    @GetMapping("/{trainingId}/recording")
    public ApiResult<Map<String, Object>> recording(@PathVariable Long trainingId) {
        return ApiResult.ok(trainingRecordingService.getRecording(trainingId, UserContext.requireUserId()));
    }

    @PostMapping("/start")
    public ApiResult<TrainingStartResponse> start(@Valid @RequestBody TrainingStartRequest request) {
        return ApiResult.ok(trainingService.startTraining(request));
    }

    @PostMapping("/end")
    public ApiResult<Map<String, Object>> end(@Valid @RequestBody TrainingEndRequest request) {
        String report = trainingService.endTraining(request);
        if (report == null) {
            return ApiResult.ok(Map.of());
        }
        return ApiResult.ok(Map.of("report", report));
    }

    @PostMapping("/report")
    public ApiResult<Map<String, String>> report(@Valid @RequestBody AiTrainingReportRequest request) {
        String content = trainingService.generateAiReport(request);
        return ApiResult.ok(Map.of("report", content));
    }
}
