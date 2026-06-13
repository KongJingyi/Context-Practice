package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.service.frontend.UserPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
@RequiredArgsConstructor
public class AuthVerifyController {

    private final UserPortalService userPortalService;

    @GetMapping("/auth/status")
    public ApiResult<Map<String, Object>> authStatus() {
        return ApiResult.ok(userPortalService.getAuthStatus());
    }

    @PostMapping("/auth/verify")
    public ApiResult<Map<String, Object>> verify(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(userPortalService.submitAuth(body));
    }

    @PostMapping("/auth/verify/upload")
    public ApiResult<Map<String, Object>> uploadVerifyDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "side", required = false) String side) {
        return ApiResult.ok(userPortalService.uploadAuthDocument(file, side));
    }

    @GetMapping("/v1/user/security")
    public ApiResult<Map<String, Object>> securityInfo() {
        return ApiResult.ok(userPortalService.getSecurityInfo());
    }

    @PostMapping("/v1/user/security/send-phone-code")
    public ApiResult<Map<String, Object>> sendChangePhoneCode(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(userPortalService.sendChangePhoneCode(body));
    }

    @PostMapping("/v1/user/security/update-phone")
    public ApiResult<Map<String, Object>> updatePhone(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(userPortalService.updatePhone(body));
    }

    @GetMapping("/v1/user/settings/privacy")
    public ApiResult<Map<String, Object>> privacySettings() {
        return ApiResult.ok(userPortalService.getPrivacySettings());
    }

    @PatchMapping("/v1/user/settings/privacy")
    public ApiResult<Map<String, Object>> patchPrivacy(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(userPortalService.patchPrivacySettings(body));
    }

    @GetMapping("/v1/user/settings/notifications")
    public ApiResult<Map<String, Object>> notificationSettings() {
        return ApiResult.ok(userPortalService.getNotificationSettings());
    }

    @PatchMapping("/v1/user/settings/notifications")
    public ApiResult<Map<String, Object>> patchNotifications(@RequestBody Map<String, Object> body) {
        return ApiResult.ok(userPortalService.patchNotificationSettings(body));
    }
}
