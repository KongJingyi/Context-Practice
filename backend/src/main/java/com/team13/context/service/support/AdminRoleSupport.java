package com.team13.context.service.support;

import com.team13.context.common.ForbiddenOperationException;
import com.team13.context.common.UserContext;
import com.team13.context.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminRoleSupport {

    private final UserRoleMapper userRoleMapper;

    public void requireAdmin() {
        Long userId = UserContext.requireUserId();
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(userId);
        if (roles == null || !roles.contains("ADMIN")) {
            throw new ForbiddenOperationException("需要管理员权限");
        }
    }

    public Long requireAdminId() {
        requireAdmin();
        return UserContext.requireUserId();
    }
}
