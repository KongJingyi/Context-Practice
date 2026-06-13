package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.dto.TrainingStartResponse;
import com.team13.context.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingEntryController {

    private final TrainingService trainingService;

    @GetMapping("/start")
    public ApiResult<TrainingStartResponse> startByOrderId(@RequestParam("orderId") Long orderId) {
        return ApiResult.ok(trainingService.startSessionByOrderId(orderId));
    }
}
