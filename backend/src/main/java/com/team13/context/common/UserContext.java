package com.team13.context.common;

/**
 * 当前请求登录用户 ID（由 {@link com.team13.context.config.JwtInterceptor} 注入）。
 */
public final class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(Long userId) {
        CURRENT_USER.set(userId);
    }

    public static Long getUserId() {
        return CURRENT_USER.get();
    }

    /**
     * 从当前请求上下文获取登录用户 ID；未登录时抛出业务异常（由全局异常处理返回 400）。
     */
    public static Long requireUserId() {
        Long userId = CURRENT_USER.get();
        if (userId == null) {
            throw new IllegalArgumentException("未登录或登录状态已失效");
        }
        return userId;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
