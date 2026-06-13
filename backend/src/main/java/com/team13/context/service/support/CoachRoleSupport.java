package com.team13.context.service.support;

import com.team13.context.common.ForbiddenOperationException;
import com.team13.context.common.UserContext;
import com.team13.context.mapper.CoachProfileMapper;
import com.team13.context.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoachRoleSupport {

    private final UserRoleMapper userRoleMapper;
    private final CoachProfileMapper coachProfileMapper;

    public boolean isCoach(Long userId) {
        if (userId == null) {
            return false;
        }
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(userId);
        return roles.contains("COACH") || coachProfileMapper.selectById(userId) != null;
    }

    public Long requireCoachId() {
        Long userId = UserContext.requireUserId();
        if (!isCoach(userId)) {
            throw new ForbiddenOperationException("需要陪练员身份");
        }
        return userId;
    }
}
