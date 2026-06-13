package com.team13.context.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team13.context.common.ApiResult;
import com.team13.context.dto.OrderSummaryResponse;
import com.team13.context.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/coach/orders")
@RequiredArgsConstructor
public class CoachOrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResult<Page<OrderSummaryResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResult.ok(orderService.listCoachOrders(status, page, size));
    }
}
