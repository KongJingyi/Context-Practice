package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team13.context.entity.Announcement;
import com.team13.context.entity.AuditLog;
import com.team13.context.entity.CoachCertificate;
import com.team13.context.entity.CoachProfile;
import com.team13.context.entity.Complaint;
import com.team13.context.entity.Order;
import com.team13.context.entity.Refund;
import com.team13.context.entity.Question;
import com.team13.context.entity.QuestionBank;
import com.team13.context.entity.Scene;
import com.team13.context.entity.SystemConfig;
import com.team13.context.entity.User;
import com.team13.context.entity.UserProfile;
import com.team13.context.entity.VerificationRequest;
import com.team13.context.mapper.AnnouncementMapper;
import com.team13.context.mapper.AuditLogMapper;
import com.team13.context.mapper.CoachCertificateMapper;
import com.team13.context.mapper.CoachProfileMapper;
import com.team13.context.mapper.ComplaintMapper;
import com.team13.context.entity.Payment;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.PaymentMapper;
import com.team13.context.mapper.PostMapper;
import com.team13.context.mapper.QuestionBankMapper;
import com.team13.context.mapper.QuestionMapper;
import com.team13.context.mapper.RefundMapper;
import com.team13.context.mapper.SceneMapper;
import com.team13.context.mapper.SystemConfigMapper;
import com.team13.context.mapper.UserMapper;
import com.team13.context.mapper.UserProfileMapper;
import com.team13.context.mapper.UserRoleMapper;
import com.team13.context.mapper.VerificationRequestMapper;
import com.team13.context.service.AuditLogService;
import com.team13.context.service.frontend.AdminPortalService;
import com.team13.context.service.frontend.CommunityService;
import com.team13.context.service.support.AdminRoleSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminPortalServiceImpl implements AdminPortalService {

    private final AdminRoleSupport adminRoleSupport;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserRoleMapper userRoleMapper;
    private final VerificationRequestMapper verificationRequestMapper;
    private final ComplaintMapper complaintMapper;
    private final RefundMapper refundMapper;
    private final CoachCertificateMapper coachCertificateMapper;
    private final CoachProfileMapper coachProfileMapper;
    private final AnnouncementMapper announcementMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final AuditLogMapper auditLogMapper;
    private final SceneMapper sceneMapper;
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final PostMapper postMapper;
    private final QuestionBankMapper questionBankMapper;
    private final QuestionMapper questionMapper;
    private final CommunityService communityService;

    @Override
    public Map<String, Object> dashboard() {
        adminRoleSupport.requireAdmin();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("userCount", userMapper.selectCount(null));
        stats.put("orderCount", orderMapper.selectCount(null));
        stats.put("pendingVerifications", verificationRequestMapper.selectCount(
                new LambdaQueryWrapper<VerificationRequest>().eq(VerificationRequest::getStatus, 0)));
        stats.put("pendingComplaints", complaintMapper.selectCount(
                new LambdaQueryWrapper<Complaint>().in(Complaint::getStatus, pendingComplaintStatuses())));
        stats.put("pendingRefunds", refundMapper.selectCount(
                new LambdaQueryWrapper<Refund>().eq(Refund::getStatus, "APPLIED")));
        stats.put("pendingCertificates", coachCertificateMapper.selectCount(
                new LambdaQueryWrapper<CoachCertificate>().eq(CoachCertificate::getStatus, 0)));
        stats.put("sceneCount", sceneMapper.selectCount(null));
        stats.put("postCount", postMapper.selectCount(null));
        return stats;
    }

    @Override
    public List<Map<String, Object>> listUsers(String keyword, int page, int size) {
        adminRoleSupport.requireAdmin();
        page = Math.max(page, 1);
        size = clampSize(size);
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<User>().orderByDesc(User::getCreatedAt);
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(User::getUsername, keyword).or().like(User::getMobile, keyword));
        }
        Page<User> result = userMapper.selectPage(new Page<>(page, size), q);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User u : result.getRecords()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", u.getId());
            row.put("username", u.getUsername());
            row.put("mobile", u.getMobile());
            row.put("status", u.getStatus());
            row.put("roles", userRoleMapper.selectRoleCodesByUserId(u.getId()));
            row.put("createdAt", u.getCreatedAt());
            UserProfile profile = userProfileMapper.selectOne(
                    new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, u.getId()).last("LIMIT 1"));
            row.put("nickname", profile != null ? profile.getNickname() : u.getUsername());
            rows.add(row);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateUserStatus(Long userId, int status) {
        adminRoleSupport.requireAdmin();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        auditLogService.record("USER_STATUS", "USER", userId, "status=" + status);
        return Map.of("ok", true, "userId", userId, "status", status);
    }

    @Override
    public List<Map<String, Object>> listVerifications(String status, int page, int size) {
        adminRoleSupport.requireAdmin();
        page = Math.max(page, 1);
        size = clampSize(size);
        LambdaQueryWrapper<VerificationRequest> q =
                new LambdaQueryWrapper<VerificationRequest>().orderByDesc(VerificationRequest::getCreatedAt);
        if ("pending".equalsIgnoreCase(status)) {
            q.eq(VerificationRequest::getStatus, 0);
        } else if ("approved".equalsIgnoreCase(status)) {
            q.eq(VerificationRequest::getStatus, 1);
        } else if ("rejected".equalsIgnoreCase(status)) {
            q.eq(VerificationRequest::getStatus, 2);
        }
        Page<VerificationRequest> result = verificationRequestMapper.selectPage(new Page<>(page, size), q);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (VerificationRequest v : result.getRecords()) {
            rows.add(verificationRow(v));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> reviewVerification(Long id, int status, String note) {
        Long adminId = adminRoleSupport.requireAdminId();
        VerificationRequest req = verificationRequestMapper.selectById(id);
        if (req == null) {
            throw new IllegalArgumentException("认证申请不存在");
        }
        req.setStatus(status);
        req.setReviewerId(adminId);
        req.setReviewNote(note);
        req.setReviewedAt(LocalDateTime.now());
        req.setUpdatedAt(LocalDateTime.now());
        verificationRequestMapper.updateById(req);
        syncUserVerifiedStatus(req.getUserId(), status);
        auditLogService.record("VERIFY_REVIEW", "VERIFICATION", id, "status=" + status);
        return Map.of("ok", true);
    }

    private void syncUserVerifiedStatus(Long userId, int reviewStatus) {
        if (userId == null) {
            return;
        }
        int verifiedStatus = switch (reviewStatus) {
            case 1 -> 2;
            case 2 -> 3;
            default -> 1;
        };
        LocalDateTime now = LocalDateTime.now();
        UserProfile profile = userProfileMapper.selectById(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setVerifiedStatus(verifiedStatus);
            profile.setCreatedAt(now);
            profile.setUpdatedAt(now);
            userProfileMapper.insert(profile);
        } else {
            profile.setVerifiedStatus(verifiedStatus);
            profile.setUpdatedAt(now);
            userProfileMapper.updateById(profile);
        }
    }

    @Override
    public List<Map<String, Object>> listComplaints(String status, int page, int size) {
        adminRoleSupport.requireAdmin();
        page = Math.max(page, 1);
        size = clampSize(size);
        LambdaQueryWrapper<Complaint> q = new LambdaQueryWrapper<Complaint>().orderByDesc(Complaint::getCreatedAt);
        if (StringUtils.hasText(status)) {
            applyComplaintStatusFilter(q, status);
        }
        Page<Complaint> result = complaintMapper.selectPage(new Page<>(page, size), q);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Complaint c : result.getRecords()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", c.getId());
            row.put("orderId", c.getOrderId());
            row.put("complainantId", c.getComplainantId());
            row.put("targetUserId", c.getTargetUserId());
            row.put("kind", c.getKind());
            row.put("content", c.getContent());
            row.put("status", c.getStatus());
            row.put("resultNote", c.getResultNote());
            row.put("createdAt", c.getCreatedAt());
            rows.add(row);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> handleComplaint(Long id, String status, String resultNote) {
        adminRoleSupport.requireAdminId();
        Complaint c = complaintMapper.selectById(id);
        if (c == null) {
            throw new IllegalArgumentException("投诉不存在");
        }
        c.setStatus(normalizeComplaintHandleStatus(status));
        c.setResultNote(resultNote);
        c.setHandledBy(adminRoleSupport.requireAdminId());
        c.setHandledAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        complaintMapper.updateById(c);
        auditLogService.record("COMPLAINT_HANDLE", "COMPLAINT", id, status);
        return Map.of("ok", true);
    }

    @Override
    public List<Map<String, Object>> listRefunds(String status, int page, int size) {
        adminRoleSupport.requireAdmin();
        page = Math.max(page, 1);
        size = clampSize(size);
        LambdaQueryWrapper<Refund> q = new LambdaQueryWrapper<Refund>().orderByDesc(Refund::getCreatedAt);
        if (StringUtils.hasText(status)) {
            q.eq(Refund::getStatus, status.toUpperCase());
        }
        Page<Refund> result = refundMapper.selectPage(new Page<>(page, size), q);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Refund r : result.getRecords()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", r.getId());
            row.put("orderId", r.getOrderId());
            row.put("amount", r.getAmount());
            row.put("reason", r.getReason());
            row.put("status", r.getStatus());
            row.put("createdAt", r.getCreatedAt());
            rows.add(row);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> decideRefund(Long id, String status) {
        Long adminId = adminRoleSupport.requireAdminId();
        Refund r = refundMapper.selectById(id);
        if (r == null) {
            throw new IllegalArgumentException("退款申请不存在");
        }
        String normalized = status.trim().toUpperCase();
        if ("APPROVED".equals(normalized)) {
            normalized = "REFUNDED";
        }
        r.setStatus(normalized);
        r.setDecidedBy(adminId);
        r.setDecidedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        refundMapper.updateById(r);
        Order order = orderMapper.selectById(r.getOrderId());
        if (order != null) {
            LocalDateTime now = LocalDateTime.now();
            if ("REFUNDED".equals(normalized)) {
                order.setStatus("REFUNDED");
                order.setUpdatedAt(now);
                orderMapper.updateById(order);
                Payment payment = paymentMapper.selectOne(
                        new LambdaQueryWrapper<Payment>().eq(Payment::getOrderId, order.getId()).last("LIMIT 1"));
                if (payment != null && "SUCCESS".equals(payment.getStatus())) {
                    payment.setStatus("CLOSED");
                    payment.setUpdatedAt(now);
                    paymentMapper.updateById(payment);
                }
            } else if ("REJECTED".equals(normalized) && "REFUNDING".equals(order.getStatus())) {
                order.setStatus("COMPLETED");
                order.setUpdatedAt(now);
                orderMapper.updateById(order);
            }
        }
        auditLogService.record("REFUND_DECIDE", "REFUND", id, normalized);
        return Map.of("ok", true, "status", normalized);
    }

    @Override
    public List<Map<String, Object>> listPendingCertificates(int page, int size) {
        adminRoleSupport.requireAdmin();
        page = Math.max(page, 1);
        size = clampSize(size);
        Page<CoachCertificate> result = coachCertificateMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<CoachCertificate>()
                        .eq(CoachCertificate::getStatus, 0)
                        .orderByDesc(CoachCertificate::getCreatedAt));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (CoachCertificate cert : result.getRecords()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", cert.getId());
            row.put("coachId", cert.getCoachId());
            row.put("kind", cert.getKind());
            row.put("title", cert.getTitle());
            row.put("verifyCode", cert.getVerifyCode());
            row.put("fileUrl", cert.getFileUrl());
            row.put("status", cert.getStatus());
            row.put("createdAt", cert.getCreatedAt());
            CoachProfile coach = coachProfileMapper.selectOne(
                    new LambdaQueryWrapper<CoachProfile>().eq(CoachProfile::getUserId, cert.getCoachId()).last("LIMIT 1"));
            row.put("coachName", coach != null ? coach.getNickname() : "陪练#" + cert.getCoachId());
            rows.add(row);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> reviewCertificate(Long id, int status, String rejectReason) {
        adminRoleSupport.requireAdmin();
        CoachCertificate cert = coachCertificateMapper.selectById(id);
        if (cert == null) {
            throw new IllegalArgumentException("证书不存在");
        }
        cert.setStatus(status);
        cert.setRejectReason(rejectReason);
        cert.setUpdatedAt(LocalDateTime.now());
        coachCertificateMapper.updateById(cert);
        auditLogService.record("CERT_REVIEW", "CERTIFICATE", id, "status=" + status);
        return Map.of("ok", true);
    }

    @Override
    public List<Map<String, Object>> listAnnouncements(int page, int size) {
        adminRoleSupport.requireAdmin();
        page = Math.max(page, 1);
        size = clampSize(size);
        Page<Announcement> result = announcementMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreatedAt));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Announcement a : result.getRecords()) {
            rows.add(announcementRow(a));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createAnnouncement(Map<String, Object> body) {
        Long adminId = adminRoleSupport.requireAdminId();
        Announcement a = new Announcement();
        a.setTitle(String.valueOf(body.getOrDefault("title", "")));
        a.setContent(String.valueOf(body.getOrDefault("content", "")));
        a.setKind(String.valueOf(body.getOrDefault("kind", "NOTICE")));
        a.setStatus(body.get("status") instanceof Number n ? n.intValue() : 1);
        a.setPublishedAt(LocalDateTime.now());
        a.setCreatedBy(adminId);
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        announcementMapper.insert(a);
        auditLogService.record("ANNOUNCE_CREATE", "ANNOUNCEMENT", a.getId(), a.getTitle());
        return announcementRow(a);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateAnnouncement(Long id, Map<String, Object> body) {
        adminRoleSupport.requireAdmin();
        Announcement a = announcementMapper.selectById(id);
        if (a == null) {
            throw new IllegalArgumentException("公告不存在");
        }
        if (body.containsKey("title")) {
            a.setTitle(String.valueOf(body.get("title")));
        }
        if (body.containsKey("content")) {
            a.setContent(String.valueOf(body.get("content")));
        }
        if (body.containsKey("kind")) {
            a.setKind(String.valueOf(body.get("kind")));
        }
        if (body.get("status") instanceof Number n) {
            a.setStatus(n.intValue());
        }
        a.setUpdatedAt(LocalDateTime.now());
        announcementMapper.updateById(a);
        auditLogService.record("ANNOUNCE_UPDATE", "ANNOUNCEMENT", id, null);
        return announcementRow(a);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteAnnouncement(Long id) {
        adminRoleSupport.requireAdmin();
        announcementMapper.deleteById(id);
        auditLogService.record("ANNOUNCE_DELETE", "ANNOUNCEMENT", id, null);
        return Map.of("ok", true);
    }

    @Override
    public List<Map<String, Object>> listConfigs() {
        adminRoleSupport.requireAdmin();
        List<SystemConfig> configs = systemConfigMapper.selectList(
                new LambdaQueryWrapper<SystemConfig>().orderByAsc(SystemConfig::getCfgKey));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SystemConfig c : configs) {
            rows.add(configRow(c));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> upsertConfig(String key, String value, String description) {
        Long adminId = adminRoleSupport.requireAdminId();
        SystemConfig existing = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getCfgKey, key).last("LIMIT 1"));
        if (existing == null) {
            existing = new SystemConfig();
            existing.setCfgKey(key);
            existing.setCfgValue(value);
            existing.setDescription(description);
            existing.setUpdatedBy(adminId);
            existing.setCreatedAt(LocalDateTime.now());
            existing.setUpdatedAt(LocalDateTime.now());
            systemConfigMapper.insert(existing);
        } else {
            existing.setCfgValue(value);
            if (description != null) {
                existing.setDescription(description);
            }
            existing.setUpdatedBy(adminId);
            existing.setUpdatedAt(LocalDateTime.now());
            systemConfigMapper.updateById(existing);
        }
        auditLogService.record("CONFIG_UPSERT", "CONFIG", existing.getId(), key);
        return configRow(existing);
    }

    @Override
    public List<Map<String, Object>> listAuditLogs(int page, int size) {
        adminRoleSupport.requireAdmin();
        page = Math.max(page, 1);
        size = clampSize(size);
        Page<AuditLog> result = auditLogMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<AuditLog>().orderByDesc(AuditLog::getCreatedAt));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AuditLog log : result.getRecords()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", log.getId());
            row.put("actorId", log.getActorId());
            row.put("action", log.getAction());
            row.put("targetType", log.getTargetType());
            row.put("targetId", log.getTargetId());
            row.put("detail", log.getDetail());
            row.put("createdAt", log.getCreatedAt());
            rows.add(row);
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> listScenes() {
        adminRoleSupport.requireAdmin();
        List<Scene> scenes = sceneMapper.selectList(
                new LambdaQueryWrapper<Scene>().orderByAsc(Scene::getId));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Scene s : scenes) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", s.getId());
            row.put("code", s.getCode());
            row.put("name", s.getName());
            row.put("description", s.getDescription());
            row.put("status", s.getStatus());
            rows.add(row);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateSceneStatus(Long id, int status) {
        adminRoleSupport.requireAdmin();
        Scene scene = sceneMapper.selectById(id);
        if (scene == null) {
            throw new IllegalArgumentException("场景不存在");
        }
        scene.setStatus(status);
        scene.setUpdatedAt(LocalDateTime.now());
        sceneMapper.updateById(scene);
        auditLogService.record("SCENE_STATUS", "SCENE", id, "status=" + status);
        return Map.of("ok", true, "id", id, "status", status);
    }

    @Override
    public List<Map<String, Object>> listOrders(String status, int page, int size) {
        adminRoleSupport.requireAdmin();
        page = Math.max(page, 1);
        size = clampSize(size);
        LambdaQueryWrapper<Order> q = new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreatedAt);
        if (StringUtils.hasText(status)) {
            q.eq(Order::getStatus, status.toUpperCase());
        }
        Page<Order> result = orderMapper.selectPage(new Page<>(page, size), q);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Order o : result.getRecords()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", o.getId());
            row.put("userId", o.getUserId());
            row.put("coachId", o.getCoachId());
            row.put("sceneId", o.getSceneId());
            row.put("amount", o.getAmount());
            row.put("status", o.getStatus());
            row.put("scheduledStart", o.getScheduledStart());
            row.put("createdAt", o.getCreatedAt());
            rows.add(row);
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> listCommunityPosts(Integer status, String keyword, int page, int size) {
        return communityService.listAdminPosts(status, keyword, page, size);
    }

    @Override
    public Map<String, Object> updateCommunityPostStatus(Long postId, int status) {
        return communityService.updatePostStatus(postId, status);
    }

    @Override
    public List<Map<String, Object>> listQuestionBanks(Long sceneId) {
        adminRoleSupport.requireAdmin();
        LambdaQueryWrapper<QuestionBank> q = new LambdaQueryWrapper<QuestionBank>().orderByAsc(QuestionBank::getId);
        if (sceneId != null) {
            q.eq(QuestionBank::getSceneId, sceneId);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (QuestionBank bank : questionBankMapper.selectList(q)) {
            Scene scene = sceneMapper.selectById(bank.getSceneId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", bank.getId());
            row.put("sceneId", bank.getSceneId());
            row.put("sceneName", scene != null ? scene.getName() : "");
            row.put("name", bank.getName());
            row.put("category", bank.getCategory());
            row.put("status", bank.getStatus());
            row.put("questionCount", questionMapper.selectCount(
                    new LambdaQueryWrapper<Question>().eq(Question::getBankId, bank.getId())));
            rows.add(row);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createQuestionBank(Map<String, Object> body) {
        adminRoleSupport.requireAdmin();
        QuestionBank bank = new QuestionBank();
        bank.setSceneId(Long.parseLong(String.valueOf(body.get("sceneId"))));
        bank.setName(String.valueOf(body.getOrDefault("name", "新题库")));
        bank.setCategory(String.valueOf(body.getOrDefault("category", "")));
        bank.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        bank.setCreatedAt(now);
        bank.setUpdatedAt(now);
        questionBankMapper.insert(bank);
        auditLogService.record("QBANK_CREATE", "QUESTION_BANK", bank.getId(), bank.getName());
        return Map.of("ok", true, "id", bank.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateQuestionBankStatus(Long id, int status) {
        adminRoleSupport.requireAdmin();
        QuestionBank bank = questionBankMapper.selectById(id);
        if (bank == null) {
            throw new IllegalArgumentException("题库不存在");
        }
        bank.setStatus(status);
        bank.setUpdatedAt(LocalDateTime.now());
        questionBankMapper.updateById(bank);
        auditLogService.record("QBANK_STATUS", "QUESTION_BANK", id, "status=" + status);
        return Map.of("ok", true);
    }

    @Override
    public List<Map<String, Object>> listQuestions(Long bankId) {
        adminRoleSupport.requireAdmin();
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getBankId, bankId)
                        .orderByAsc(Question::getId));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Question q : questions) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", q.getId());
            row.put("bankId", q.getBankId());
            row.put("title", q.getTitle());
            row.put("difficulty", q.getDifficulty());
            row.put("tags", q.getTags());
            row.put("status", q.getStatus());
            rows.add(row);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createQuestion(Long bankId, Map<String, Object> body) {
        adminRoleSupport.requireAdmin();
        Question q = new Question();
        q.setBankId(bankId);
        q.setTitle(String.valueOf(body.getOrDefault("title", "")).trim());
        if (!StringUtils.hasText(q.getTitle())) {
            throw new IllegalArgumentException("题目内容不能为空");
        }
        if (body.get("difficulty") instanceof Number n) {
            q.setDifficulty(n.intValue());
        }
        q.setTags(body.containsKey("tags") ? String.valueOf(body.get("tags")) : null);
        q.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        q.setCreatedAt(now);
        q.setUpdatedAt(now);
        questionMapper.insert(q);
        auditLogService.record("QUESTION_CREATE", "QUESTION", q.getId(), q.getTitle());
        return Map.of("ok", true, "id", q.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateQuestionStatus(Long id, int status) {
        adminRoleSupport.requireAdmin();
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new IllegalArgumentException("题目不存在");
        }
        q.setStatus(status);
        q.setUpdatedAt(LocalDateTime.now());
        questionMapper.updateById(q);
        auditLogService.record("QUESTION_STATUS", "QUESTION", id, "status=" + status);
        return Map.of("ok", true);
    }

    private static int clampSize(int size) {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }

    private Map<String, Object> verificationRow(VerificationRequest v) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", v.getId());
        row.put("userId", v.getUserId());
        row.put("kind", v.getKind());
        row.put("status", v.getStatus());
        row.put("submitPayload", v.getSubmitPayload());
        row.put("reviewNote", v.getReviewNote());
        row.put("createdAt", v.getCreatedAt());
        return row;
    }

    private Map<String, Object> announcementRow(Announcement a) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", a.getId());
        row.put("title", a.getTitle());
        row.put("content", a.getContent());
        row.put("kind", a.getKind());
        row.put("status", a.getStatus());
        row.put("publishedAt", a.getPublishedAt());
        row.put("createdAt", a.getCreatedAt());
        return row;
    }

    private Map<String, Object> configRow(SystemConfig c) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", c.getId());
        row.put("key", c.getCfgKey());
        row.put("value", c.getCfgValue());
        row.put("description", c.getDescription());
        row.put("updatedAt", c.getUpdatedAt());
        return row;
    }

    private static List<String> pendingComplaintStatuses() {
        return List.of("SUBMITTED", "IN_REVIEW", "OPEN");
    }

    private void applyComplaintStatusFilter(LambdaQueryWrapper<Complaint> q, String status) {
        String normalized = status.trim().toUpperCase();
        if ("OPEN".equals(normalized) || "PENDING".equals(normalized)) {
            q.in(Complaint::getStatus, pendingComplaintStatuses());
        } else if ("CLOSED".equals(normalized) || "RESOLVED".equals(normalized)) {
            q.in(Complaint::getStatus, List.of("RESOLVED", "REJECTED"));
        } else {
            q.eq(Complaint::getStatus, normalized);
        }
    }

    private static String normalizeComplaintHandleStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return "RESOLVED";
        }
        String normalized = status.trim().toUpperCase();
        if ("CLOSED".equals(normalized)) {
            return "RESOLVED";
        }
        return normalized;
    }
}
