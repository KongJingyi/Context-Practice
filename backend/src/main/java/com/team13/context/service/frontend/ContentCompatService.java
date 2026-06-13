package com.team13.context.service.frontend;

import java.util.List;
import java.util.Map;

public interface ContentCompatService {

    List<Map<String, Object>> listCoupons(Number amount);

    Map<String, Object> prepayOrder(Map<String, Object> body);

    Map<String, Object> smartMatch(Map<String, Object> body);

    Map<String, Object> matchRecommend(String sceneId);

    List<Map<String, Object>> listQuestionCategories();

    List<Map<String, Object>> listQuestions(String category);

    Map<String, Object> analyzeVoice(Map<String, Object> body);

    Map<String, Object> optimizeText(Map<String, Object> body);

    Map<String, Object> saveHighlight(Map<String, Object> body);

    void reportPressureEvent(Map<String, Object> body);

    List<Map<String, Object>> listPosts(String type);

    List<Map<String, Object>> listPostComments(String postId);

    Map<String, Object> commentPost(String postId, Map<String, Object> body);

    Map<String, Object> likePost(String postId, Map<String, Object> body);

    List<Map<String, Object>> listExpertTips();

    List<Map<String, Object>> listMyFootprint();

    Map<String, Object> openComplaint(Map<String, Object> body);

    Map<String, Object> getComplaintStatus(String complaintId);

    Map<String, Object> listFavorites(String type);

    Map<String, Object> removeFavorite(String type, String id);

    Map<String, Object> fetchCaptcha();

    Map<String, Object> sendSmsCode(Map<String, Object> body);

    Map<String, Object> loginLockStatus(String phone);
}
