package com.team13.context.controller;

import com.team13.context.common.ApiResult;
import com.team13.context.dto.TrainingStartResponse;
import com.team13.context.mapper.UserRoleMapper;
import com.team13.context.service.UserService;
import com.team13.context.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final UserRoleMapper userRoleMapper;

    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(@RequestBody Map<String, String> loginReq) {
        String username = loginReq.getOrDefault("username", "test_user");
        Long userId = userService.resolveOrCreateUserForLogin(username);
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(userId);
        if (roles.isEmpty()) {
            roles = List.of("USER");
        }
        String token = jwtUtils.generateToken(userId, username);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("username", username);
        data.put("userId", userId);
        data.put("roles", roles);
        return ApiResult.ok(data);
    }
}
