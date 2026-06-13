package com.team13.context.trtc;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.config.TrtcProperties;
import com.team13.context.entity.TrainingParticipant;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.mapper.TrainingParticipantMapper;
import com.team13.context.mapper.TrainingRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * TRTC 云录制生命周期：双人进房后启动，结束训练后停止；VOD 回调写入 playback URL。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrtcCloudRecordingService {

    public static final String STATUS_IDLE = "IDLE";
    public static final String STATUS_RECORDING = "RECORDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_READY = "READY";
    public static final String STATUS_FAILED = "FAILED";

    private final TrainingRecordMapper trainingRecordMapper;
    private final TrainingParticipantMapper trainingParticipantMapper;
    private final TrtcProperties trtcProperties;
    private final TencentTrtcCloudRecordingClient tencentClient;

    @Transactional(rollbackFor = Exception.class)
    public void maybeStartRecording(Long trainingId) {
        if (trainingId == null) {
            return;
        }
        TrainingRecord record = trainingRecordMapper.selectById(trainingId);
        if (record == null || !StringUtils.hasText(record.getRoomId())) {
            return;
        }
        if (STATUS_RECORDING.equals(record.getRecordingStatus())) {
            return;
        }
        long activeJoins = trainingParticipantMapper.selectCount(
                Wrappers.<TrainingParticipant>lambdaQuery()
                        .eq(TrainingParticipant::getTrainingId, trainingId)
                        .isNotNull(TrainingParticipant::getJoinedAt)
                        .isNull(TrainingParticipant::getLeftAt));
        if (activeJoins < 2) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (tencentClient.isEnabled()) {
            try {
                String taskId = tencentClient.startRecording(trainingId, record.getRoomId());
                record.setRecordingStatus(STATUS_RECORDING);
                record.setTrtcTaskId(taskId);
                record.setUpdatedAt(now);
                trainingRecordMapper.updateById(record);
                log.info("Started TRTC cloud recording trainingId={} taskId={}", trainingId, taskId);
                return;
            } catch (TrtcRecordingException ex) {
                log.error("Start cloud recording failed trainingId={}: {}", trainingId, ex.getMessage());
                record.setRecordingStatus(STATUS_FAILED);
                record.setUpdatedAt(now);
                trainingRecordMapper.updateById(record);
                return;
            }
        }

        record.setRecordingStatus(STATUS_RECORDING);
        record.setTrtcTaskId("DEV-" + trainingId);
        record.setUpdatedAt(now);
        trainingRecordMapper.updateById(record);
        log.info("Started dev recording placeholder trainingId={}", trainingId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void stopAndFinalize(Long trainingId) {
        if (trainingId == null) {
            return;
        }
        TrainingRecord record = trainingRecordMapper.selectById(trainingId);
        if (record == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();

        if (tencentClient.isEnabled() && StringUtils.hasText(record.getTrtcTaskId())) {
            try {
                tencentClient.stopRecording(record.getTrtcTaskId());
            } catch (TrtcRecordingException ex) {
                log.warn("Stop cloud recording failed trainingId={} taskId={}: {}",
                        trainingId, record.getTrtcTaskId(), ex.getMessage());
            }
            record.setRecordingStatus(STATUS_PROCESSING);
            record.setUpdatedAt(now);
            trainingRecordMapper.updateById(record);
            log.info("Cloud recording stopped, awaiting VOD callback trainingId={}", trainingId);
            return;
        }

        if (StringUtils.hasText(record.getRecordingUrl())) {
            record.setRecordingStatus(STATUS_READY);
            record.setRecordingExpiresAt(now.plusDays(trtcProperties.getRecordingRetentionDays()));
            record.setUpdatedAt(now);
            trainingRecordMapper.updateById(record);
            return;
        }

        record.setRecordingUrl(trtcProperties.getDevRecordingUrl());
        record.setRecordingStatus(STATUS_READY);
        record.setRecordingExpiresAt(now.plusDays(trtcProperties.getRecordingRetentionDays()));
        record.setUpdatedAt(now);
        trainingRecordMapper.updateById(record);
        log.info("Finalized dev recording trainingId={}", trainingId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleCallback(Map<String, Object> payload) {
        TrtcRecordingCallbackParser.ParsedCallback parsed = TrtcRecordingCallbackParser.parse(payload);
        if (!parsed.hasRecordingUrl()) {
            log.debug("Recording callback ignored: no video url, taskId={}", parsed.taskId());
            return;
        }

        TrainingRecord record = null;
        if (parsed.trainingId() != null) {
            record = trainingRecordMapper.selectById(parsed.trainingId());
        } else if (StringUtils.hasText(parsed.taskId())) {
            record = trainingRecordMapper.selectOne(
                    Wrappers.<TrainingRecord>lambdaQuery()
                            .eq(TrainingRecord::getTrtcTaskId, parsed.taskId())
                            .last("LIMIT 1"));
        }
        if (record == null) {
            log.warn("Recording callback: training not found taskId={}", parsed.taskId());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        record.setRecordingUrl(parsed.recordingUrl());
        record.setRecordingStatus(STATUS_READY);
        record.setRecordingExpiresAt(now.plusDays(trtcProperties.getRecordingRetentionDays()));
        record.setUpdatedAt(now);
        trainingRecordMapper.updateById(record);
        log.info("Recording callback applied trainingId={} taskId={}", record.getId(), parsed.taskId());
    }
}
