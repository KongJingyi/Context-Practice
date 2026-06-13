package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.service.frontend.ContentCompatService;
import com.team13.context.service.frontend.ReviewReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
@RequiredArgsConstructor
public class ReviewReportController {

    private final ReviewReportService reviewReportService;
    private final ContentCompatService contentCompatService;

    @PostMapping("/v1/reviews/submit")
    public ApiResult<Map<String, Object>> submitReview(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(reviewReportService.submitReview(body));
    }

    @GetMapping("/v1/reports/{orderId}")
    public ApiResult<Map<String, Object>> reportDetail(@PathVariable String orderId) {
        return ApiResult.ok(reviewReportService.getReportByOrderId(orderId));
    }

    /** 训练结束评价页：查询是否可评、是否已评、陪练/场景信息 */
    @GetMapping("/v1/reviews/status")
    public ApiResult<Map<String, Object>> reviewStatus(@RequestParam("order_id") Long orderId) {
        return ApiResult.ok(reviewReportService.getReviewStatus(orderId));
    }

    /** 文档别名：POST /api/v1/orders/{orderId}/rating */
    @PostMapping("/v1/orders/{orderId}/rating")
    public ApiResult<Map<String, Object>> submitOrderRating(
            @PathVariable Long orderId,
            @RequestBody Map<String, Object> body) {
        body.put("order_id", orderId);
        return ApiResult.ok(reviewReportService.submitReview(body));
    }

    @PostMapping("/training/highlight")
    public ApiResult<Map<String, Object>> highlight(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(contentCompatService.saveHighlight(body));
    }

    @PostMapping("/training/pressure-event")
    public ApiResult<Void> pressureEvent(@RequestBody Map<String, Object> body) {
        contentCompatService.reportPressureEvent(body);
        return ApiResult.ok(null);
    }
}
