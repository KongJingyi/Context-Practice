package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team13.context.common.UserContext;
import com.team13.context.entity.CoachProfile;
import com.team13.context.entity.Post;
import com.team13.context.entity.PostCollect;
import com.team13.context.entity.PostComment;
import com.team13.context.entity.PostLike;
import com.team13.context.mapper.CoachProfileMapper;
import com.team13.context.mapper.PostCollectMapper;
import com.team13.context.mapper.PostCommentMapper;
import com.team13.context.mapper.PostLikeMapper;
import com.team13.context.mapper.PostMapper;
import com.team13.context.service.AuditLogService;
import com.team13.context.service.frontend.CommunityService;
import com.team13.context.service.support.AdminRoleSupport;
import com.team13.context.service.support.PostMetaSupport;
import com.team13.context.service.support.UserDisplayHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostCollectMapper postCollectMapper;
    private final CoachProfileMapper coachProfileMapper;
    private final UserDisplayHelper userDisplayHelper;
    private final PostMetaSupport postMetaSupport;
    private final AdminRoleSupport adminRoleSupport;
    private final AuditLogService auditLogService;

    @Override
    public List<Map<String, Object>> listPosts(String type) {
        var wrapper = Wrappers.<Post>lambdaQuery().eq(Post::getStatus, 1);
        if ("highlight".equalsIgnoreCase(type)) {
            wrapper.eq(Post::getKind, "HIGHLIGHT");
        }
        if ("latest".equalsIgnoreCase(type)) {
            wrapper.orderByDesc(Post::getCreatedAt);
        } else {
            wrapper.orderByDesc(Post::getHotScore, Post::getCreatedAt);
        }
        wrapper.last("LIMIT 50");
        List<Post> posts = postMapper.selectList(wrapper);
        Long viewerId = UserContext.getUserId();
        Set<Long> liked = likedPostIds(viewerId, posts);
        Set<Long> collected = collectedPostIds(viewerId, posts);
        return posts.stream()
                .map(p -> toPostDto(p, liked.contains(p.getId()), collected.contains(p.getId())))
                .toList();
    }

    @Override
    public List<Map<String, Object>> listPostComments(String postId) {
        Long pid = parsePostId(postId);
        List<PostComment> rows = postCommentMapper.selectList(
                Wrappers.<PostComment>lambdaQuery()
                        .eq(PostComment::getPostId, pid)
                        .eq(PostComment::getStatus, 1)
                        .orderByAsc(PostComment::getCreatedAt));
        List<Map<String, Object>> out = new ArrayList<>();
        for (PostComment c : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", String.valueOf(c.getId()));
            item.put("userName", userDisplayHelper.resolveNickname(c.getAuthorId(), false));
            item.put("content", c.getContent());
            item.put("createdAt", formatRelativeTime(c.getCreatedAt()));
            out.add(item);
        }
        return out;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> commentPost(String postId, Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        Long pid = parsePostId(postId);
        Post post = requirePost(pid);
        String content = body != null ? String.valueOf(body.getOrDefault("content", "")).trim() : "";
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        if (content.length() > 1000) {
            content = content.substring(0, 1000);
        }
        Long parentId = null;
        if (body != null && body.get("parent_id") != null && !String.valueOf(body.get("parent_id")).isBlank()) {
            parentId = Long.parseLong(String.valueOf(body.get("parent_id")));
        }
        LocalDateTime now = LocalDateTime.now();
        PostComment comment = new PostComment();
        comment.setPostId(pid);
        comment.setAuthorId(userId);
        comment.setParentId(parentId);
        comment.setContent(content);
        comment.setStatus(1);
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);
        postCommentMapper.insert(comment);

        post.setCommentCount(safeCount(post.getCommentCount()) + 1);
        post.setUpdatedAt(now);
        postMapper.updateById(post);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", String.valueOf(comment.getId()));
        result.put("userName", userDisplayHelper.resolveNickname(userId, false));
        result.put("content", content);
        result.put("createdAt", "刚刚");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> likePost(String postId, Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        Long pid = parsePostId(postId);
        Post post = requirePost(pid);
        boolean liked = body != null && Boolean.TRUE.equals(body.get("liked"));
        LocalDateTime now = LocalDateTime.now();

        PostLike existing = postLikeMapper.selectOne(
                Wrappers.<PostLike>lambdaQuery()
                        .eq(PostLike::getPostId, pid)
                        .eq(PostLike::getUserId, userId)
                        .last("LIMIT 1"));

        if (liked && existing == null) {
            PostLike row = new PostLike();
            row.setPostId(pid);
            row.setUserId(userId);
            row.setCreatedAt(now);
            row.setUpdatedAt(now);
            postLikeMapper.insert(row);
            post.setLikeCount(safeCount(post.getLikeCount()) + 1);
        } else if (!liked && existing != null) {
            postLikeMapper.deleteById(existing.getId());
            post.setLikeCount(Math.max(0, safeCount(post.getLikeCount()) - 1));
        }
        post.setUpdatedAt(now);
        postMapper.updateById(post);
        return Map.of("ok", true, "liked", liked);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> collectPost(String postId, Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        Long pid = parsePostId(postId);
        Post post = requirePost(pid);
        boolean collected = body != null && Boolean.TRUE.equals(body.get("collected"));
        LocalDateTime now = LocalDateTime.now();

        PostCollect existing = postCollectMapper.selectOne(
                Wrappers.<PostCollect>lambdaQuery()
                        .eq(PostCollect::getPostId, pid)
                        .eq(PostCollect::getUserId, userId)
                        .last("LIMIT 1"));

        if (collected && existing == null) {
            PostCollect row = new PostCollect();
            row.setPostId(pid);
            row.setUserId(userId);
            row.setCreatedAt(now);
            row.setUpdatedAt(now);
            postCollectMapper.insert(row);
            post.setCollectCount(safeCount(post.getCollectCount()) + 1);
        } else if (!collected && existing != null) {
            postCollectMapper.deleteById(existing.getId());
            post.setCollectCount(Math.max(0, safeCount(post.getCollectCount()) - 1));
        }
        post.setUpdatedAt(now);
        postMapper.updateById(post);
        return Map.of("ok", true, "collected", collected);
    }

    @Override
    public List<Map<String, Object>> listAdminPosts(Integer status, String keyword, int page, int size) {
        adminRoleSupport.requireAdmin();
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 50);
        LambdaQueryWrapper<Post> q = new LambdaQueryWrapper<Post>().orderByDesc(Post::getCreatedAt);
        if (status != null) {
            q.eq(Post::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            q.and(w -> w.like(Post::getContent, keyword).or().like(Post::getTitle, keyword));
        }
        Page<Post> result = postMapper.selectPage(new Page<>(page, size), q);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Post post : result.getRecords()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", post.getId());
            row.put("authorId", post.getAuthorId());
            row.put("authorName", userDisplayHelper.resolveNickname(post.getAuthorId(), false));
            row.put("kind", post.getKind());
            row.put("type", mapKindToType(post.getKind()));
            row.put("title", post.getTitle());
            row.put("content", post.getContent());
            row.put("status", post.getStatus());
            row.put("commentCount", safeCount(post.getCommentCount()));
            row.put("likeCount", safeCount(post.getLikeCount()));
            row.put("createdAt", post.getCreatedAt());
            rows.add(row);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updatePostStatus(Long postId, int status) {
        adminRoleSupport.requireAdmin();
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new IllegalArgumentException("帖子不存在");
        }
        post.setStatus(status);
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(post);
        auditLogService.record("POST_STATUS", "POST", postId, "status=" + status);
        return Map.of("ok", true);
    }

    @Override
    public List<Map<String, Object>> listAdminComments(Long postId) {
        adminRoleSupport.requireAdmin();
        return listPostComments(String.valueOf(postId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateCommentStatus(Long commentId, int status) {
        adminRoleSupport.requireAdmin();
        PostComment comment = postCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        comment.setStatus(status);
        comment.setUpdatedAt(LocalDateTime.now());
        postCommentMapper.updateById(comment);
        auditLogService.record("COMMENT_STATUS", "COMMENT", commentId, "status=" + status);
        return Map.of("ok", true);
    }

    @Override
    public List<Map<String, Object>> listExpertTips() {
        List<CoachProfile> coaches = coachProfileMapper.selectList(
                Wrappers.<CoachProfile>lambdaQuery()
                        .eq(CoachProfile::getStatus, 1)
                        .orderByAsc(CoachProfile::getUserId)
                        .last("LIMIT 8"));
        List<Map<String, Object>> tips = new ArrayList<>();
        for (CoachProfile coach : coaches) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", String.valueOf(coach.getUserId()));
            item.put("name", coach.getNickname());
            item.put("title", resolveCoachTitle(coach));
            item.put("tip", resolveCoachTip(coach));
            tips.add(item);
        }
        return tips;
    }

    @Override
    public List<Map<String, Object>> listMyFootprint() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return List.of();
        }
        List<Post> posts = postMapper.selectList(
                Wrappers.<Post>lambdaQuery()
                        .eq(Post::getAuthorId, userId)
                        .eq(Post::getStatus, 1)
                        .orderByDesc(Post::getCreatedAt)
                        .last("LIMIT 10"));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Post p : posts) {
            Map<String, Object> meta = postMetaSupport.parseMeta(p.getMediaUrls());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", String.valueOf(p.getId()));
            item.put("date", p.getCreatedAt() != null ? p.getCreatedAt().toLocalDate().toString() : "");
            item.put("title", p.getTitle() != null ? p.getTitle() : "社区动态");
            item.put("type", postMetaSupport.kindToType(p.getKind(), meta));
            item.put("summary", summarize(p.getContent()));
            out.add(item);
        }
        return out;
    }

    private Map<String, Object> toPostDto(Post post, boolean liked, boolean collected) {
        Map<String, Object> meta = postMetaSupport.parseMeta(post.getMediaUrls());
        String type = postMetaSupport.kindToType(post.getKind(), meta);
        boolean displayAsStudent = Boolean.TRUE.equals(meta.get("display_as_student"));
        Long coachId = post.getCoachId() != null ? post.getCoachId() : post.getAuthorId();

        String displayName;
        String medal;
        if (displayAsStudent && meta.get("display_name") != null) {
            displayName = String.valueOf(meta.get("display_name"));
            medal = meta.get("display_medal") != null ? String.valueOf(meta.get("display_medal")) : null;
        } else if (coachId != null) {
            displayName = userDisplayHelper.resolveNickname(coachId, true);
            medal = meta.get("display_medal") != null
                    ? String.valueOf(meta.get("display_medal"))
                    : resolveCoachTitle(coachProfileMapper.selectById(coachId));
        } else {
            displayName = userDisplayHelper.resolveNickname(post.getAuthorId(), false);
            medal = meta.get("display_medal") != null ? String.valueOf(meta.get("display_medal")) : null;
        }

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("name", displayName);
        if (medal != null) {
            user.put("medal", medal);
        }
        String avatar = coachId != null && !displayAsStudent
                ? userDisplayHelper.resolveAvatar(coachId, true)
                : userDisplayHelper.resolveAvatar(post.getAuthorId(), false);
        if (avatar != null) {
            user.put("avatar", avatar);
        }

        Map<String, Object> stats = Map.of(
                "likes", safeCount(post.getLikeCount()),
                "comments", safeCount(post.getCommentCount()),
                "collects", safeCount(post.getCollectCount()));

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", String.valueOf(post.getId()));
        dto.put("type", type);
        dto.put("user", user);
        dto.put("title", post.getTitle());
        dto.put("content", post.getContent());
        dto.put("tags", postMetaSupport.tagsFromMeta(meta));
        dto.put("stats", stats);
        dto.put("published_at", formatRelativeTime(post.getCreatedAt()));
        dto.put("liked", liked);
        dto.put("collected", collected);
        if (coachId != null) {
            dto.put("coach_id", coachId);
        }
        if (meta.get("company") != null) {
            dto.put("company", meta.get("company"));
        }
        if (meta.get("role") != null) {
            dto.put("role", meta.get("role"));
        }
        if (meta.get("video_preview") != null) {
            dto.put("has_video", true);
            dto.put("video_preview", meta.get("video_preview"));
        }
        return dto;
    }

    private Set<Long> likedPostIds(Long userId, List<Post> posts) {
        if (userId == null || posts.isEmpty()) {
            return Set.of();
        }
        List<Long> ids = posts.stream().map(Post::getId).toList();
        return postLikeMapper.selectList(
                        Wrappers.<PostLike>lambdaQuery()
                                .eq(PostLike::getUserId, userId)
                                .in(PostLike::getPostId, ids))
                .stream()
                .map(PostLike::getPostId)
                .collect(Collectors.toSet());
    }

    private Set<Long> collectedPostIds(Long userId, List<Post> posts) {
        if (userId == null || posts.isEmpty()) {
            return Set.of();
        }
        List<Long> ids = posts.stream().map(Post::getId).toList();
        return postCollectMapper.selectList(
                        Wrappers.<PostCollect>lambdaQuery()
                                .eq(PostCollect::getUserId, userId)
                                .in(PostCollect::getPostId, ids))
                .stream()
                .map(PostCollect::getPostId)
                .collect(Collectors.toSet());
    }

    private Post requirePost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !Objects.equals(post.getStatus(), 1)) {
            throw new IllegalArgumentException("帖子不存在");
        }
        return post;
    }

    private static Long parsePostId(String postId) {
        if (!StringUtils.hasText(postId)) {
            throw new IllegalArgumentException("postId 无效");
        }
        String raw = postId.startsWith("p_") ? postId.substring(2) : postId;
        return Long.parseLong(raw);
    }

    private static int safeCount(Integer n) {
        return n != null ? n : 0;
    }

    private static String summarize(String content) {
        if (content == null) {
            return "";
        }
        String oneLine = content.replace('\n', ' ').trim();
        return oneLine.length() > 48 ? oneLine.substring(0, 48) + "…" : oneLine;
    }

    private static String formatRelativeTime(LocalDateTime time) {
        if (time == null) {
            return "刚刚";
        }
        Duration d = Duration.between(time, LocalDateTime.now());
        long minutes = d.toMinutes();
        if (minutes < 1) {
            return "刚刚";
        }
        if (minutes < 60) {
            return minutes + " 分钟前";
        }
        long hours = d.toHours();
        if (hours < 24) {
            return hours + " 小时前";
        }
        long days = d.toDays();
        if (days == 1) {
            return "昨天";
        }
        if (days < 7) {
            return days + " 天前";
        }
        return time.toLocalDate().toString();
    }

    private static String resolveCoachTitle(CoachProfile coach) {
        if (coach == null || coach.getBio() == null) {
            return "陪练专家";
        }
        String bio = coach.getBio();
        int idx = bio.indexOf('·');
        if (idx > 0) {
            return bio.substring(0, idx).trim();
        }
        return bio.length() > 16 ? bio.substring(0, 16) : bio;
    }

    private static String resolveCoachTip(CoachProfile coach) {
        if (coach == null) {
            return "保持结论先行，再展开论据。";
        }
        if (coach.getServiceTags() != null && !coach.getServiceTags().isBlank()) {
            String tag = coach.getServiceTags().split(",")[0].trim();
            return "擅长「" + tag + "」—— 练之前先写一句话结论。";
        }
        return coach.getBio() != null ? coach.getBio() : "多练一次，表达就更稳一分。";
    }

    private static String mapKindToType(String kind) {
        if ("HIGHLIGHT".equals(kind)) {
            return "highlight";
        }
        if ("EXPERIENCE".equals(kind)) {
            return "interview";
        }
        return "insight";
    }
}
