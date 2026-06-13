package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.common.ForbiddenOperationException;
import com.team13.context.common.ResourceNotFoundException;
import com.team13.context.entity.Order;
import com.team13.context.entity.TrainingNote;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.TrainingNoteMapper;
import com.team13.context.mapper.TrainingRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TrainingRecordingServiceImpl implements com.team13.context.service.TrainingRecordingService {

    private final TrainingRecordMapper trainingRecordMapper;
    private final TrainingNoteMapper trainingNoteMapper;
    private final OrderMapper orderMapper;

    @Override
    public Map<String, Object> getRecording(Long trainingId, Long requesterId) {
        TrainingRecord record = trainingRecordMapper.selectById(trainingId);
        if (record == null) {
            throw new ResourceNotFoundException("训练记录不存在");
        }
        assertCanView(record, requesterId);

        List<TrainingNote> notes = trainingNoteMapper.selectList(
                Wrappers.<TrainingNote>lambdaQuery()
                        .eq(TrainingNote::getTrainingId, trainingId)
                        .orderByAsc(TrainingNote::getStartSec));
        boolean expired = record.getRecordingExpiresAt() != null
                && record.getRecordingExpiresAt().isBefore(LocalDateTime.now());
        String baseUrl = !expired && StringUtils.hasText(record.getRecordingUrl())
                ? record.getRecordingUrl()
                : "";
        String recordingStatus = expired ? "EXPIRED" : record.getRecordingStatus();
        List<Map<String, Object>> highlights = new ArrayList<>();
        for (TrainingNote note : notes) {
            int start = note.getStartSec() != null ? note.getStartSec() : 0;
            int end = note.getEndSec() != null ? note.getEndSec() : start + 15;
            Map<String, Object> h = new LinkedHashMap<>();
            h.put("startSec", start);
            h.put("endSec", end);
            h.put("label", note.getLabel() != null ? note.getLabel() : note.getContent());
            if (StringUtils.hasText(baseUrl)) {
                h.put("clipUrl", baseUrl + "#t=" + start + "," + end);
            }
            highlights.add(h);
        }

        int duration = 0;
        if (record.getStartedAt() != null && record.getEndedAt() != null) {
            duration = (int) java.time.Duration.between(record.getStartedAt(), record.getEndedAt()).getSeconds();
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("recordingUrl", baseUrl);
        data.put("recordingStatus", recordingStatus);
        data.put("expired", expired);
        data.put("durationSeconds", duration);
        data.put("highlights", highlights);
        data.put("expiresAt", record.getRecordingExpiresAt() != null
                ? record.getRecordingExpiresAt().toString()
                : LocalDateTime.now().plusDays(7).toString());
        return data;
    }

    private void assertCanView(TrainingRecord record, Long requesterId) {
        if (Objects.equals(record.getUserId(), requesterId)) {
            return;
        }
        if (record.getOrderId() != null) {
            Order order = orderMapper.selectById(record.getOrderId());
            if (order != null && (Objects.equals(order.getCoachId(), requesterId)
                    || Objects.equals(order.getUserId(), requesterId))) {
                return;
            }
        }
        throw new ForbiddenOperationException("无权查看该录制");
    }
}
