package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.metrics.BusinessMetricsRecorder;
import com.team13.context.metrics.MetricKeys;
import com.team13.context.service.frontend.AdminPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPortalController {

    private final AdminPortalService adminPortalService;
    private final BusinessMetricsRecorder businessMetricsRecorder;

    @GetMapping("/dashboard")
    public ApiResult<Map<String, Object>> dashboard() {
        return ApiResult.ok(adminPortalService.dashboard());
    }

    @GetMapping("/users")
    public ApiResult<List<Map<String, Object>>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.ok(adminPortalService.listUsers(keyword, page, size));
    }

    @PutMapping("/users/{userId}/status")
    public ApiResult<Map<String, Object>> updateUserStatus(
            @PathVariable Long userId, @RequestBody Map<String, Object> body) {
        int status = body.get("status") instanceof Number n ? n.intValue() : 0;
        return ApiResult.ok(adminPortalService.updateUserStatus(userId, status));
    }

    @GetMapping("/verifications")
    public ApiResult<List<Map<String, Object>>> verifications(
            @RequestParam(defaultValue = "pending") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.ok(adminPortalService.listVerifications(status, page, size));
    }

    @PostMapping("/verifications/{id}/review")
    public ApiResult<Map<String, Object>> reviewVerification(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        int status = body.get("status") instanceof Number n ? n.intValue() : 1;
        String note = String.valueOf(body.getOrDefault("note", ""));
        return ApiResult.ok(adminPortalService.reviewVerification(id, status, note));
    }

    @GetMapping("/complaints")
    public ApiResult<List<Map<String, Object>>> complaints(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.ok(adminPortalService.listComplaints(status, page, size));
    }

    @PostMapping("/complaints/{id}/handle")
    public ApiResult<Map<String, Object>> handleComplaint(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = String.valueOf(body.getOrDefault("status", "CLOSED"));
        String note = String.valueOf(body.getOrDefault("resultNote", ""));
        return ApiResult.ok(adminPortalService.handleComplaint(id, status, note));
    }

    @GetMapping("/refunds")
    public ApiResult<List<Map<String, Object>>> refunds(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.ok(adminPortalService.listRefunds(status, page, size));
    }

    @PostMapping("/refunds/{id}/decide")
    public ApiResult<Map<String, Object>> decideRefund(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = String.valueOf(body.getOrDefault("status", "APPROVED"));
        return ApiResult.ok(adminPortalService.decideRefund(id, status));
    }

    @GetMapping("/certificates/pending")
    public ApiResult<List<Map<String, Object>>> pendingCertificates(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.ok(adminPortalService.listPendingCertificates(page, size));
    }

    @PostMapping("/certificates/{id}/review")
    public ApiResult<Map<String, Object>> reviewCertificate(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        int status = body.get("status") instanceof Number n ? n.intValue() : 1;
        String reason = String.valueOf(body.getOrDefault("rejectReason", ""));
        return ApiResult.ok(adminPortalService.reviewCertificate(id, status, reason));
    }

    @GetMapping("/announcements")
    public ApiResult<List<Map<String, Object>>> announcements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.ok(adminPortalService.listAnnouncements(page, size));
    }

    @PostMapping("/announcements")
    public ApiResult<Map<String, Object>> createAnnouncement(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(adminPortalService.createAnnouncement(body));
    }

    @PutMapping("/announcements/{id}")
    public ApiResult<Map<String, Object>> updateAnnouncement(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResult.ok(adminPortalService.updateAnnouncement(id, body));
    }

    @DeleteMapping("/announcements/{id}")
    public ApiResult<Map<String, Object>> deleteAnnouncement(@PathVariable Long id) {
        return ApiResult.ok(adminPortalService.deleteAnnouncement(id));
    }

    @GetMapping("/config")
    public ApiResult<List<Map<String, Object>>> configs() {
        return ApiResult.ok(adminPortalService.listConfigs());
    }

    @PutMapping("/config/{key}")
    public ApiResult<Map<String, Object>> upsertConfig(
            @PathVariable String key, @RequestBody Map<String, Object> body) {
        String value = String.valueOf(body.getOrDefault("value", ""));
        String description = body.containsKey("description") ? String.valueOf(body.get("description")) : null;
        return ApiResult.ok(adminPortalService.upsertConfig(key, value, description));
    }

    @GetMapping("/audit-logs")
    public ApiResult<List<Map<String, Object>>> auditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        businessMetricsRecorder.increment(MetricKeys.AUDIT_LOG_READ);
        return ApiResult.ok(adminPortalService.listAuditLogs(page, size));
    }

    @GetMapping("/scenes")
    public ApiResult<List<Map<String, Object>>> scenes() {
        return ApiResult.ok(adminPortalService.listScenes());
    }

    @PutMapping("/scenes/{id}/status")
    public ApiResult<Map<String, Object>> updateSceneStatus(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        int status = body.get("status") instanceof Number n ? n.intValue() : 1;
        return ApiResult.ok(adminPortalService.updateSceneStatus(id, status));
    }

    @GetMapping("/orders")
    public ApiResult<List<Map<String, Object>>> orders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.ok(adminPortalService.listOrders(status, page, size));
    }

    @GetMapping("/community/posts")
    public ApiResult<List<Map<String, Object>>> communityPosts(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResult.ok(adminPortalService.listCommunityPosts(status, keyword, page, size));
    }

    @PutMapping("/community/posts/{id}/status")
    public ApiResult<Map<String, Object>> updateCommunityPostStatus(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        int status = body.get("status") instanceof Number n ? n.intValue() : 0;
        return ApiResult.ok(adminPortalService.updateCommunityPostStatus(id, status));
    }

    @GetMapping("/question-banks")
    public ApiResult<List<Map<String, Object>>> questionBanks(
            @RequestParam(required = false) Long sceneId) {
        return ApiResult.ok(adminPortalService.listQuestionBanks(sceneId));
    }

    @PostMapping("/question-banks")
    public ApiResult<Map<String, Object>> createQuestionBank(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(adminPortalService.createQuestionBank(body));
    }

    @PutMapping("/question-banks/{id}/status")
    public ApiResult<Map<String, Object>> updateQuestionBankStatus(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        int status = body.get("status") instanceof Number n ? n.intValue() : 1;
        return ApiResult.ok(adminPortalService.updateQuestionBankStatus(id, status));
    }

    @GetMapping("/question-banks/{bankId}/questions")
    public ApiResult<List<Map<String, Object>>> questions(@PathVariable Long bankId) {
        return ApiResult.ok(adminPortalService.listQuestions(bankId));
    }

    @PostMapping("/question-banks/{bankId}/questions")
    public ApiResult<Map<String, Object>> createQuestion(
            @PathVariable Long bankId, @RequestBody Map<String, Object> body) {
        return ApiResult.ok(adminPortalService.createQuestion(bankId, body));
    }

    @PutMapping("/questions/{id}/status")
    public ApiResult<Map<String, Object>> updateQuestionStatus(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        int status = body.get("status") instanceof Number n ? n.intValue() : 1;
        return ApiResult.ok(adminPortalService.updateQuestionStatus(id, status));
    }
}
