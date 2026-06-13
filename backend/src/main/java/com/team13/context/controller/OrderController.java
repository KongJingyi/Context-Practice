package com.team13.context.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team13.context.common.ApiResult;
import com.team13.context.dto.CreateOrderRequest;
import com.team13.context.dto.MockPayRequest;
import com.team13.context.dto.OrderSummaryResponse;
import com.team13.context.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResult<Map<String, Long>> create(@Valid @RequestBody CreateOrderRequest request) {
        Long orderId = orderService.createPendingOrder(request);
        return ApiResult.ok(Map.of("orderId", orderId));
    }

    @PostMapping("/mock-pay")
    public ApiResult<Void> mockPay(@Valid @RequestBody MockPayRequest request) {
        orderService.mockPay(request);
        return ApiResult.ok(null);
    }

    @GetMapping
    public ApiResult<Page<OrderSummaryResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResult.ok(orderService.listUserOrders(status, page, size));
    }

    @GetMapping("/home-reminders")
    public ApiResult<List<OrderSummaryResponse>> homeReminders(
            @RequestParam(defaultValue = "3") int size) {
        return ApiResult.ok(orderService.listHomeReminders(size));
    }

    @GetMapping("/{orderId}")
    public ApiResult<OrderSummaryResponse> detail(@PathVariable Long orderId) {
        return ApiResult.ok(orderService.getOrderDetail(orderId));
    }
}
