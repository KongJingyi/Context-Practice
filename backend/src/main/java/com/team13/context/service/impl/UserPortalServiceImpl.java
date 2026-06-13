package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team13.context.auth.ChineseIdCardValidator;
import com.team13.context.auth.VerifyUploadStorage;
import com.team13.context.common.UserContext;
import com.team13.context.config.AuthProperties;
import com.team13.context.entity.CoachProfile;
import com.team13.context.entity.CoachScene;
import com.team13.context.entity.Order;
import com.team13.context.mapper.CoachProfileMapper;
import com.team13.context.mapper.CoachSceneMapper;
import com.team13.context.mapper.RatingMapper;
import com.team13.context.entity.Rating;
import com.team13.context.entity.TrainingReport;
import com.team13.context.mapper.TrainingReportMapper;
import com.team13.context.service.support.CoachRoleSupport;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.auth.sms.SmsVerificationClient;
import com.team13.context.auth.SmsRateLimiter;
import com.team13.context.entity.User;
import com.team13.context.entity.UserAuth;
import com.team13.context.entity.UserProfile;
import com.team13.context.entity.VerificationRequest;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.TrainingRecordMapper;
import com.team13.context.mapper.UserMapper;
import com.team13.context.mapper.UserAuthMapper;
import com.team13.context.mapper.UserProfileMapper;
import com.team13.context.mapper.VerificationRequestMapper;
import com.team13.context.service.frontend.UserPortalService;
import com.team13.context.service.support.UserDisplayHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserPortalServiceImpl implements UserPortalService {

    private static final String VERIFY_KIND_ID_DOC = "USER_ID_DOC";
    private static final String PROVIDER_MOBILE = "MOBILE";
    private static final String SMS_SCENE_CHANGE_PHONE = "change_phone";

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final UserProfileMapper userProfileMapper;
    private final VerificationRequestMapper verificationRequestMapper;
    private final OrderMapper orderMapper;
    private final TrainingRecordMapper trainingRecordMapper;
    private final UserDisplayHelper userDisplayHelper;
    private final JdbcTemplate jdbcTemplate;
    private final VerifyUploadStorage verifyUploadStorage;
    private final AuthProperties authProperties;
    private final ObjectMapper objectMapper;
    private final CoachRoleSupport coachRoleSupport;
    private final CoachProfileMapper coachProfileMapper;
    private final CoachSceneMapper coachSceneMapper;
    private final RatingMapper ratingMapper;
    private final TrainingReportMapper trainingReportMapper;
    private final SmsVerificationClient smsVerificationClient;
    private final SmsRateLimiter smsRateLimiter;

    private final Map<Long, Map<String, Object>> privacyCache = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Object>> notifyCache = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> getDashboardProfile() {
        Long userId = UserContext.requireUserId();
        if (coachRoleSupport.isCoach(userId)) {
            return buildCoachDashboardProfile(userId);
        }
        User user = userMapper.selectById(userId);
        UserProfile profile = userProfileMapper.selectById(userId);
        long joinDays = user != null && user.getCreatedAt() != null
                ? ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), LocalDate.now()) + 1
                : 1;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", userDisplayHelper.resolveNickname(userId, false));
        data.put("joinDays", joinDays);
        data.put("rankTag", "进阶表达者");
        data.put("level", "Lv.8");
        data.put("finishedCourses", countCompletedOrders(userId));
        data.put("totalHoursLabel", "12.5h");
        data.put("avatarUrl", profile != null ? profile.getAvatarUrl() : null);
        return data;
    }

    @Override
    public Map<String, Object> getGrowthData(String period) {
        String p = period == null || period.isBlank() ? "month" : period;
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("asOfDate", LocalDate.now().toString());
        switch (p) {
            case "week" -> {
                data.put("xLabels", List.of("周一", "周二", "周三", "周四", "周五", "周六", "周日"));
                data.put("values", List.of(72, 74, 76, 75, 78, 80, 82));
            }
            case "year" -> {
                data.put("xLabels", List.of("1月", "3月", "5月", "7月", "9月", "11月"));
                data.put("values", List.of(60, 65, 70, 74, 78, 82));
            }
            default -> {
                data.put("xLabels", List.of("W1", "W2", "W3", "W4"));
                data.put("values", List.of(70, 74, 78, 82));
            }
        }
        return data;
    }

    @Override
    public List<Map<String, Object>> getAbilityTags() {
        Long userId = UserContext.requireUserId();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT tag, weight FROM ctx_user_ability_tag WHERE user_id = ? ORDER BY weight DESC",
                userId);
        if (rows.isEmpty()) {
            return List.of(
                    tag("逻辑清晰", 0.95),
                    tag("表达流畅", 0.88),
                    tag("抗压稳定", 0.82)
            );
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            int weight = ((Number) row.get("weight")).intValue();
            result.add(tag(String.valueOf(row.get("tag")), weight / 100.0));
        }
        return result;
    }

    @Override
    public Map<String, Object> getCompositeScore() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("score", 88);
        data.put("hint", "基于近 30 次陪练反馈");
        return data;
    }

    @Override
    public List<Map<String, Object>> getRecentReports() {
        Long userId = UserContext.requireUserId();
        if (coachRoleSupport.isCoach(userId)) {
            return buildCoachRecentReports(userId);
        }
        List<TrainingRecord> records = trainingRecordMapper.selectList(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .eq(TrainingRecord::getUserId, userId)
                        .orderByDesc(TrainingRecord::getCreatedAt)
                        .last("LIMIT 5"));
        List<Map<String, Object>> items = new ArrayList<>();
        for (TrainingRecord record : records) {
            Order order = record.getOrderId() != null ? orderMapper.selectById(record.getOrderId()) : null;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", record.getOrderId() != null ? String.valueOf(record.getOrderId()) : record.getRoomId());
            item.put("title", "训练报告");
            item.put("date", record.getStartedAt() != null ? record.getStartedAt().toLocalDate().toString() : LocalDate.now().toString());
            item.put("tutor", "陪练员");
            item.put("score", 85);
            if (order != null) {
                item.put("id", String.valueOf(order.getId()));
            }
            items.add(item);
        }
        if (items.isEmpty()) {
            items.add(Map.of("id", "demo-1", "title", "结构化表达陪练", "date", LocalDate.now().toString(), "tutor", "李教练", "score", 88));
        }
        return items;
    }

    @Override
    public Map<String, Object> getEditableProfile() {
        Long userId = UserContext.requireUserId();
        if (coachRoleSupport.isCoach(userId)) {
            return buildCoachEditableProfile(userId);
        }
        UserProfile profile = userProfileMapper.selectById(userId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("nickname", userDisplayHelper.resolveNickname(userId, false));
        data.put("avatarUrl", profile != null ? profile.getAvatarUrl() : "");
        data.put("trainingGoalIds", profile != null && profile.getTrainingGoal() != null
                ? List.of(profile.getTrainingGoal()) : List.of("job-interview"));
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateProfile(Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        if (coachRoleSupport.isCoach(userId)) {
            return updateCoachProfile(userId, body);
        }
        UserProfile profile = userProfileMapper.selectById(userId);
        LocalDateTime now = LocalDateTime.now();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setVerifiedStatus(0);
            profile.setCreatedAt(now);
        }
        if (body.get("nickname") != null) {
            profile.setNickname(String.valueOf(body.get("nickname")));
        }
        if (body.get("avatarUrl") != null) {
            profile.setAvatarUrl(String.valueOf(body.get("avatarUrl")));
        }
        if (body.get("trainingGoalIds") instanceof List<?> goals && !goals.isEmpty()) {
            profile.setTrainingGoal(String.valueOf(goals.get(0)));
        }
        profile.setUpdatedAt(now);
        if (userProfileMapper.selectById(userId) == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }
        return Map.of("ok", true);
    }

    @Override
    public Map<String, Object> getAuthStatus() {
        Long userId = UserContext.requireUserId();
        UserProfile profile = userProfileMapper.selectById(userId);
        VerificationRequest latest = verificationRequestMapper.selectOne(
                Wrappers.<VerificationRequest>lambdaQuery()
                        .eq(VerificationRequest::getUserId, userId)
                        .eq(VerificationRequest::getKind, VERIFY_KIND_ID_DOC)
                        .orderByDesc(VerificationRequest::getCreatedAt)
                        .last("LIMIT 1"));

        int status = profile != null && profile.getVerifiedStatus() != null ? profile.getVerifiedStatus() : 0;
        if (latest != null && latest.getStatus() != null) {
            int fromRequest = switch (latest.getStatus()) {
                case 1 -> 2;
                case 2 -> 3;
                default -> 1;
            };
            if (fromRequest != status) {
                status = fromRequest;
                if (profile == null) {
                    profile = new UserProfile();
                    profile.setUserId(userId);
                    profile.setCreatedAt(LocalDateTime.now());
                }
                profile.setVerifiedStatus(fromRequest);
                profile.setUpdatedAt(LocalDateTime.now());
                if (userProfileMapper.selectById(userId) == null) {
                    userProfileMapper.insert(profile);
                } else {
                    userProfileMapper.updateById(profile);
                }
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", switch (status) {
            case 1 -> "pending";
            case 2 -> "verified";
            case 3 -> "rejected";
            default -> "unverified";
        });
        if (status == 3 && latest != null && StringUtils.hasText(latest.getReviewNote())) {
            data.put("rejectReason", latest.getReviewNote());
        } else if (status == 3) {
            data.put("rejectReason", "资料不清晰，请重新上传");
        }
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitAuth(Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        String realName = stringVal(body.get("realName"));
        String idCard = stringVal(body.get("idCard"));
        String frontUrl = stringVal(body.get("idCardFrontUrl"));
        String backUrl = stringVal(body.get("idCardBackUrl"));

        if (!StringUtils.hasText(realName) || realName.trim().length() < 2) {
            throw new IllegalArgumentException("请填写真实姓名");
        }
        if (!ChineseIdCardValidator.isValid(idCard)) {
            throw new IllegalArgumentException("身份证号格式不正确");
        }
        if (!isOwnedVerifyUrl(userId, frontUrl) || !isOwnedVerifyUrl(userId, backUrl)) {
            throw new IllegalArgumentException("请先上传身份证正反面照片");
        }

        UserProfile profile = userProfileMapper.selectById(userId);
        LocalDateTime now = LocalDateTime.now();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setCreatedAt(now);
        }
        profile.setRealName(realName.trim());
        boolean autoApprove = authProperties.isVerifyDevAutoApprove();
        profile.setVerifiedStatus(autoApprove ? 2 : 1);
        profile.setUpdatedAt(now);
        if (userProfileMapper.selectById(userId) == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateById(profile);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("realName", realName.trim());
        payload.put("idCard", idCard.trim().toUpperCase());
        payload.put("idCardFrontUrl", frontUrl);
        payload.put("idCardBackUrl", backUrl);

        VerificationRequest request = new VerificationRequest();
        request.setUserId(userId);
        request.setKind(VERIFY_KIND_ID_DOC);
        request.setStatus(autoApprove ? 1 : 0);
        request.setSubmitPayload(writePayload(payload));
        request.setCreatedAt(now);
        request.setUpdatedAt(now);
        if (autoApprove) {
            request.setReviewedAt(now);
            request.setReviewNote("DEV_AUTO_APPROVE");
        }
        verificationRequestMapper.insert(request);

        return Map.of("status", autoApprove ? "verified" : "pending");
    }

    @Override
    public Map<String, Object> uploadAuthDocument(MultipartFile file, String side) {
        Long userId = UserContext.requireUserId();
        String url = verifyUploadStorage.store(userId, file);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("url", url);
        if (StringUtils.hasText(side)) {
            data.put("side", side.trim().toLowerCase());
        }
        return data;
    }

    private boolean isOwnedVerifyUrl(Long userId, String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }
        String prefix = authProperties.getVerifyUpload().getUrlPrefix();
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        String expectedPrefix = prefix + userId + "/";
        return url.startsWith(expectedPrefix);
    }

    private String writePayload(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("认证资料序列化失败", e);
        }
    }

    private static String stringVal(Object raw) {
        return raw != null ? String.valueOf(raw).trim() : "";
    }

    private static String requirePhone(Object raw) {
        String phone = stringVal(raw);
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("请输入正确手机号");
        }
        return phone;
    }

    private void assertPhoneNotBoundByOther(Long userId, String phone) {
        User existing = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getMobile, phone)
                        .ne(User::getId, userId)
                        .last("LIMIT 1"));
        if (existing != null) {
            throw new IllegalArgumentException("该手机号已被其他账号绑定");
        }
    }

    @Override
    public Map<String, Object> getSecurityInfo() {
        Long userId = UserContext.requireUserId();
        User user = userMapper.selectById(userId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("phoneMasked", UserDisplayHelper.maskPhone(user != null ? user.getMobile() : null));
        return data;
    }

    @Override
    public Map<String, Object> sendChangePhoneCode(Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        String phone = requirePhone(body.get("phone"));
        assertPhoneNotBoundByOther(userId, phone);

        smsRateLimiter.checkAllowed(phone);
        SmsVerificationClient.SmsSendResult sendResult = smsVerificationClient.send(phone, SMS_SCENE_CHANGE_PHONE);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("expireIn", authProperties.getSmsExpireSeconds());
        if (sendResult.devCode() != null) {
            data.put("devCode", sendResult.devCode());
        }
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updatePhone(Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        String phone = requirePhone(body.get("phone"));
        String code = stringVal(body.get("code"));
        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("请输入短信验证码");
        }
        if (!smsVerificationClient.verify(phone, SMS_SCENE_CHANGE_PHONE, code)) {
            throw new IllegalArgumentException("短信验证码错误或已过期");
        }
        assertPhoneNotBoundByOther(userId, phone);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setMobile(phone);
        user.setUpdatedAt(now);
        userMapper.updateById(user);

        UserAuth auth = userAuthMapper.selectOne(
                Wrappers.<UserAuth>lambdaQuery()
                        .eq(UserAuth::getUserId, userId)
                        .eq(UserAuth::getProvider, PROVIDER_MOBILE)
                        .last("LIMIT 1"));
        if (auth != null) {
            auth.setMobile(phone);
            auth.setUpdatedAt(now);
            userAuthMapper.updateById(auth);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ok", true);
        data.put("phoneMasked", UserDisplayHelper.maskPhone(phone));
        return data;
    }

    @Override
    public Map<String, Object> getPrivacySettings() {
        return privacyCache.computeIfAbsent(UserContext.requireUserId(), id -> defaultPrivacy());
    }

    @Override
    public Map<String, Object> patchPrivacySettings(Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        Map<String, Object> current = privacyCache.computeIfAbsent(userId, id -> defaultPrivacy());
        if (body.get("share_report_with_expert") != null) {
            current.put("share_report_with_expert", body.get("share_report_with_expert"));
        }
        if (body.get("anonymous_community") != null) {
            current.put("anonymous_community", body.get("anonymous_community"));
        }
        return Map.of("ok", true);
    }

    @Override
    public Map<String, Object> getNotificationSettings() {
        return notifyCache.computeIfAbsent(UserContext.requireUserId(), id -> defaultNotify());
    }

    @Override
    public Map<String, Object> patchNotificationSettings(Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        Map<String, Object> current = notifyCache.computeIfAbsent(userId, id -> defaultNotify());
        current.putAll(body);
        return Map.of("ok", true);
    }

    private int countCompletedOrders(Long userId) {
        Long count = orderMapper.selectCount(
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getUserId, userId)
                        .in(Order::getStatus, "COMPLETED", "IN_SERVICE"));
        return count != null ? count.intValue() : 0;
    }

    private static Map<String, Object> tag(String text, double weight) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("text", text);
        item.put("weight", weight);
        return item;
    }

    private static Map<String, Object> defaultPrivacy() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("share_report_with_expert", true);
        data.put("anonymous_community", false);
        return data;
    }

    private static Map<String, Object> defaultNotify() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sms_on_book", true);
        data.put("push_on_report", true);
        data.put("email_weekly", false);
        return data;
    }

    private Map<String, Object> buildCoachDashboardProfile(Long userId) {
        CoachProfile profile = coachProfileMapper.selectById(userId);
        User user = userMapper.selectById(userId);
        long joinDays = user != null && user.getCreatedAt() != null
                ? ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), LocalDate.now()) + 1
                : 1;
        Long completed = orderMapper.selectCount(
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getCoachId, userId)
                        .eq(Order::getStatus, "COMPLETED"));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", profile != null && StringUtils.hasText(profile.getNickname())
                ? profile.getNickname() : userDisplayHelper.resolveNickname(userId, true));
        data.put("nickname", data.get("name"));
        data.put("joinDays", joinDays);
        data.put("rankTag", "认证陪练");
        data.put("level", resolveCoachLevelName(profile));
        data.put("finishedCourses", completed != null ? completed.intValue() : 0);
        data.put("totalHoursLabel", (completed != null ? completed : 0) + "h");
        data.put("avatarUrl", profile != null ? profile.getAvatarUrl() : null);
        data.put("tags", splitTags(profile != null ? profile.getServiceTags() : null));
        data.put("goodReviewRate", computeCoachGoodReviewRate(userId));
        return data;
    }

    private Map<String, Object> buildCoachEditableProfile(Long userId) {
        CoachProfile profile = coachProfileMapper.selectById(userId);
        List<CoachScene> scenes = coachSceneMapper.selectList(
                Wrappers.<CoachScene>lambdaQuery()
                        .eq(CoachScene::getCoachId, userId)
                        .orderByAsc(CoachScene::getSortOrder));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("nickname", profile != null && StringUtils.hasText(profile.getNickname())
                ? profile.getNickname() : userDisplayHelper.resolveNickname(userId, true));
        data.put("avatarUrl", profile != null ? profile.getAvatarUrl() : "");
        data.put("bio", profile != null ? profile.getBio() : "");
        data.put("scenes", scenes.stream().map(CoachScene::getSceneKey).toList());
        data.put("tags", splitTags(profile != null ? profile.getServiceTags() : null));
        return data;
    }

    @Transactional(rollbackFor = Exception.class)
    protected Map<String, Object> updateCoachProfile(Long userId, Map<String, Object> body) {
        CoachProfile profile = coachProfileMapper.selectById(userId);
        LocalDateTime now = LocalDateTime.now();
        if (profile == null) {
            profile = new CoachProfile();
            profile.setUserId(userId);
            profile.setStatus(1);
            profile.setCreatedAt(now);
        }
        if (body.get("nickname") != null) {
            profile.setNickname(String.valueOf(body.get("nickname")));
        }
        if (body.get("avatarUrl") != null) {
            profile.setAvatarUrl(String.valueOf(body.get("avatarUrl")));
        }
        if (body.get("bio") != null) {
            profile.setBio(String.valueOf(body.get("bio")));
        }
        if (body.get("serviceTags") != null) {
            profile.setServiceTags(joinTagList(body.get("serviceTags")));
        } else if (body.get("tags") != null) {
            profile.setServiceTags(joinTagList(body.get("tags")));
        }
        profile.setUpdatedAt(now);
        if (coachProfileMapper.selectById(userId) == null) {
            coachProfileMapper.insert(profile);
        } else {
            coachProfileMapper.updateById(profile);
        }
        if (body.get("scenes") instanceof List<?> sceneList) {
            coachSceneMapper.delete(Wrappers.<CoachScene>lambdaQuery().eq(CoachScene::getCoachId, userId));
            int sort = 0;
            for (Object key : sceneList) {
                CoachScene scene = new CoachScene();
                scene.setCoachId(userId);
                scene.setSceneKey(String.valueOf(key));
                scene.setSortOrder(sort++);
                scene.setCreatedAt(now);
                scene.setUpdatedAt(now);
                coachSceneMapper.insert(scene);
            }
        }
        return Map.of("ok", true);
    }

    private List<Map<String, Object>> buildCoachRecentReports(Long coachId) {
        List<Order> orders = orderMapper.selectList(
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getCoachId, coachId)
                        .eq(Order::getStatus, "COMPLETED")
                        .orderByDesc(Order::getUpdatedAt)
                        .last("LIMIT 20"));
        List<Map<String, Object>> items = new ArrayList<>();
        for (Order order : orders) {
            TrainingRecord record = trainingRecordMapper.selectOne(
                    Wrappers.<TrainingRecord>lambdaQuery()
                            .eq(TrainingRecord::getOrderId, order.getId())
                            .last("LIMIT 1"));
            TrainingReport report = record != null
                    ? trainingReportMapper.selectOne(
                    Wrappers.<TrainingReport>lambdaQuery().eq(TrainingReport::getTrainingId, record.getId()))
                    : null;
            int score = 85;
            if (report != null && report.getScoreLogic() != null) {
                score = (report.getScoreLogic() + (report.getScoreFluency() != null ? report.getScoreFluency() : 4)
                        + (report.getScoreContent() != null ? report.getScoreContent() : 4)) * 20 / 3;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", order.getId());
            item.put("orderId", order.getId());
            item.put("title", resolveSceneName(order.getSceneId()));
            item.put("sceneName", item.get("title"));
            item.put("date", order.getScheduledStart() != null
                    ? order.getScheduledStart().toLocalDate().toString()
                    : LocalDate.now().toString());
            item.put("score", score);
            item.put("totalScore", score);
            items.add(item);
        }
        return items;
    }

    private String resolveCoachLevelName(CoachProfile profile) {
        if (profile == null || profile.getLevelId() == null) {
            return "正式陪练";
        }
        List<String> names = jdbcTemplate.query(
                "SELECT name FROM ctx_coach_level WHERE id = ?",
                (rs, rowNum) -> rs.getString("name"),
                profile.getLevelId());
        return names.isEmpty() ? "正式陪练" : names.get(0);
    }

    private double computeCoachGoodReviewRate(Long coachId) {
        List<Rating> ratings = ratingMapper.selectList(
                Wrappers.<Rating>lambdaQuery()
                        .eq(Rating::getCoachId, coachId)
                        .eq(Rating::getStatus, 1));
        if (ratings.isEmpty()) {
            return 0.96;
        }
        long good = ratings.stream()
                .filter(r -> (r.getScoreProfessional() + r.getScoreAttitude() + r.getScoreQuality()) >= 12)
                .count();
        return good / (double) ratings.size();
    }

    private String resolveSceneName(Long sceneId) {
        if (sceneId == null) {
            return "训练场景";
        }
        return jdbcTemplate.query(
                "SELECT name FROM ctx_scene WHERE id = ?",
                rs -> rs.next() ? rs.getString("name") : "训练场景",
                sceneId);
    }

    private static List<String> splitTags(String raw) {
        if (!StringUtils.hasText(raw)) {
            return List.of();
        }
        return List.of(raw.split(","));
    }

    private static String joinTagList(Object raw) {
        if (raw instanceof List<?> list) {
            return list.stream().map(String::valueOf).filter(StringUtils::hasText).reduce((a, b) -> a + "," + b).orElse("");
        }
        return String.valueOf(raw);
    }
}
