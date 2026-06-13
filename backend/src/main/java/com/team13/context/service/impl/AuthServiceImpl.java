package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.auth.AuthCaptchaGenerator;
import com.team13.context.auth.AuthCaptchaGenerator.CaptchaImage;
import com.team13.context.auth.AuthVerificationStore;
import com.team13.context.auth.AuthVerificationStore.LoginLockRecord;
import com.team13.context.auth.CaptchaRateLimiter;
import com.team13.context.auth.SmsRateLimiter;
import com.team13.context.auth.sms.SmsVerificationClient;
import com.team13.context.config.AuthProperties;
import com.team13.context.entity.Role;
import com.team13.context.entity.User;
import com.team13.context.entity.UserAuth;
import com.team13.context.entity.UserProfile;
import com.team13.context.entity.UserRole;
import com.team13.context.mapper.RoleMapper;
import com.team13.context.mapper.UserAuthMapper;
import com.team13.context.mapper.UserMapper;
import com.team13.context.mapper.UserProfileMapper;
import com.team13.context.mapper.UserRoleMapper;
import com.team13.context.service.AuthService;
import com.team13.context.service.support.UserDisplayHelper;
import com.team13.context.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String PROVIDER_MOBILE = "MOBILE";
    private static final String DEFAULT_ROLE = "USER";

    private final AuthProperties authProperties;
    private final AuthVerificationStore verificationStore;
    private final CaptchaRateLimiter captchaRateLimiter;
    private final SmsRateLimiter smsRateLimiter;
    private final SmsVerificationClient smsVerificationClient;
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final UserDisplayHelper userDisplayHelper;

    @Override
    public Map<String, Object> createCaptcha(String clientIp) {
        captchaRateLimiter.checkAllowed(clientIp);
        String code = AuthCaptchaGenerator.randomCode(authProperties.getCaptchaLength());
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        CaptchaImage image = AuthCaptchaGenerator.toCaptchaImage(
                code,
                authProperties.getCaptchaWidth(),
                authProperties.getCaptchaHeight());
        verificationStore.saveCaptcha(
                captchaKey,
                code,
                Duration.ofSeconds(authProperties.getCaptchaExpireSeconds()));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("captchaKey", captchaKey);
        data.put("captchaImage", image.getDataUrl());
        data.put("expireIn", authProperties.getCaptchaExpireSeconds());
        return data;
    }

    @Override
    public Map<String, Object> sendSmsCode(Map<String, Object> body) {
        String phone = requirePhone(body.get("phone"));
        String captcha = stringVal(body.get("captcha"));
        String captchaKey = stringVal(body.get("captchaKey"));
        String scene = stringVal(body.get("scene"));
        if (!StringUtils.hasText(scene)) {
            scene = "login";
        }
        if (!StringUtils.hasText(captcha) || !StringUtils.hasText(captchaKey)) {
            throw new IllegalArgumentException("请先填写图形验证码");
        }
        if (!verificationStore.verifyCaptcha(captchaKey, captcha)) {
            throw new IllegalArgumentException("图形验证码错误或已过期");
        }
        assertNotLocked(phone);
        if ("register".equalsIgnoreCase(scene) && findUserByPhone(phone) != null) {
            throw new IllegalArgumentException("该手机号已注册，请直接登录");
        }

        smsRateLimiter.checkAllowed(phone);

        SmsVerificationClient.SmsSendResult sendResult = smsVerificationClient.send(phone, scene);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("expireIn", authProperties.getSmsExpireSeconds());
        if (sendResult.devCode() != null) {
            data.put("devCode", sendResult.devCode());
        }
        return data;
    }

    @Override
    public Map<String, Object> loginLockStatus(String phone) {
        if (!StringUtils.hasText(phone)) {
            return lockView(LoginLockRecord.empty());
        }
        LoginLockRecord record = verificationStore.getLoginLock(normalizePhone(phone));
        if (record.isLockedNow()) {
            return lockView(record);
        }
        User user = findUserByPhone(phone);
        if (user != null) {
            UserAuth auth = findMobileAuth(user.getId());
            if (auth != null && auth.getLockedUntil() != null && auth.getLockedUntil().isAfter(LocalDateTime.now())) {
                long remain = Duration.between(LocalDateTime.now(), auth.getLockedUntil()).getSeconds();
                return Map.of(
                        "locked", true,
                        "remainSeconds", Math.max(1, remain),
                        "failCount", auth.getLoginFailCount() != null ? auth.getLoginFailCount() : authProperties.getMaxLoginAttempts());
            }
        }
        return lockView(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> loginByPhone(Map<String, Object> body) {
        String phone = requirePhone(body.get("phone"));
        String smsCode = stringVal(body.get("smsCode"));
        if (!StringUtils.hasText(smsCode)) {
            throw new IllegalArgumentException("请输入短信验证码");
        }
        assertNotLocked(phone);
        if (!verifySms(phone, "login", smsCode)) {
            recordLoginFail(phone);
            throw new IllegalArgumentException("短信验证码错误或已过期");
        }

        User user = findUserByPhone(phone);
        if (user == null) {
            throw new IllegalArgumentException("该手机号尚未注册，请先注册");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new IllegalArgumentException("账号已冻结，请联系客服");
        }
        touchLoginSuccess(user.getId());
        verificationStore.clearLoginLock(phone);
        return buildAuthResult(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> registerByPhone(Map<String, Object> body) {
        String phone = requirePhone(body.get("phone"));
        String smsCode = stringVal(body.get("smsCode"));
        if (!StringUtils.hasText(smsCode)) {
            throw new IllegalArgumentException("请输入短信验证码");
        }
        if (!verifySms(phone, "register", smsCode)) {
            throw new IllegalArgumentException("短信验证码错误或已过期");
        }

        User existing = findUserByPhone(phone);
        if (existing != null) {
            throw new IllegalArgumentException("该手机号已注册，请直接登录");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername("u_" + phone);
        user.setMobile(phone);
        user.setStatus(0);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);

        UserProfile profile = new UserProfile();
        profile.setUserId(user.getId());
        profile.setNickname("用户" + phone.substring(phone.length() - 4));
        profile.setVerifiedStatus(0);
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        userProfileMapper.insert(profile);

        UserAuth auth = new UserAuth();
        auth.setUserId(user.getId());
        auth.setProvider(PROVIDER_MOBILE);
        auth.setMobile(phone);
        auth.setLoginFailCount(0);
        auth.setLastLoginAt(now);
        auth.setCreatedAt(now);
        auth.setUpdatedAt(now);
        userAuthMapper.insert(auth);

        ensureUserRole(user.getId(), DEFAULT_ROLE);
        verificationStore.clearLoginLock(phone);
        return buildAuthResult(user);
    }

    private Map<String, Object> buildAuthResult(User user) {
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(user.getId());
        if (roles.isEmpty()) {
            roles = List.of(DEFAULT_ROLE);
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        Map<String, Object> userView = new LinkedHashMap<>();
        userView.put("userId", user.getId());
        userView.put("username", user.getUsername());
        userView.put("nickname", userDisplayHelper.resolveNickname(user.getId(), false));
        userView.put("phone", UserDisplayHelper.maskPhone(user.getMobile()));
        userView.put("roles", roles);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("user", userView);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("roles", roles);
        return data;
    }

    private void touchLoginSuccess(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        UserAuth auth = findMobileAuth(userId);
        if (auth != null) {
            auth.setLastLoginAt(now);
            auth.setLoginFailCount(0);
            auth.setLockedUntil(null);
            auth.setUpdatedAt(now);
            userAuthMapper.updateById(auth);
        }
    }

    private void recordLoginFail(String phone) {
        LoginLockRecord current = verificationStore.getLoginLock(phone);
        int failCount = current.failCount() + 1;
        long lockedUntil = 0L;
        if (failCount >= authProperties.getMaxLoginAttempts()) {
            lockedUntil = System.currentTimeMillis() + authProperties.getLockMinutes() * 60_000L;
        }
        LoginLockRecord next = new LoginLockRecord(failCount, lockedUntil);
        verificationStore.saveLoginLock(
                phone,
                next,
                Duration.ofMinutes(authProperties.getLockMinutes() + 5L));

        User user = findUserByPhone(phone);
        if (user != null) {
            UserAuth auth = findMobileAuth(user.getId());
            if (auth != null) {
                auth.setLoginFailCount(failCount);
                if (lockedUntil > 0) {
                    auth.setLockedUntil(LocalDateTime.now().plusMinutes(authProperties.getLockMinutes()));
                }
                auth.setUpdatedAt(LocalDateTime.now());
                userAuthMapper.updateById(auth);
            }
        }
    }

    private void assertNotLocked(String phone) {
        LoginLockRecord record = verificationStore.getLoginLock(phone);
        if (record.isLockedNow()) {
            throw new IllegalArgumentException("登录失败次数过多，请稍后再试");
        }
        User user = findUserByPhone(phone);
        if (user != null) {
            UserAuth auth = findMobileAuth(user.getId());
            if (auth != null && auth.getLockedUntil() != null && auth.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("登录失败次数过多，请稍后再试");
            }
        }
    }

    private boolean verifySms(String phone, String scene, String smsCode) {
        return smsVerificationClient.verify(phone, scene, smsCode);
    }

    private User findUserByPhone(String phone) {
        String normalized = normalizePhone(phone);
        User byMobile = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getMobile, normalized).last("LIMIT 1"));
        if (byMobile != null) {
            return byMobile;
        }
        UserAuth auth = userAuthMapper.selectOne(
                Wrappers.<UserAuth>lambdaQuery()
                        .eq(UserAuth::getProvider, PROVIDER_MOBILE)
                        .eq(UserAuth::getMobile, normalized)
                        .last("LIMIT 1"));
        if (auth == null) {
            return null;
        }
        return userMapper.selectById(auth.getUserId());
    }

    private UserAuth findMobileAuth(Long userId) {
        return userAuthMapper.selectOne(
                Wrappers.<UserAuth>lambdaQuery()
                        .eq(UserAuth::getUserId, userId)
                        .eq(UserAuth::getProvider, PROVIDER_MOBILE)
                        .last("LIMIT 1"));
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

    private static Map<String, Object> lockView(LoginLockRecord record) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("locked", record.isLockedNow());
        data.put("remainSeconds", record.remainSeconds());
        data.put("failCount", record.failCount());
        return data;
    }

    private static String requirePhone(Object raw) {
        String phone = normalizePhone(stringVal(raw));
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("请输入正确手机号");
        }
        return phone;
    }

    private static String normalizePhone(String phone) {
        return phone == null ? "" : phone.trim();
    }

    private static String stringVal(Object raw) {
        return raw == null ? "" : String.valueOf(raw).trim();
    }
}
