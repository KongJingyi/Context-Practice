package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.common.UserContext;
import com.team13.context.dto.UserMeResponse;
import com.team13.context.entity.Role;
import com.team13.context.entity.User;
import com.team13.context.entity.UserProfile;
import com.team13.context.entity.UserRole;
import com.team13.context.mapper.RoleMapper;
import com.team13.context.mapper.UserMapper;
import com.team13.context.mapper.UserProfileMapper;
import com.team13.context.mapper.UserRoleMapper;
import com.team13.context.service.UserService;
import com.team13.context.service.support.UserDisplayHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE = "USER";

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final UserDisplayHelper userDisplayHelper;

    @Override
    public UserMeResponse getCurrentUser() {
        Long userId = UserContext.requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(userId);
        if (roles.isEmpty()) {
            roles = List.of(DEFAULT_ROLE);
        }
        boolean coachSide = roles.contains("COACH");
        return UserMeResponse.builder()
                .userId(userId)
                .username(user.getUsername())
                .nickname(userDisplayHelper.resolveNickname(userId, coachSide))
                .avatar(userDisplayHelper.resolveAvatar(userId, coachSide))
                .roles(roles)
                .phone(UserDisplayHelper.maskPhone(user.getMobile()))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long resolveOrCreateUserForLogin(String username) {
        String normalized = username == null || username.isBlank() ? "test_user" : username.trim();
        User existing = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, normalized).last("LIMIT 1"));
        if (existing != null) {
            ensureUserRole(existing.getId(), DEFAULT_ROLE);
            return existing.getId();
        }
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(normalized);
        user.setStatus(0);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
        ensureUserRole(user.getId(), DEFAULT_ROLE);
        return user.getId();
    }

    private void ensureUserRole(Long userId, String roleCode) {
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(userId);
        if (roles.contains(roleCode)) {
            return;
        }
        Role role = roleMapper.selectOne(
                Wrappers.<Role>lambdaQuery().eq(Role::getCode, roleCode).last("LIMIT 1"));
        if (role == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getId());
        userRole.setCreatedAt(now);
        userRole.setUpdatedAt(now);
        userRoleMapper.insert(userRole);
    }
}
