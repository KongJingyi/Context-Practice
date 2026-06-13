package com.team13.context.order;

import com.team13.context.entity.Order;
import com.team13.context.entity.Payment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentOrderAssertionsTest {

    @Test
    void rejectsWhenAmountsDiffer() {
        Order order = new Order();
        order.setAmount(new BigDecimal("10.00"));
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("9.99"));
        assertThrows(IllegalArgumentException.class, () -> PaymentOrderAssertions.assertPaymentMatchesOrder(order, payment));
    }

    @Test
    void acceptsWhenAmountsEqual() {
        Order order = new Order();
        order.setAmount(new BigDecimal("10.00"));
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("10.00"));
        assertDoesNotThrow(() -> PaymentOrderAssertions.assertPaymentMatchesOrder(order, payment));
    }
}
