package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin
@RequiredArgsConstructor
public class AuthExtendedController {

    private final AuthService authService;

    @GetMapping("/captcha")
    public ApiResult<Map<String, Object>> captcha(HttpServletRequest request) {
        return ApiResult.ok(authService.createCaptcha(resolveClientIp(request)));
    }

    @PostMapping("/sms/send")
    public ApiResult<Map<String, Object>> sendSms(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(authService.sendSmsCode(body));
    }

    @GetMapping("/login/lock-status")
    public ApiResult<Map<String, Object>> lockStatus(@RequestParam(required = false) String phone) {
        return ApiResult.ok(authService.loginLockStatus(phone));
    }

    @PostMapping("/login/phone")
    public ApiResult<Map<String, Object>> loginPhone(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(authService.loginByPhone(body));
    }

    @PostMapping("/register/phone")
    public ApiResult<Map<String, Object>> registerPhone(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(authService.registerByPhone(body));
    }

    @GetMapping("/oauth/{provider}")
    public ApiResult<Map<String, Object>> oauth(@PathVariable String provider) {
        return ApiResult.badRequest("暂未开放 " + provider + " 登录，请使用手机号登录");
    }

    @PostMapping("/oauth/{provider}/callback")
    public ApiResult<Map<String, Object>> oauthCallback(
            @PathVariable String provider,
            @RequestBody Map<String, Object> body) {
        return ApiResult.badRequest("暂未开放 " + provider + " 登录");
    }

    static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
