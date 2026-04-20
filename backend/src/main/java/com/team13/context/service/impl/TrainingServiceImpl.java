package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.ai.AiReportFacade;
import com.team13.context.dto.AiTrainingReportRequest;
import com.team13.context.dto.TrainingEndRequest;
import com.team13.context.dto.TrainingStartRequest;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.mapper.TrainingRecordMapper;
import com.team13.context.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRecordMapper trainingRecordMapper;
    private final AiReportFacade aiReportFacade;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startTraining(TrainingStartRequest request) {
        String roomId = IdWorker.get32UUID();
        TrainingRecord record = new TrainingRecord();
        record.setUserId(request.getUserId());
        record.setOrderId(request.getOrderId());
        record.setRoomId(roomId);
        record.setStatus("IN_PROGRESS");
        record.setStartedAt(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        trainingRecordMapper.insert(record);
        return roomId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endTraining(TrainingEndRequest request) {
        TrainingRecord record = trainingRecordMapper.selectOne(
                Wrappers.<TrainingRecord>lambdaQuery()
                        .eq(TrainingRecord::getRoomId, request.getRoomId())
                        .eq(TrainingRecord::getUserId, request.getUserId())
                        .last("LIMIT 1"));
        if (record == null) {
            return;
        }
        record.setStatus("ENDED");
        record.setEndedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        trainingRecordMapper.updateById(record);
    }

    @Override
    public String generateAiReport(AiTrainingReportRequest request) {
        TrainingRecord record = trainingRecordMapper.selectById(request.getTrainingRecordId());
        if (record == null) {
            throw new IllegalArgumentException("训练记录不存在");
        }
        return aiReportFacade.generateTrainingReport(record, null);
    }
}
