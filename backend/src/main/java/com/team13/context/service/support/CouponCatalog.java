package com.team13.context.service.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 收银台可用优惠券（与 {@code ContentCompatServiceImpl#listCoupons} 保持一致）。
 */
public final class CouponCatalog {

    private CouponCatalog() {
    }

    @Getter
    @RequiredArgsConstructor
    public static class CouponDef {
        private final String id;
        private final String title;
        private final BigDecimal discount;
        private final BigDecimal minAmount;
    }

    private static final List<CouponDef> ALL = List.of(
            new CouponDef("c1", "新人立减 20", BigDecimal.valueOf(20), BigDecimal.ZERO),
            new CouponDef("c2", "满 100 减 15", BigDecimal.valueOf(15), BigDecimal.valueOf(100)),
            new CouponDef("c3", "满 200 减 40", BigDecimal.valueOf(40), BigDecimal.valueOf(200)),
            new CouponDef("cp-20", "新人体验券", BigDecimal.valueOf(20), BigDecimal.ZERO),
            new CouponDef("cp-50", "进阶训练券", BigDecimal.valueOf(50), BigDecimal.valueOf(200)),
            new CouponDef("cp-30", "周末专享", BigDecimal.valueOf(30), BigDecimal.valueOf(150)));

    public static List<CouponDef> eligible(BigDecimal originalAmount) {
        BigDecimal amt = originalAmount != null ? originalAmount : BigDecimal.ZERO;
        return ALL.stream().filter(c -> amt.compareTo(c.getMinAmount()) >= 0).toList();
    }

    public static Optional<CouponDef> find(String couponId) {
        if (couponId == null || couponId.isBlank()) {
            return Optional.empty();
        }
        return ALL.stream().filter(c -> c.getId().equals(couponId.trim())).findFirst();
    }

    public static BigDecimal applyDiscount(BigDecimal originalAmount, String couponId) {
        BigDecimal original = originalAmount != null ? originalAmount : BigDecimal.ZERO;
        if (couponId == null || couponId.isBlank()) {
            return original.max(BigDecimal.valueOf(0.01)).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        CouponDef coupon = find(couponId)
                .orElseThrow(() -> new IllegalArgumentException("优惠券不可用或已失效"));
        if (original.compareTo(coupon.getMinAmount()) < 0) {
            throw new IllegalArgumentException("未达到优惠券使用门槛");
        }
        BigDecimal discount = coupon.getDiscount().min(original);
        BigDecimal payable = original.subtract(discount);
        if (payable.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            payable = BigDecimal.valueOf(0.01);
        }
        return payable.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
