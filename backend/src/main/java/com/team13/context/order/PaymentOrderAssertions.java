package com.team13.context.order;

import com.team13.context.entity.Order;
import com.team13.context.entity.Payment;

import java.math.BigDecimal;

public final class PaymentOrderAssertions {

    private PaymentOrderAssertions() {
    }

    public static void assertPaymentMatchesOrder(Order order, Payment payment) {
        BigDecimal oa = order.getAmount();
        BigDecimal pa = payment.getAmount();
        if (oa == null || pa == null) {
            throw new IllegalArgumentException("订单或支付金额缺失，无法完成校验");
        }
        if (pa.compareTo(oa) != 0) {
            throw new IllegalArgumentException("支付金额与订单金额不一致");
        }
    }
}
