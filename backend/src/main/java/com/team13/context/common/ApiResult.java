package com.team13.context.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应包装（占位，可按团队规范扩展）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {

    private int code;
    private String message;
    private T data;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(0, "success", data);
    }

    public static <T> ApiResult<T> fail(String message) {
        return new ApiResult<>(-1, message, null);
    }
}
