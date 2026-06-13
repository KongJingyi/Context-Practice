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

import java.util.Map;

@RestController
@RequestMapping("/api/match")
@CrossOrigin
@RequiredArgsConstructor
public class MatchController {

    private final ContentCompatService contentCompatService;

    @PostMapping("/smart")
    public ApiResult<Map<String, Object>> smartMatch(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(contentCompatService.smartMatch(body));
    }

    @GetMapping("/recommend")
    public ApiResult<Map<String, Object>> recommend(
            @RequestParam(required = false, name = "scene_id") String sceneId) {
        return ApiResult.ok(contentCompatService.matchRecommend(sceneId));
    }
}
