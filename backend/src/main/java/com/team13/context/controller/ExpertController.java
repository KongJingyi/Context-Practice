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
@RequestMapping("/api/expert")
@CrossOrigin
@RequiredArgsConstructor
public class ExpertController {

    private final CoachCatalogService coachCatalogService;

    @GetMapping("/{id}")
    public ApiResult<Map<String, Object>> detail(@PathVariable String id) {
        return ApiResult.ok(coachCatalogService.getExpertDetail(id));
    }

    @GetMapping("/{id}/slots")
    public ApiResult<List<Map<String, Object>>> slots(
            @PathVariable String id,
            @RequestParam(required = false) String date) {
        return ApiResult.ok(coachCatalogService.getExpertSlots(id, date));
    }
}
