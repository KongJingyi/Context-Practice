package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.service.frontend.CoachCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
@RequiredArgsConstructor
public class CoachController {

    private final CoachCatalogService coachCatalogService;

    @GetMapping("/coaches")
    public ApiResult<List<Map<String, Object>>> list(
            @RequestParam(required = false, name = "scene_id") String sceneId,
            @RequestParam(required = false, name = "level_min") Integer levelMin,
            @RequestParam(required = false, name = "level_max") Integer levelMax,
            @RequestParam(required = false) Double stars,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false, name = "today_only") Boolean todayOnly) {
        return ApiResult.ok(coachCatalogService.listCoaches(sceneId, levelMin, levelMax, stars, tags, todayOnly));
    }

    @GetMapping("/coaches/{coachId}")
    public ApiResult<Map<String, Object>> detail(@PathVariable String coachId) {
        return ApiResult.ok(coachCatalogService.getCoachDetail(coachId));
    }

    @GetMapping("/v1/coaches/{coachId}/reviews")
    public ApiResult<List<Map<String, Object>>> reviews(@PathVariable String coachId) {
        return ApiResult.ok(coachCatalogService.listCoachReviews(coachId));
    }
}
