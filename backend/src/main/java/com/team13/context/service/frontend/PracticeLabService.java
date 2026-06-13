package com.team13.context.service.frontend;

import java.util.List;
import java.util.Map;

public interface PracticeLabService {

    List<Map<String, Object>> listQuestionCategories();

    List<Map<String, Object>> listQuestions(String category);

    Map<String, Object> analyzeVoice(Map<String, Object> body);

    Map<String, Object> optimizeText(Map<String, Object> body);
}
