package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.common.UserContext;
import com.team13.context.dto.CreateOrderRequest;
import com.team13.context.dto.MockPayRequest;
import com.team13.context.entity.Complaint;
import com.team13.context.entity.Favorite;
import com.team13.context.entity.Order;
import com.team13.context.entity.Question;
import com.team13.context.entity.QuestionBank;
import com.team13.context.entity.Scene;
import com.team13.context.entity.TrainingNote;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.mapper.ComplaintMapper;
import com.team13.context.mapper.QuestionBankMapper;
import com.team13.context.mapper.QuestionMapper;
import com.team13.context.mapper.SceneMapper;
import com.team13.context.mapper.FavoriteMapper;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.TrainingNoteMapper;
import com.team13.context.mapper.TrainingRecordMapper;
import com.team13.context.service.OrderService;
import com.team13.context.service.frontend.CommunityService;
import com.team13.context.service.frontend.ContentCompatService;
import com.team13.context.service.frontend.CoachCatalogService;
import com.team13.context.service.frontend.support.FrontendCoachSupport;
import com.team13.context.service.support.CouponCatalog;
import com.team13.context.service.support.OrderBookingSupport;
import com.team13.context.service.support.SceneResolveSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContentCompatServiceImpl implements ContentCompatService {

    private final CoachCatalogService coachCatalogService;
    private final OrderService orderService;
    private final OrderBookingSupport orderBookingSupport;
    private final SceneResolveSupport sceneResolveSupport;
    private final FavoriteMapper favoriteMapper;
    private final ComplaintMapper complaintMapper;
    private final TrainingRecordMapper trainingRecordMapper;
    private final TrainingNoteMapper trainingNoteMapper;
    private final CommunityService communityService;
    private final QuestionBankMapper questionBankMapper;
    private final QuestionMapper questionMapper;
    private final SceneMapper sceneMapper;

    @Override
    public List<Map<String, Object>> listCoupons(Number amount) {
        BigDecimal amt = amount != null ? BigDecimal.valueOf(amount.doubleValue()) : BigDecimal.ZERO;
        List<Map<String, Object>> result = new ArrayList<>();
        for (CouponCatalog.CouponDef coupon : CouponCatalog.eligible(amt)) {
            result.add(coupon(
                    coupon.getId(),
                    coupon.getTitle(),
                    coupon.getDiscount().intValue(),
                    coupon.getMinAmount().intValue()));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> prepayOrder(Map<String, Object> body) {
        Object existingOrderId = body.get("existingOrderId");
        if (existingOrderId == null) {
            existingOrderId = body.get("orderId");
        }
        if (existingOrderId != null && !String.valueOf(existingOrderId).isBlank()) {
            MockPayRequest payReq = new MockPayRequest();
            payReq.setOrderId(Long.parseLong(String.valueOf(existingOrderId)));
            orderService.mockPay(payReq);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("orderId", String.valueOf(existingOrderId));
            result.put("orderNo", "EF" + existingOrderId);
            result.put("prepayId", "mock-prepay-" + existingOrderId);
            return result;
        }
        CreateOrderRequest req = new CreateOrderRequest();
        Long coachId = null;
        if (body.get("coachId") != null) {
            coachId = Long.parseLong(String.valueOf(body.get("coachId")));
        }
        if (body.get("expertId") != null) {
            coachId = FrontendCoachSupport.parseCoachUserId(String.valueOf(body.get("expertId")));
        }
        if (body.get("slotId") != null || body.get("date") != null) {
            if (coachId == null) {
                throw new IllegalArgumentException("无法识别陪练员，请返回专家页重试");
            }
            orderBookingSupport.requireActiveCoach(coachId);
        } else if (coachId != null) {
            orderBookingSupport.requireActiveCoach(coachId);
        }
        if (body.get("productId") != null) {
            req.setProductId(Long.parseLong(String.valueOf(body.get("productId"))));
        }
        req.setSceneId(sceneResolveSupport.resolveSceneId(body.get("sceneId"), body.get("sceneCode")));
        if (body.get("slotId") != null) {
            req.setSlotId(String.valueOf(body.get("slotId")));
        }
        if (body.get("date") != null) {
            req.setDate(String.valueOf(body.get("date")));
        }
        String couponId = body.get("couponId") != null ? String.valueOf(body.get("couponId")) : null;
        if (couponId != null && couponId.isBlank()) {
            couponId = null;
        }
        req.setCouponId(couponId);
        if (coachId != null) {
            req.setCoachId(coachId);
        }
        BigDecimal clientAmount = null;
        if (body.get("amount") != null) {
            clientAmount = new BigDecimal(String.valueOf(body.get("amount")));
        }
        if (coachId != null) {
            req.setAmount(orderBookingSupport.resolvePayableAmount(coachId, req.getProductId(), clientAmount, couponId));
        } else if (clientAmount != null) {
            req.setAmount(clientAmount);
        } else {
            req.setAmount(BigDecimal.valueOf(99));
        }
        Long orderId = orderService.createPendingOrder(req);
        MockPayRequest payReq = new MockPayRequest();
        payReq.setOrderId(orderId);
        orderService.mockPay(payReq);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", String.valueOf(orderId));
        result.put("orderNo", "EF" + orderId);
        result.put("prepayId", "mock-prepay-" + orderId);
        return result;
    }

    @Override
    public Map<String, Object> smartMatch(Map<String, Object> body) {
        String sceneId = body.get("sceneId") != null ? String.valueOf(body.get("sceneId")) : "job-tech-deep";
        List<Map<String, Object>> coaches = coachCatalogService.listCoaches(sceneId, null, null, null, null, null);
        List<Map<String, Object>> items = new ArrayList<>();
        int limit = Math.min(3, coaches.size());
        for (int i = 0; i < limit; i++) {
            Map<String, Object> coach = coaches.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("coach", coach);
            item.put("matchPercent", 95 - i * 7);
            item.put("reason", "擅长场景匹配，评分高");
            item.put("highlightTag", "STAR 法则");
            items.add(item);
        }
        return Map.of("items", items);
    }

    @Override
    public Map<String, Object> matchRecommend(String sceneId) {
        return smartMatch(Map.of("sceneId", sceneId != null ? sceneId : "job-tech-deep"));
    }

    @Override
    public List<Map<String, Object>> listQuestionCategories() {
        List<Scene> scenes = sceneMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<Scene>lambdaQuery()
                        .eq(Scene::getStatus, 1)
                        .orderByAsc(Scene::getId));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Scene scene : scenes) {
            String catId = scene.getCode() != null ? scene.getCode().toLowerCase() : String.valueOf(scene.getId());
            rows.add(cat(catId, scene.getName()));
        }
        if (rows.isEmpty()) {
            return List.of(
                    cat("interview", "面试题"),
                    cat("report", "汇报题"),
                    cat("pressure", "压力题")
            );
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> listQuestions(String category) {
        String c = category != null ? category.trim() : "interview";
        List<QuestionBank> banks = questionBankMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<QuestionBank>lambdaQuery()
                        .eq(QuestionBank::getStatus, 1)
                        .and(w -> w.eq(QuestionBank::getCategory, c)
                                .or().eq(QuestionBank::getCategory, c.toUpperCase())));
        if (banks.isEmpty()) {
            Scene scene = sceneMapper.selectOne(
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.<Scene>lambdaQuery()
                            .eq(Scene::getCode, c.toUpperCase())
                            .last("LIMIT 1"));
            if (scene != null) {
                banks = questionBankMapper.selectList(
                        com.baomidou.mybatisplus.core.toolkit.Wrappers.<QuestionBank>lambdaQuery()
                                .eq(QuestionBank::getSceneId, scene.getId())
                                .eq(QuestionBank::getStatus, 1));
            }
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (QuestionBank bank : banks) {
            List<Question> questions = questionMapper.selectList(
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.<Question>lambdaQuery()
                            .eq(Question::getBankId, bank.getId())
                            .eq(Question::getStatus, 1)
                            .last("LIMIT 20"));
            for (Question q : questions) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", String.valueOf(q.getId()));
                row.put("text", q.getTitle());
                row.put("title", q.getTitle());
                row.put("categoryId", c);
                row.put("category", c);
                row.put("difficulty", q.getDifficulty());
                rows.add(row);
            }
        }
        if (rows.isEmpty()) {
            return List.of(
                    question("q1", "请用 30 秒介绍你的核心优势", c),
                    question("q2", "项目延期时如何向上级汇报", c)
            );
        }
        return rows;
    }

    @Override
    public Map<String, Object> analyzeVoice(Map<String, Object> body) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("score", 82);
        data.put("fluency", 85);
        data.put("logic", 80);
        data.put("pressure", 78);
        data.put("suggestion", "语速略快，建议在关键结论前停顿 1 秒");
        data.put("radar", List.of(
                Map.of("label", "流畅度", "value", 85),
                Map.of("label", "逻辑", "value", 80),
                Map.of("label", "感染力", "value", 76)
        ));
        return data;
    }

    @Override
    public Map<String, Object> optimizeText(Map<String, Object> body) {
        String text = String.valueOf(body.getOrDefault("text", ""));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("original", text);
        data.put("optimized", "【结论先行】" + text);
        data.put("tips", List.of("先给出结论", "再用数据支撑", "最后补充行动项"));
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> saveHighlight(Map<String, Object> body) {
        normalizeHighlightBody(body);
        Long userId = UserContext.requireUserId();
        Long trainingId = body.get("trainingId") != null
                ? Long.parseLong(String.valueOf(body.get("trainingId")))
                : null;
        TrainingRecord record = null;
        if (trainingId == null && body.get("roomId") != null) {
            record = trainingRecordMapper.selectOne(
                    Wrappers.<TrainingRecord>lambdaQuery()
                            .eq(TrainingRecord::getRoomId, String.valueOf(body.get("roomId")))
                            .last("LIMIT 1"));
            if (record != null) {
                trainingId = record.getId();
            }
        } else if (trainingId != null) {
            record = trainingRecordMapper.selectById(trainingId);
        }
        if (body.get("startSec") == null && body.get("at_ms") != null && record != null) {
            resolveHighlightSeconds(body, record);
        }
        if (trainingId != null) {
            LocalDateTime now = LocalDateTime.now();
            TrainingNote note = new TrainingNote();
            note.setTrainingId(trainingId);
            note.setCoachId(userId);
            note.setNoteType(String.valueOf(body.getOrDefault("type", "HIGHLIGHT")));
            note.setLabel(String.valueOf(body.getOrDefault("label", "训练亮点")));
            note.setContent(String.valueOf(body.getOrDefault("content", "")));
            if (body.get("startSec") != null) {
                note.setStartSec(((Number) body.get("startSec")).intValue());
            }
            if (body.get("endSec") != null) {
                note.setEndSec(((Number) body.get("endSec")).intValue());
            }
            note.setCreatedAt(now);
            note.setUpdatedAt(now);
            trainingNoteMapper.insert(note);
            return Map.of("id", note.getId(), "ok", true);
        }
        return Map.of("id", UUID.randomUUID().toString(), "ok", true);
    }

    private void normalizeHighlightBody(Map<String, Object> body) {
        if (body.get("roomId") == null && body.get("room_id") != null) {
            body.put("roomId", String.valueOf(body.get("room_id")));
        }
        if (body.get("trainingId") == null && body.get("training_id") != null) {
            body.put("trainingId", body.get("training_id"));
        }
    }

    private void resolveHighlightSeconds(Map<String, Object> body, TrainingRecord record) {
        int durationSec = 15;
        if (body.get("duration_sec") != null) {
            durationSec = ((Number) body.get("duration_sec")).intValue();
        } else if (body.get("durationSec") != null) {
            durationSec = ((Number) body.get("durationSec")).intValue();
        }
        long atMs = ((Number) body.get("at_ms")).longValue();
        int startSec = 0;
        if (record.getStartedAt() != null) {
            long startedMs = record.getStartedAt()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            startSec = (int) Math.max(0, (atMs - startedMs) / 1000);
        }
        body.put("startSec", startSec);
        body.put("endSec", startSec + durationSec);
    }

    @Override
    public void reportPressureEvent(Map<String, Object> body) {
        // 前端兼容路径；实际压力事件走 /v1/rooms/{roomId}/pressure/*
    }

    @Override
    public List<Map<String, Object>> listPosts(String type) {
        return communityService.listPosts(type);
    }

    @Override
    public List<Map<String, Object>> listPostComments(String postId) {
        return communityService.listPostComments(postId);
    }

    @Override
    public Map<String, Object> commentPost(String postId, Map<String, Object> body) {
        return communityService.commentPost(postId, body);
    }

    @Override
    public Map<String, Object> likePost(String postId, Map<String, Object> body) {
        return communityService.likePost(postId, body);
    }

    @Override
    public List<Map<String, Object>> listExpertTips() {
        return List.of(
                tip("e1", "王若溪", "汇报时先给结论"),
                tip("e2", "周予安", "压力面保持 3 秒思考再答"),
                tip("e3", "林晚晴", "用 STAR 拆解项目经历")
        );
    }

    @Override
    public List<Map<String, Object>> listMyFootprint() {
        return List.of(
                footprint("f1", "完成压力面模拟", "2026-05-20"),
                footprint("f2", "发布社区心得", "2026-05-18"),
                footprint("f3", "收藏陪练员", "2026-05-15")
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> openComplaint(Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        Complaint complaint = new Complaint();
        complaint.setComplainantId(userId);
        if (body.get("order_id") != null) {
            complaint.setOrderId(Long.parseLong(String.valueOf(body.get("order_id"))));
        }
        complaint.setKind(String.valueOf(body.getOrDefault("type", "other")));
        complaint.setContent(String.valueOf(body.getOrDefault("description", "")));
        complaint.setStatus("SUBMITTED");
        LocalDateTime now = LocalDateTime.now();
        complaint.setCreatedAt(now);
        complaint.setUpdatedAt(now);
        complaintMapper.insert(complaint);
        return Map.of("id", complaint.getId(), "status", "pending");
    }

    @Override
    public Map<String, Object> getComplaintStatus(String complaintId) {
        Complaint complaint = complaintMapper.selectById(Long.parseLong(complaintId));
        List<Map<String, Object>> steps = List.of(
                step("提交投诉", "done", complaint != null ? complaint.getCreatedAt() : LocalDateTime.now()),
                step("平台受理", complaint != null ? "done" : "pending", null),
                step("调查核实", "pending", null),
                step("处理完成", "pending", null)
        );
        return Map.of("steps", steps);
    }

    @Override
    public Map<String, Object> listFavorites(String type) {
        Long userId = UserContext.requireUserId();
        List<Favorite> favorites = favoriteMapper.selectList(
                Wrappers.<Favorite>lambdaQuery().eq(Favorite::getUserId, userId));
        List<Map<String, Object>> experts = new ArrayList<>();
        List<Map<String, Object>> scenes = new ArrayList<>();
        for (Favorite fav : favorites) {
            if ("COACH".equals(fav.getTargetType())) {
                try {
                    Map<String, Object> coach = coachCatalogService.getCoachDetail(
                            FrontendCoachSupport.buildCoachId("default", fav.getTargetId()));
                    experts.add(Map.of(
                            "id", coach.get("id"),
                            "name", coach.get("name"),
                            "jobTitle", coach.get("jobTitle"),
                            "rating", coach.get("rating")));
                } catch (RuntimeException ignored) {
                    /* skip missing coach */
                }
            } else if ("SCENE".equals(fav.getTargetType())) {
                scenes.add(Map.of("id", String.valueOf(fav.getTargetId()), "name", "训练场景"));
            }
        }
        if ("experts".equals(type)) {
            return Map.of("experts", experts);
        }
        if ("scenes".equals(type)) {
            return Map.of("scenes", scenes);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("experts", experts.isEmpty() ? demoFavoriteExperts() : experts);
        data.put("scenes", scenes.isEmpty() ? demoFavoriteScenes() : scenes);
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> removeFavorite(String type, String id) {
        Long userId = UserContext.requireUserId();
        String targetType = "experts".equals(type) ? "COACH" : "SCENE";
        Long targetId = FrontendCoachSupport.parseCoachUserId(id);
        if (targetId == null) {
            targetId = Long.parseLong(id.replaceAll("\\D+", "0"));
        }
        favoriteMapper.delete(
                Wrappers.<Favorite>lambdaQuery()
                        .eq(Favorite::getUserId, userId)
                        .eq(Favorite::getTargetType, targetType)
                        .eq(Favorite::getTargetId, targetId));
        return Map.of("ok", true);
    }

    @Override
    public Map<String, Object> fetchCaptcha() {
        String key = UUID.randomUUID().toString();
        return Map.of(
                "captchaKey", key,
                "captchaImage", "data:image/png;base64,iVBORw0KGgo=");
    }

    @Override
    public Map<String, Object> sendSmsCode(Map<String, Object> body) {
        return Map.of("expireIn", 300);
    }

    @Override
    public Map<String, Object> loginLockStatus(String phone) {
        return Map.of("locked", false, "failCount", 0);
    }

    private static Map<String, Object> coupon(String id, String title, int discount, int minAmount) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("id", id);
        c.put("title", title);
        c.put("discountAmount", discount);
        c.put("minAmount", minAmount);
        return c;
    }

    private static Map<String, Object> cat(String id, String name) {
        return Map.of("id", id, "name", name);
    }

    private static Map<String, Object> question(String id, String title, String category) {
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("id", id);
        q.put("title", title);
        q.put("category", category);
        return q;
    }

    private static List<Map<String, Object>> demoPosts() {
        List<Map<String, Object>> posts = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Map<String, Object> post = new LinkedHashMap<>();
            post.put("id", "p" + i);
            post.put("author", "学员 " + i);
            post.put("content", "今天陪练收获：结论先行真的很重要 #" + i);
            post.put("likes", 10 + i * 3);
            post.put("comments", i);
            post.put("hasVideo", i % 2 == 0);
            post.put("videoPreview", "");
            post.put("publishedAt", LocalDateTime.now().minusDays(i).toString());
            post.put("kind", i <= 2 ? "hot" : "latest");
            posts.add(post);
        }
        return posts;
    }

    private static Map<String, Object> comment(String id, String content, String author) {
        return Map.of("id", id, "content", content, "author", author);
    }

    private static Map<String, Object> tip(String id, String name, String tip) {
        return Map.of("id", id, "name", name, "tip", tip);
    }

    private static Map<String, Object> footprint(String id, String title, String date) {
        return Map.of("id", id, "title", title, "date", date);
    }

    private static Map<String, Object> step(String title, String status, LocalDateTime at) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("title", title);
        s.put("status", status);
        if (at != null) {
            s.put("at", at.toString());
        }
        return s;
    }

    private static List<Map<String, Object>> demoFavoriteExperts() {
        return List.of(Map.of("id", "coach-default-1", "name", "李教练", "jobTitle", "结构化表达", "rating", 4.9));
    }

    private static List<Map<String, Object>> demoFavoriteScenes() {
        return List.of(Map.of("id", "job-tech-deep", "name", "技术面深度追问"));
    }
}
