package com.team13.context;

import com.team13.context.common.UserContext;
import com.team13.context.constant.BusinessStatuses;
import com.team13.context.dto.MockPayRequest;
import com.team13.context.entity.Order;
import com.team13.context.entity.Payment;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.PaymentMapper;
import com.team13.context.metrics.BusinessMetricsRecorder;
import com.team13.context.service.impl.OrderServiceImpl;
import com.team13.context.service.support.OrderBookingSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplMockPayAmountTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private BusinessMetricsRecorder businessMetricsRecorder;

    @Mock
    private OrderBookingSupport orderBookingSupport;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void mockPay_rejectsWhenExistingSuccessPaymentAmountDiffersFromOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setAmount(new BigDecimal("100.00"));
        order.setStatus(BusinessStatuses.Order.PENDING_PAY);

        Payment payment = new Payment();
        payment.setOrderId(1L);
        payment.setStatus(BusinessStatuses.Payment.SUCCESS);
        payment.setAmount(new BigDecimal("99.99"));

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(paymentMapper.selectOne(any())).thenReturn(payment);
        when(orderBookingSupport.isPayExpired(any(), any())).thenReturn(false);

        MockPayRequest req = new MockPayRequest();
        req.setOrderId(1L);

        UserContext.setUserId(10L);
        try {
            assertThrows(IllegalArgumentException.class, () -> orderService.mockPay(req));
            verify(orderMapper, never()).updateById(any(Order.class));
        } finally {
            UserContext.clear();
        }
    }

    @Test
    void mockPay_rejectsWhenOrderBelongsToAnotherUser() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(10L);
        order.setAmount(new BigDecimal("100.00"));
        order.setStatus(BusinessStatuses.Order.PENDING_PAY);

        when(orderMapper.selectById(1L)).thenReturn(order);

        MockPayRequest req = new MockPayRequest();
        req.setOrderId(1L);

        UserContext.setUserId(99L);
        try {
            assertThrows(IllegalArgumentException.class, () -> orderService.mockPay(req));
            verify(orderMapper, never()).updateById(any(Order.class));
        } finally {
            UserContext.clear();
        }
    }
}
