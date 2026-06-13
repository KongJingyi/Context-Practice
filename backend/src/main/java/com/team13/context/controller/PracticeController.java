package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.service.frontend.ContentCompatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/practice")
@CrossOrigin
@RequiredArgsConstructor
public class PracticeController {

    private final ContentCompatService contentCompatService;

    @GetMapping("/questions/categories")
    public ApiResult<List<Map<String, Object>>> categories() {
        return ApiResult.ok(contentCompatService.listQuestionCategories());
    }

    @GetMapping("/questions")
    public ApiResult<List<Map<String, Object>>> questions(@RequestParam(required = false) String category) {
        return ApiResult.ok(contentCompatService.listQuestions(category));
    }

    @PostMapping("/analyze-voice")
    public ApiResult<Map<String, Object>> analyzeVoice(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(contentCompatService.analyzeVoice(body));
    }

    @PostMapping("/optimize-text")
    public ApiResult<Map<String, Object>> optimizeText(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(contentCompatService.optimizeText(body));
    }
}
