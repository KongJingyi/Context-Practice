package com.team13.context.service;

import com.team13.context.dto.UserMeResponse;

public interface UserService {

    UserMeResponse getCurrentUser();

    /**
     * 登录时按用户名查找或创建用户，并确保拥有 USER 角色。
     */
    Long resolveOrCreateUserForLogin(String username);
}
