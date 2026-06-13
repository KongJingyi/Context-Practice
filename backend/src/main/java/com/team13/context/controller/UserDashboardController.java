package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.service.frontend.UserPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
@RequiredArgsConstructor
public class UserDashboardController {

    private final UserPortalService userPortalService;

    @GetMapping("/profile")
    public ApiResult<Map<String, Object>> profile() {
        return ApiResult.ok(userPortalService.getDashboardProfile());
    }

    @GetMapping("/growth")
    public ApiResult<Map<String, Object>> growth(@RequestParam(required = false) String period) {
        return ApiResult.ok(userPortalService.getGrowthData(period));
    }

    @GetMapping("/ability-tags")
    public ApiResult<List<Map<String, Object>>> abilityTags() {
        return ApiResult.ok(userPortalService.getAbilityTags());
    }

    @GetMapping("/composite-score")
    public ApiResult<Map<String, Object>> compositeScore() {
        return ApiResult.ok(userPortalService.getCompositeScore());
    }

    @GetMapping("/reports/recent")
    public ApiResult<List<Map<String, Object>>> recentReports() {
        return ApiResult.ok(userPortalService.getRecentReports());
    }

    @GetMapping("/profile/edit")
    public ApiResult<Map<String, Object>> editableProfile() {
        return ApiResult.ok(userPortalService.getEditableProfile());
    }

    @PutMapping("/update")
    public ApiResult<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(userPortalService.updateProfile(body));
    }
}
