package com.team13.context.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team13.context.dto.CreateOrderRequest;
import com.team13.context.dto.MockPayRequest;
import com.team13.context.dto.OrderSummaryResponse;

import java.util.List;

public interface OrderService {

    Long createPendingOrder(CreateOrderRequest request);

    void mockPay(MockPayRequest request);

    List<OrderSummaryResponse> listHomeReminders(int size);

    Page<OrderSummaryResponse> listUserOrders(String status, int page, int size);

    Page<OrderSummaryResponse> listCoachOrders(String status, int page, int size);

    OrderSummaryResponse getOrderDetail(Long orderId);

    void cancelOrder(Long orderId, String reason);

    void applyRefund(Long orderId);
}
