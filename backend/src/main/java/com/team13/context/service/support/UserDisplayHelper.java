package com.team13.context.service.support;

import com.team13.context.entity.CoachProfile;
import com.team13.context.entity.User;
import com.team13.context.entity.UserProfile;
import com.team13.context.mapper.CoachProfileMapper;
import com.team13.context.mapper.UserMapper;
import com.team13.context.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDisplayHelper {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final CoachProfileMapper coachProfileMapper;

    public String resolveNickname(Long userId, boolean coachSide) {
        if (coachSide) {
            CoachProfile coachProfile = coachProfileMapper.selectById(userId);
            if (coachProfile != null && coachProfile.getNickname() != null && !coachProfile.getNickname().isBlank()) {
                return coachProfile.getNickname();
            }
        }
        UserProfile profile = userProfileMapper.selectById(userId);
        if (profile != null && profile.getNickname() != null && !profile.getNickname().isBlank()) {
            return profile.getNickname();
        }
        User user = userMapper.selectById(userId);
        if (user != null && user.getUsername() != null) {
            return user.getUsername();
        }
        return (coachSide ? "陪练" : "用户") + userId;
    }

    public String resolveAvatar(Long userId, boolean coachSide) {
        if (coachSide) {
            CoachProfile coachProfile = coachProfileMapper.selectById(userId);
            if (coachProfile != null && coachProfile.getAvatarUrl() != null) {
                return coachProfile.getAvatarUrl();
            }
        }
        UserProfile profile = userProfileMapper.selectById(userId);
        if (profile != null && profile.getAvatarUrl() != null) {
            return profile.getAvatarUrl();
        }
        return null;
    }

    public static String maskPhone(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }
}
