package com.team13.context.service.frontend;

import java.util.List;
import java.util.Map;

public interface AdminPortalService {

    Map<String, Object> dashboard();

    List<Map<String, Object>> listUsers(String keyword, int page, int size);

    Map<String, Object> updateUserStatus(Long userId, int status);

    List<Map<String, Object>> listVerifications(String status, int page, int size);

    Map<String, Object> reviewVerification(Long id, int status, String note);

    List<Map<String, Object>> listComplaints(String status, int page, int size);

    Map<String, Object> handleComplaint(Long id, String status, String resultNote);

    List<Map<String, Object>> listRefunds(String status, int page, int size);

    Map<String, Object> decideRefund(Long id, String status);

    List<Map<String, Object>> listPendingCertificates(int page, int size);

    Map<String, Object> reviewCertificate(Long id, int status, String rejectReason);

    List<Map<String, Object>> listAnnouncements(int page, int size);

    Map<String, Object> createAnnouncement(Map<String, Object> body);

    Map<String, Object> updateAnnouncement(Long id, Map<String, Object> body);

    Map<String, Object> deleteAnnouncement(Long id);

    List<Map<String, Object>> listConfigs();

    Map<String, Object> upsertConfig(String key, String value, String description);

    List<Map<String, Object>> listAuditLogs(int page, int size);

    List<Map<String, Object>> listScenes();

    Map<String, Object> updateSceneStatus(Long id, int status);

    List<Map<String, Object>> listOrders(String status, int page, int size);

    List<Map<String, Object>> listCommunityPosts(Integer status, String keyword, int page, int size);

    Map<String, Object> updateCommunityPostStatus(Long postId, int status);

    List<Map<String, Object>> listQuestionBanks(Long sceneId);

    Map<String, Object> createQuestionBank(Map<String, Object> body);

    Map<String, Object> updateQuestionBankStatus(Long id, int status);

    List<Map<String, Object>> listQuestions(Long bankId);

    Map<String, Object> createQuestion(Long bankId, Map<String, Object> body);

    Map<String, Object> updateQuestionStatus(Long id, int status);
}
