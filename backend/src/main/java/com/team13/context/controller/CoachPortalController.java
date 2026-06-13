package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.service.frontend.CoachPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coach")
@RequiredArgsConstructor
public class CoachPortalController {

    private final CoachPortalService coachPortalService;

    @GetMapping("/dashboard")
    public ApiResult<Map<String, Object>> dashboard() {
        return ApiResult.ok(coachPortalService.getDashboard());
    }

    @GetMapping("/schedule")
    public ApiResult<List<Map<String, Object>>> schedule() {
        return ApiResult.ok(coachPortalService.getWeeklySchedule());
    }

    @PostMapping("/schedule")
    public ApiResult<Map<String, Object>> saveSchedule(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> slots = body.get("slots") instanceof List<?> list
                ? (List<Map<String, Object>>) list
                : List.of();
        return ApiResult.ok(coachPortalService.saveWeeklySchedule(slots));
    }

    @GetMapping("/income/summary")
    public ApiResult<Map<String, Object>> incomeSummary() {
        return ApiResult.ok(coachPortalService.getIncomeSummary());
    }

    @GetMapping("/income")
    public ApiResult<Map<String, Object>> income() {
        return ApiResult.ok(Map.of("records", coachPortalService.listIncomeRecords()));
    }

    @GetMapping("/certificates")
    public ApiResult<Map<String, Object>> certificates() {
        return ApiResult.ok(Map.of("items", coachPortalService.listCertificates()));
    }

    @PostMapping("/certificates")
    public ApiResult<Map<String, Object>> submitCertificate(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String verifyCode,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        return ApiResult.ok(coachPortalService.submitCertificate(type, title, verifyCode, file));
    }

    @PostMapping("/ratings/{ratingId}/reply")
    public ApiResult<Map<String, Object>> replyRating(
            @PathVariable Long ratingId,
            @RequestBody Map<String, Object> body) {
        String reply = String.valueOf(body.getOrDefault("reply", body.getOrDefault("content", "")));
        return ApiResult.ok(coachPortalService.replyRating(ratingId, reply));
    }

    @PostMapping("/ratings/{ratingId}/appeal")
    public ApiResult<Map<String, Object>> appealRating(
            @PathVariable Long ratingId,
            @RequestBody Map<String, Object> body) {
        String reason = String.valueOf(body.getOrDefault("reason", ""));
        return ApiResult.ok(coachPortalService.appealRating(ratingId, reason));
    }
}
