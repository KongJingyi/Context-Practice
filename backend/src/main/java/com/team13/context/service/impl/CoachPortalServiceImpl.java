package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.auth.CoachUploadStorage;
import com.team13.context.common.ForbiddenOperationException;
import com.team13.context.common.ResourceNotFoundException;
import com.team13.context.constant.BusinessStatuses;
import com.team13.context.entity.CoachCertificate;
import com.team13.context.entity.CoachProfile;
import com.team13.context.entity.CoachScheduleSlot;
import com.team13.context.entity.CoachWeeklySchedule;
import com.team13.context.entity.Order;
import com.team13.context.entity.Rating;
import com.team13.context.entity.RoomMaterial;
import com.team13.context.entity.Scene;
import com.team13.context.entity.TrainingNote;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.entity.TrainingReport;
import com.team13.context.mapper.CoachCertificateMapper;
import com.team13.context.mapper.CoachProfileMapper;
import com.team13.context.mapper.CoachScheduleSlotMapper;
import com.team13.context.mapper.CoachWeeklyScheduleMapper;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.RatingMapper;
import com.team13.context.mapper.RoomMaterialMapper;
import com.team13.context.mapper.SceneMapper;
import com.team13.context.mapper.TrainingNoteMapper;
import com.team13.context.mapper.TrainingRecordMapper;
import com.team13.context.mapper.TrainingReportMapper;
import com.team13.context.service.TrainingRecordingService;
import com.team13.context.service.frontend.CoachPortalService;
import com.team13.context.service.support.CoachRoleSupport;
import com.team13.context.service.support.OrderBookingSupport;
import com.team13.context.service.support.RoomContextSupport;
import com.team13.context.service.support.UserDisplayHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CoachPortalServiceImpl implements CoachPortalService {

    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.15");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final CoachRoleSupport coachRoleSupport;
    private final CoachProfileMapper coachProfileMapper;
    private final CoachWeeklyScheduleMapper weeklyScheduleMapper;
    private final CoachScheduleSlotMapper scheduleSlotMapper;
    private final CoachCertificateMapper certificateMapper;
    private final OrderMapper orderMapper;
    private final RatingMapper ratingMapper;
    private final TrainingRecordMapper trainingRecordMapper;
    private final TrainingReportMapper trainingReportMapper;
    private final TrainingNoteMapper trainingNoteMapper;
    private final RoomMaterialMapper roomMaterialMapper;
    private final SceneMapper sceneMapper;
    private final CoachUploadStorage coachUploadStorage;
    private final RoomContextSupport roomContextSupport;
    private final UserDisplayHelper userDisplayHelper;
    private final OrderBookingSupport orderBookingSupport;
    private final JdbcTemplate jdbcTemplate;
    private final TrainingRecordingService trainingRecordingService;

    @Override
    public Map<String, Object> getDashboard() {
        Long coachId = coachRoleSupport.requireCoachId();
        long totalSessions = countCoachOrders(coachId, BusinessStatuses.Order.COMPLETED);
        double totalHours = totalSessions * 1.0;
        double goodReviewRate = computeGoodReviewRate(coachId);
        Map<String, Object> income = buildIncomeSummary(coachId);
        BigDecimal monthIncome = (BigDecimal) income.get("monthNet");
        CoachProfile profile = coachProfileMapper.selectById(coachId);
        String levelName = resolveLevelName(profile);
        double levelProgress = Math.min(1.0, totalSessions / 50.0);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalSessions", totalSessions);
        data.put("totalHours", totalHours);
        data.put("goodReviewRate", goodReviewRate);
        data.put("levelName", levelName);
        data.put("levelProgress", levelProgress);
        data.put("monthIncome", monthIncome);
        data.put("lastTrainingId", resolveLastTrainingId(coachId));
        return data;
    }

    private Long resolveLastTrainingId(Long coachId) {
        Order latestOrder = orderMapper.selectOne(
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getCoachId, coachId)
                        .in(Order::getStatus, BusinessStatuses.Order.COMPLETED, BusinessStatuses.Order.IN_SERVICE)
                        .orderByDesc(Order::getUpdatedAt)
                        .last("LIMIT 1"));
        if (latestOrder == null) {
            return null;
        }
        TrainingRecord record = trainingRecordMapper.selectOne(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .eq(TrainingRecord::getOrderId, latestOrder.getId())
                        .orderByDesc(TrainingRecord::getCreatedAt)
                        .last("LIMIT 1"));
        return record != null ? record.getId() : null;
    }

    @Override
    public List<Map<String, Object>> getWeeklySchedule() {
        Long coachId = coachRoleSupport.requireCoachId();
        List<CoachWeeklySchedule> rows = weeklyScheduleMapper.selectList(
                Wrappers.<CoachWeeklySchedule>lambdaQuery()
                        .eq(CoachWeeklySchedule::getCoachId, coachId)
                        .orderByAsc(CoachWeeklySchedule::getDayOfWeek, CoachWeeklySchedule::getStartTime));
        List<Map<String, Object>> slots = new ArrayList<>();
        for (CoachWeeklySchedule row : rows) {
            slots.add(weeklySlotMap(row));
        }
        return slots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveWeeklySchedule(List<Map<String, Object>> slots) {
        Long coachId = coachRoleSupport.requireCoachId();
        weeklyScheduleMapper.delete(
                Wrappers.<CoachWeeklySchedule>lambdaQuery().eq(CoachWeeklySchedule::getCoachId, coachId));
        LocalDateTime now = LocalDateTime.now();
        if (slots != null) {
            for (Map<String, Object> slot : slots) {
                CoachWeeklySchedule row = new CoachWeeklySchedule();
                row.setCoachId(coachId);
                row.setDayOfWeek(((Number) slot.get("dayOfWeek")).intValue());
                row.setStartTime(LocalTime.parse(String.valueOf(slot.get("startTime")), TIME_FMT));
                row.setEndTime(LocalTime.parse(String.valueOf(slot.get("endTime")), TIME_FMT));
                Object enabled = slot.get("enabled");
                row.setEnabled(enabled instanceof Boolean b ? (b ? 1 : 0) : 1);
                row.setCreatedAt(now);
                row.setUpdatedAt(now);
                weeklyScheduleMapper.insert(row);
            }
        }
        syncBookableSlots(coachId);
        return Map.of("ok", true);
    }

    private void syncBookableSlots(Long coachId) {
        orderBookingSupport.syncFutureSlotsFromWeekly(coachId);
    }

    @Override
    public Map<String, Object> getIncomeSummary() {
        Long coachId = coachRoleSupport.requireCoachId();
        return buildIncomeSummary(coachId);
    }

    private Map<String, Object> buildIncomeSummary(Long coachId) {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<Order> orders = orderMapper.selectList(
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getCoachId, coachId)
                        .ge(Order::getPayAt, monthStart)
                        .in(Order::getStatus,
                                BusinessStatuses.Order.PAID,
                                BusinessStatuses.Order.IN_SERVICE,
                                BusinessStatuses.Order.COMPLETED));
        BigDecimal monthTotal = BigDecimal.ZERO;
        BigDecimal monthNet = BigDecimal.ZERO;
        BigDecimal pending = BigDecimal.ZERO;
        for (Order order : orders) {
            BigDecimal paid = order.getAmount() != null ? order.getAmount() : BigDecimal.ZERO;
            BigDecimal net = order.getCoachIncomeAmount() != null && order.getCoachIncomeAmount().signum() > 0
                    ? order.getCoachIncomeAmount()
                    : paid.multiply(BigDecimal.ONE.subtract(PLATFORM_FEE_RATE)).setScale(2, RoundingMode.HALF_UP);
            monthTotal = monthTotal.add(paid);
            if (BusinessStatuses.Order.COMPLETED.equals(order.getStatus())) {
                monthNet = monthNet.add(net);
            } else {
                pending = pending.add(net);
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("monthTotal", monthTotal);
        data.put("monthNet", monthNet);
        data.put("pendingAmount", pending);
        data.put("orderCount", orders.size());
        return data;
    }

    @Override
    public List<Map<String, Object>> listIncomeRecords() {
        Long coachId = coachRoleSupport.requireCoachId();
        List<Order> orders = orderMapper.selectList(
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getCoachId, coachId)
                        .in(Order::getStatus,
                                BusinessStatuses.Order.PAID,
                                BusinessStatuses.Order.IN_SERVICE,
                                BusinessStatuses.Order.COMPLETED)
                        .orderByDesc(Order::getPayAt, Order::getCreatedAt)
                        .last("LIMIT 50"));
        List<Map<String, Object>> records = new ArrayList<>();
        for (Order order : orders) {
            BigDecimal paid = order.getAmount() != null ? order.getAmount() : BigDecimal.ZERO;
            BigDecimal fee = order.getPlatformFeeAmount() != null && order.getPlatformFeeAmount().signum() > 0
                    ? order.getPlatformFeeAmount()
                    : paid.multiply(PLATFORM_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal net = order.getCoachIncomeAmount() != null && order.getCoachIncomeAmount().signum() > 0
                    ? order.getCoachIncomeAmount()
                    : paid.subtract(fee);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", order.getId());
            row.put("orderId", order.getId());
            row.put("sceneName", resolveSceneName(order.getSceneId()));
            row.put("userName", userDisplayHelper.resolveNickname(order.getUserId(), false));
            row.put("paidAmount", paid);
            row.put("platformFee", fee);
            row.put("netAmount", net);
            row.put("status", BusinessStatuses.Order.COMPLETED.equals(order.getStatus()) ? "SETTLED" : "PENDING");
            if (order.getPayAt() != null) {
                row.put("settledAt", order.getPayAt().toLocalDate().toString());
            }
            records.add(row);
        }
        return records;
    }

    @Override
    public List<Map<String, Object>> listCertificates() {
        Long coachId = coachRoleSupport.requireCoachId();
        List<CoachCertificate> certs = certificateMapper.selectList(
                Wrappers.<CoachCertificate>lambdaQuery()
                        .eq(CoachCertificate::getCoachId, coachId)
                        .orderByDesc(CoachCertificate::getCreatedAt));
        List<Map<String, Object>> items = new ArrayList<>();
        for (CoachCertificate cert : certs) {
            items.add(certificateMap(cert));
        }
        return items;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitCertificate(String type, String title, String verifyCode, MultipartFile file) {
        Long coachId = coachRoleSupport.requireCoachId();
        String fileUrl = file != null && !file.isEmpty()
                ? coachUploadStorage.store(coachId, "certificates", file)
                : "";
        if (!StringUtils.hasText(fileUrl) && !StringUtils.hasText(verifyCode)) {
            throw new IllegalArgumentException("请上传文件或填写验证码");
        }
        LocalDateTime now = LocalDateTime.now();
        CoachCertificate cert = new CoachCertificate();
        cert.setCoachId(coachId);
        cert.setKind("AWARD".equalsIgnoreCase(type) ? "AWARD" : "XUEXIN");
        cert.setTitle(StringUtils.hasText(title) ? title : ("XUEXIN".equals(cert.getKind()) ? "学信网认证" : "比赛证书"));
        cert.setVerifyCode(verifyCode);
        cert.setFileUrl(fileUrl);
        cert.setStatus(0);
        cert.setCreatedAt(now);
        cert.setUpdatedAt(now);
        certificateMapper.insert(cert);
        return Map.of("ok", true, "id", cert.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> replyRating(Long ratingId, String reply) {
        Long coachId = coachRoleSupport.requireCoachId();
        Rating rating = requireCoachRating(ratingId, coachId);
        rating.setCoachReply(reply);
        rating.setUpdatedAt(LocalDateTime.now());
        ratingMapper.updateById(rating);
        return Map.of("ok", true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> appealRating(Long ratingId, String reason) {
        Long coachId = coachRoleSupport.requireCoachId();
        Rating rating = requireCoachRating(ratingId, coachId);
        rating.setAppealStatus("PENDING");
        rating.setUpdatedAt(LocalDateTime.now());
        ratingMapper.updateById(rating);
        return Map.of("ok", true, "reason", reason != null ? reason : "");
    }

    @Override
    public Map<String, Object> getRecording(Long trainingId) {
        Long coachId = coachRoleSupport.requireCoachId();
        return trainingRecordingService.getRecording(trainingId, coachId);
    }

    @Override
    public List<Map<String, Object>> listRoomMaterials(String roomId) {
        coachRoleSupport.requireCoachId();
        roomContextSupport.loadRoomContext(roomId);
        List<RoomMaterial> materials = roomMaterialMapper.selectList(
                Wrappers.<RoomMaterial>lambdaQuery()
                        .eq(RoomMaterial::getRoomId, roomId)
                        .orderByDesc(RoomMaterial::getCreatedAt));
        List<Map<String, Object>> items = new ArrayList<>();
        for (RoomMaterial m : materials) {
            items.add(materialMap(m));
        }
        return items;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadRoomMaterial(String roomId, MultipartFile file) {
        Long coachId = coachRoleSupport.requireCoachId();
        var ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        String url = coachUploadStorage.store(coachId, "materials", file);
        LocalDateTime now = LocalDateTime.now();
        RoomMaterial material = new RoomMaterial();
        material.setRoomId(roomId);
        material.setTrainingId(ctx.training() != null ? ctx.training().getId() : null);
        material.setUploaderId(coachId);
        material.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        material.setFileUrl(url);
        material.setFileSize(file.getSize());
        material.setCreatedAt(now);
        material.setUpdatedAt(now);
        roomMaterialMapper.insert(material);
        return materialMap(material);
    }

    private Rating requireCoachRating(Long ratingId, Long coachId) {
        Rating rating = ratingMapper.selectById(ratingId);
        if (rating == null || !Objects.equals(rating.getCoachId(), coachId)) {
            throw new ResourceNotFoundException("评价不存在");
        }
        return rating;
    }

    private long countCoachOrders(Long coachId, String status) {
        Long count = orderMapper.selectCount(
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getCoachId, coachId)
                        .eq(Order::getStatus, status));
        return count != null ? count : 0;
    }

    private double computeGoodReviewRate(Long coachId) {
        List<Rating> ratings = ratingMapper.selectList(
                Wrappers.<Rating>lambdaQuery()
                        .eq(Rating::getCoachId, coachId)
                        .eq(Rating::getStatus, 1));
        if (ratings.isEmpty()) {
            return 0.96;
        }
        long good = ratings.stream().filter(r -> avgScore(r) >= 4).count();
        return good / (double) ratings.size();
    }

    private static double avgScore(Rating r) {
        return (r.getScoreProfessional() + r.getScoreAttitude() + r.getScoreQuality()) / 3.0;
    }

    private String resolveLevelName(CoachProfile profile) {
        if (profile == null || profile.getLevelId() == null) {
            return "正式陪练";
        }
        List<String> names = jdbcTemplate.query(
                "SELECT name FROM ctx_coach_level WHERE id = ?",
                (rs, rowNum) -> rs.getString("name"),
                profile.getLevelId());
        return names.isEmpty() ? "正式陪练" : names.get(0);
    }

    private String resolveSceneName(Long sceneId) {
        if (sceneId == null) {
            return "训练场景";
        }
        Scene scene = sceneMapper.selectById(sceneId);
        return scene != null ? scene.getName() : "训练场景";
    }

    private static Map<String, Object> weeklySlotMap(CoachWeeklySchedule row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", row.getId());
        m.put("dayOfWeek", row.getDayOfWeek());
        m.put("startTime", row.getStartTime().format(TIME_FMT));
        m.put("endTime", row.getEndTime().format(TIME_FMT));
        m.put("enabled", Objects.equals(row.getEnabled(), 1));
        return m;
    }

    private static Map<String, Object> certificateMap(CoachCertificate cert) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", cert.getId());
        m.put("type", "AWARD".equals(cert.getKind()) ? "AWARD" : "XUEXIN");
        m.put("title", cert.getTitle());
        m.put("verifyCode", cert.getVerifyCode());
        m.put("fileUrl", cert.getFileUrl());
        m.put("status", certStatus(cert.getStatus()));
        m.put("rejectReason", cert.getRejectReason());
        m.put("submittedAt", cert.getCreatedAt() != null ? cert.getCreatedAt().toLocalDate().toString() : null);
        return m;
    }

    private static String certStatus(Integer status) {
        if (status == null || status == 0) {
            return "PENDING";
        }
        if (status == 1) {
            return "APPROVED";
        }
        if (status == 2) {
            return "REJECTED";
        }
        return "NONE";
    }

    private static Map<String, Object> materialMap(RoomMaterial m) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", m.getId());
        row.put("name", m.getFileName());
        row.put("url", m.getFileUrl());
        row.put("sizeLabel", formatSize(m.getFileSize()));
        row.put("uploadedAt", m.getCreatedAt() != null
                ? m.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : "");
        return row;
    }

    private static String formatSize(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "—";
        }
        if (bytes >= 1024 * 1024) {
            return String.format("%.1f MB", bytes / 1024.0 / 1024.0);
        }
        return String.format("%.1f KB", bytes / 1024.0);
    }
}
