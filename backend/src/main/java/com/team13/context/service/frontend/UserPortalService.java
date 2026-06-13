package com.team13.context.service.frontend;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface UserPortalService {

    Map<String, Object> getDashboardProfile();

    Map<String, Object> getGrowthData(String period);

    List<Map<String, Object>> getAbilityTags();

    Map<String, Object> getCompositeScore();

    List<Map<String, Object>> getRecentReports();

    Map<String, Object> getEditableProfile();

    Map<String, Object> updateProfile(Map<String, Object> body);

    Map<String, Object> getAuthStatus();

    Map<String, Object> submitAuth(Map<String, Object> body);

    Map<String, Object> uploadAuthDocument(MultipartFile file, String side);

    Map<String, Object> getSecurityInfo();

    Map<String, Object> updatePassword(Map<String, Object> body);

    Map<String, Object> updatePhone(Map<String, Object> body);

    Map<String, Object> getPrivacySettings();

    Map<String, Object> patchPrivacySettings(Map<String, Object> body);

    Map<String, Object> getNotificationSettings();

    Map<String, Object> patchNotificationSettings(Map<String, Object> body);
}
