package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.team13.context.common.UserContext;
import com.team13.context.entity.Post;
import com.team13.context.entity.PostComment;
import com.team13.context.entity.PostLike;
import com.team13.context.mapper.PostCommentMapper;
import com.team13.context.mapper.PostLikeMapper;
import com.team13.context.mapper.PostMapper;
import com.team13.context.service.AuditLogService;
import com.team13.context.service.frontend.CommunityService;
import com.team13.context.service.support.AdminRoleSupport;
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

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final PostMapper postMapper;
    private final PostCommentMapper postCommentMapper;
    private final PostLikeMapper postLikeMapper;
    private final UserDisplayHelper userDisplayHelper;
    private final AdminRoleSupport adminRoleSupport;
    private final AuditLogService auditLogService;

    @Override
    public List<Map<String, Object>> listPosts(String type) {
        LambdaQueryWrapper<Post> q = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreatedAt);
        if ("highlight".equalsIgnoreCase(type)) {
            q.eq(Post::getKind, "HIGHLIGHT");
        } else if ("interview".equalsIgnoreCase(type)) {
            q.eq(Post::getKind, "EXPERIENCE");
        } else if ("insight".equalsIgnoreCase(type)) {
            q.eq(Post::getKind, "NOTE");
        }
        List<Post> posts = postMapper.selectPage(new Page<>(1, 30), q).getRecords();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Post post : posts) {
            rows.add(toFeedRow(post));
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> listPostComments(String postId) {
        Long pid = Long.parseLong(postId);
        List<PostComment> comments = postCommentMapper.selectList(
                new LambdaQueryWrapper<PostComment>()
                        .eq(PostComment::getPostId, pid)
                        .eq(PostComment::getStatus, 1)
                        .orderByAsc(PostComment::getCreatedAt));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (PostComment c : comments) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", String.valueOf(c.getId()));
            row.put("userName", userDisplayHelper.resolveNickname(c.getAuthorId(), false));
            row.put("content", c.getContent());
            row.put("createdAt", formatRelative(c.getCreatedAt()));
            rows.add(row);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> commentPost(String postId, Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        Long pid = Long.parseLong(postId);
        String content = String.valueOf(body.getOrDefault("content", "")).trim();
        if (content.length() < 2) {
            throw new IllegalArgumentException("评论内容过短");
        }
        LocalDateTime now = LocalDateTime.now();
        PostComment comment = new PostComment();
        comment.setPostId(pid);
        comment.setAuthorId(userId);
        comment.setContent(content);
        comment.setStatus(1);
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);
        postCommentMapper.insert(comment);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(comment.getId()));
        row.put("userName", userDisplayHelper.resolveNickname(userId, false));
        row.put("content", content);
        row.put("createdAt", "刚刚");
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> likePost(String postId, Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        Long pid = Long.parseLong(postId);
        boolean liked = body.get("liked") == null || Boolean.parseBoolean(String.valueOf(body.get("liked")));
        PostLike existing = postLikeMapper.selectOne(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, pid)
                        .eq(PostLike::getUserId, userId)
                        .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (liked && existing == null) {
            PostLike like = new PostLike();
            like.setPostId(pid);
            like.setUserId(userId);
            like.setCreatedAt(now);
            like.setUpdatedAt(now);
            postLikeMapper.insert(like);
        } else if (!liked && existing != null) {
            postLikeMapper.deleteById(existing.getId());
        }
        return Map.of("ok", true, "liked", liked);
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
            q.like(Post::getContent, keyword);
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
            row.put("content", post.getContent());
            row.put("status", post.getStatus());
            row.put("commentCount", postCommentMapper.selectCount(
                    new LambdaQueryWrapper<PostComment>().eq(PostComment::getPostId, post.getId())));
            row.put("likeCount", postLikeMapper.selectCount(
                    new LambdaQueryWrapper<PostLike>().eq(PostLike::getPostId, post.getId())));
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

    private Map<String, Object> toFeedRow(Post post) {
        long likes = postLikeMapper.selectCount(
                new LambdaQueryWrapper<PostLike>().eq(PostLike::getPostId, post.getId()));
        long comments = postCommentMapper.selectCount(
                new LambdaQueryWrapper<PostComment>()
                        .eq(PostComment::getPostId, post.getId())
                        .eq(PostComment::getStatus, 1));
        String type = mapKindToType(post.getKind());
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("name", userDisplayHelper.resolveNickname(post.getAuthorId(), false));
        user.put("medal", "学员");
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("likes", likes);
        stats.put("comments", comments);
        stats.put("collects", 0);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(post.getId()));
        row.put("type", type);
        row.put("user", user);
        row.put("title", extractTitle(post.getContent()));
        row.put("content", post.getContent());
        row.put("tags", List.of());
        row.put("stats", stats);
        row.put("has_video", "HIGHLIGHT".equals(post.getKind()));
        row.put("video_preview", "HIGHLIGHT".equals(post.getKind())
                ? "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                : null);
        row.put("published_at", formatRelative(post.getCreatedAt()));
        return row;
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

    private static String extractTitle(String content) {
        if (!StringUtils.hasText(content)) {
            return "社区动态";
        }
        String line = content.split("\n", 2)[0].trim();
        return line.length() > 40 ? line.substring(0, 40) + "…" : line;
    }

    private static String formatRelative(LocalDateTime time) {
        if (time == null) {
            return "刚刚";
        }
        Duration d = Duration.between(time, LocalDateTime.now());
        long hours = d.toHours();
        if (hours < 1) {
            return "刚刚";
        }
        if (hours < 24) {
            return hours + " 小时前";
        }
        return (hours / 24) + " 天前";
    }
}
