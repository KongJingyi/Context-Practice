package com.team13.context.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应包装。
 *
 * 约定：
 * - code: 200 成功；400 参数/业务校验错误；403 权限不足；500 服务器异常
 * - message: 前端可直接展示的提示信息
 * - data: 业务数据（对象/数组/空）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {

    public static final int CODE_OK = 200;
    public static final int CODE_BAD_REQUEST = 400;
    public static final int CODE_FORBIDDEN = 403;
    public static final int CODE_NOT_FOUND = 404;

    public static final int CODE_SERVER_ERROR = 500;

    private int code;
    private String message;
    private T data;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(CODE_OK, "操作成功", data);
    }

    public static <T> ApiResult<T> fail(int code, String message) {
        return new ApiResult<>(code, message, null);
    }

    public static <T> ApiResult<T> badRequest(String message) {
        return fail(CODE_BAD_REQUEST, message == null || message.isBlank() ? "参数错误" : message);
    }

    public static <T> ApiResult<T> forbidden(String message) {
        return fail(CODE_FORBIDDEN, message == null || message.isBlank() ? "权限不足" : message);
    }

    public static <T> ApiResult<T> notFound(String message) {
        return fail(CODE_NOT_FOUND, message == null || message.isBlank() ? "资源不存在" : message);
    }

    public static <T> ApiResult<T> serverError(String message) {
        return fail(CODE_SERVER_ERROR, message == null || message.isBlank() ? "服务器异常" : message);
    }
}
