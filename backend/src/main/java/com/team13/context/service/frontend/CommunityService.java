package com.team13.context.service.frontend;

import java.util.List;
import java.util.Map;

public interface CommunityService {

    List<Map<String, Object>> listPosts(String type);

    List<Map<String, Object>> listPostComments(String postId);

    Map<String, Object> commentPost(String postId, Map<String, Object> body);

    Map<String, Object> likePost(String postId, Map<String, Object> body);

    List<Map<String, Object>> listAdminPosts(Integer status, String keyword, int page, int size);

    Map<String, Object> updatePostStatus(Long postId, int status);

    List<Map<String, Object>> listAdminComments(Long postId);

    Map<String, Object> updateCommentStatus(Long commentId, int status);
}
