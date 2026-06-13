package com.team13.context.service.frontend;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CoachPortalService {

    Map<String, Object> getDashboard();

    List<Map<String, Object>> getWeeklySchedule();

    Map<String, Object> saveWeeklySchedule(List<Map<String, Object>> slots);

    Map<String, Object> getIncomeSummary();

    List<Map<String, Object>> listIncomeRecords();

    List<Map<String, Object>> listCertificates();

    Map<String, Object> submitCertificate(String type, String title, String verifyCode, MultipartFile file);

    Map<String, Object> replyRating(Long ratingId, String reply);

    Map<String, Object> appealRating(Long ratingId, String reason);

    Map<String, Object> getRecording(Long trainingId);

    List<Map<String, Object>> listRoomMaterials(String roomId);

    Map<String, Object> uploadRoomMaterial(String roomId, MultipartFile file);
}
