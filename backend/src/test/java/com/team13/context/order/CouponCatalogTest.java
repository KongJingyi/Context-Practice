package com.team13.context.order;

import com.team13.context.service.support.CouponCatalog;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CouponCatalogTest {

    @Test
    void applyDiscount_subtractsCouponAmount() {
        BigDecimal payable = CouponCatalog.applyDiscount(BigDecimal.valueOf(128), "cp-20");
        assertEquals(new BigDecimal("108.00"), payable);
    }

    @Test
    void applyDiscount_rejectsBelowMinAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                CouponCatalog.applyDiscount(BigDecimal.valueOf(50), "cp-50"));
    }

    @Test
    void applyDiscount_withoutCoupon_returnsOriginal() {
        BigDecimal payable = CouponCatalog.applyDiscount(BigDecimal.valueOf(99), null);
        assertEquals(new BigDecimal("99.00"), payable);
    }
}
