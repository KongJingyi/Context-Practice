package com.team13.context.metrics;

/**
 * 线上可观测性指标键（Redis 计数 / 聚合的技术储备，对应立项 KPI 样例）。
 * <p>
 * 命名约定：metrics:&lt;key&gt; 由 {@link BusinessMetricsRecorder} 统一前缀。
 */
public final class MetricKeys {

    private MetricKeys() {
    }

    /** 订单创建成功次数（ctx_order 新增成功） */
    public static final String ORDER_CREATE_SUCCESS = "order.create.success";

    /** 订单创建失败（校验失败、DB 异常等，按需埋点） */
    public static final String ORDER_CREATE_FAIL = "order.create.fail";

    /** 支付成功次数（ctx_payment 置 SUCCESS） */
    public static final String PAYMENT_SUCCESS = "payment.success";

    /** 支付失败 / 拒绝次数 */
    public static final String PAYMENT_FAIL = "payment.fail";

    /** 训练会话启动成功（通过支付校验后写入 ctx_training_record） */
    public static final String TRAINING_START_SUCCESS = "training.start.success";

    /** 训练会话启动被拒绝（订单状态、支付记录、金额不一致等） */
    public static final String TRAINING_START_REJECT = "training.start.reject";

    /** 训练正常结束次数 */
    public static final String TRAINING_END_SUCCESS = "training.end.success";

    /** 会话总时长（秒）累加，配合 TRAINING_END_SUCCESS 可算平均时长 */
    public static final String TRAINING_DURATION_SECONDS_SUM = "training.duration.seconds.sum";

    /** 收藏新增成功 */
    public static final String FAVORITE_CREATE_SUCCESS = "favorite.create.success";

    /** 审计类敏感读请求（管理员接口调用次数） */
    public static final String AUDIT_LOG_READ = "audit.log.read";
}
