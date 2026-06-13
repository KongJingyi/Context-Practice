package com.team13.context.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常统一返回格式：
 * { code, message, data }
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ApiResult.badRequest(msg);
    }

    @ExceptionHandler(BindException.class)
    public ApiResult<Void> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ApiResult.badRequest(msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResult<Void> handleConstraintViolation(ConstraintViolationException e) {
        return ApiResult.badRequest(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResult<Void> handleIllegalArgument(IllegalArgumentException e) {
        return ApiResult.badRequest(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ApiResult<Void> handleIllegalState(IllegalStateException e) {
        log.warn("Illegal state: {}", e.getMessage());
        return ApiResult.badRequest(e.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ApiResult<Void> handleForbiddenOperation(ForbiddenOperationException e) {
        return ApiResult.forbidden(e.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResult<Void> handleResourceNotFound(ResourceNotFoundException e) {
        return ApiResult.notFound(e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ApiResult<Void> handleAccessDenied(AccessDeniedException e) {
        return ApiResult.forbidden("权限不足，无法访问该资源");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResult<Void> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation: {}", e.getMostSpecificCause().getMessage());
        String detail = e.getMostSpecificCause().getMessage();
        if (detail != null && detail.contains("uk_ctx_user_username")) {
            return ApiResult.badRequest("用户名冲突，请换一个手机号重试");
        }
        if (detail != null && detail.contains("mobile")) {
            return ApiResult.badRequest("该手机号已注册，请直接登录");
        }
        return ApiResult.badRequest("数据约束冲突，请避免重复或非法提交");
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception e) {
        log.error("Unhandled API exception", e);
        return ApiResult.serverError("服务器异常");
    }
}

