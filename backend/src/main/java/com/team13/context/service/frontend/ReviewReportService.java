package com.team13.context.service.frontend;

import java.util.List;
import java.util.Map;

public interface ReviewReportService {

    Map<String, Object> submitReview(Map<String, Object> body);

    Map<String, Object> getReportByOrderId(String orderId);

    Map<String, Object> getReviewStatus(Long orderId);
}
