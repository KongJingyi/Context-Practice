package com.team13.context.constant;

/**
 * 与 Flyway 脚本中订单、支付状态注释保持一致的业务常量。
 */
public final class BusinessStatuses {

    private BusinessStatuses() {
    }

    public static final class Order {
        public static final String PENDING_PAY = "PENDING_PAY";
        public static final String PAID = "PAID";
        public static final String CANCELLED = "CANCELLED";
        public static final String IN_SERVICE = "IN_SERVICE";
        public static final String COMPLETED = "COMPLETED";
        public static final String REFUNDING = "REFUNDING";
        public static final String REFUNDED = "REFUNDED";
    }

    public static final class Payment {
        public static final String INIT = "INIT";
        public static final String SUCCESS = "SUCCESS";
        public static final String FAILED = "FAILED";
        public static final String CLOSED = "CLOSED";
    }

    public static final class PaymentChannel {
        public static final String MOCK = "MOCK";
    }
}
