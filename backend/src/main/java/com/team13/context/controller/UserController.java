package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.dto.UserMeResponse;
import com.team13.context.service.UserService;
import com.team13.context.service.frontend.UserPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserPortalService userPortalService;

    @GetMapping("/me")
    public ApiResult<UserMeResponse> me() {
        return ApiResult.ok(userService.getCurrentUser());
    }

    @GetMapping("/profile")
    public ApiResult<Map<String, Object>> profile() {
        return ApiResult.ok(userPortalService.getDashboardProfile());
    }
}
