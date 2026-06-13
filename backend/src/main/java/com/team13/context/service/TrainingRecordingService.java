package com.team13.context.service;

import java.util.Map;

public interface TrainingRecordingService {

    Map<String, Object> getRecording(Long trainingId, Long requesterId);
}
