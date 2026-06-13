package com.team13.context.service.frontend;

import java.util.List;
import java.util.Map;

public interface CoachCatalogService {

    List<Map<String, Object>> listCoaches(
            String sceneId,
            Integer levelMin,
            Integer levelMax,
            Double stars,
            List<String> tags,
            Boolean todayOnly);

    Map<String, Object> getCoachDetail(String coachId);

    List<Map<String, Object>> listCoachReviews(String coachId);

    Map<String, Object> getExpertDetail(String expertId);

    List<Map<String, Object>> getExpertSlots(String expertId, String date);
}
