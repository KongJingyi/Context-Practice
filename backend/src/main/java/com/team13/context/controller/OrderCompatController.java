package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.dto.CreateOrderRequest;
import com.team13.context.service.OrderService;
import com.team13.context.service.frontend.ContentCompatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
@RequiredArgsConstructor
public class OrderCompatController {

    private final OrderService orderService;
    private final ContentCompatService contentCompatService;

    @PostMapping("/orders/{orderId}/cancel")
    public ApiResult<Map<String, Object>> cancel(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, Object> body) {
        String reason = body != null ? String.valueOf(body.getOrDefault("reason", "")) : "";
        orderService.cancelOrder(orderId, reason);
        return ApiResult.ok(Map.of("ok", true));
    }

    @PostMapping("/orders/{orderId}/refund")
    public ApiResult<Map<String, Object>> refund(@PathVariable Long orderId) {
        orderService.applyRefund(orderId);
        return ApiResult.ok(Map.of("ok", true));
    }

    @PostMapping("/order/create")
    public ApiResult<Map<String, Object>> legacyCreate(@RequestBody Map<String, Object> body) {
        CreateOrderRequest req = new CreateOrderRequest();
        if (body.get("amount") != null) {
            req.setAmount(new BigDecimal(String.valueOf(body.get("amount"))));
        } else {
            req.setAmount(BigDecimal.valueOf(99));
        }
        if (body.get("expertId") != null) {
            Long coachId = com.team13.context.service.frontend.support.FrontendCoachSupport
                    .parseCoachUserId(String.valueOf(body.get("expertId")));
            if (coachId != null) {
                req.setCoachId(coachId);
            }
        }
        if (body.get("sceneId") != null) {
            req.setSceneId(Long.parseLong(String.valueOf(body.get("sceneId"))));
        }
        if (body.get("slotId") != null) {
            req.setSlotId(String.valueOf(body.get("slotId")));
        }
        if (body.get("date") != null) {
            req.setDate(String.valueOf(body.get("date")));
        }
        Long orderId = orderService.createPendingOrder(req);
        return ApiResult.ok(Map.of("orderId", String.valueOf(orderId)));
    }

    @PostMapping("/order/prepay")
    public ApiResult<Map<String, Object>> prepay(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(contentCompatService.prepayOrder(body));
    }

    @GetMapping("/coupons/available")
    public ApiResult<List<Map<String, Object>>> coupons(@RequestParam(required = false) Double amount) {
        return ApiResult.ok(contentCompatService.listCoupons(amount));
    }
}
