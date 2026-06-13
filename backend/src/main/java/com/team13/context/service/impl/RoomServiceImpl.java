package com.team13.context.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.team13.context.common.ForbiddenOperationException;
import com.team13.context.common.UserContext;
import com.team13.context.constant.BusinessStatuses;
import com.team13.context.constant.ParticipantRoles;
import com.team13.context.constant.TrainingStatuses;
import com.team13.context.dto.RoomDetailResponse;
import com.team13.context.dto.RoomEndRequest;
import com.team13.context.dto.RoomEndResponse;
import com.team13.context.dto.RoomJoinInfoResponse;
import com.team13.context.dto.RoomJoinResponse;
import com.team13.context.dto.RoomLeaveRequest;
import com.team13.context.dto.RoomLeaveResponse;
import com.team13.context.dto.RoomParticipantResponse;
import com.team13.context.dto.RoomPeerResponse;
import com.team13.context.dto.TrainingEndRequest;
import com.team13.context.entity.Order;
import com.team13.context.entity.Scene;
import com.team13.context.entity.TrainingParticipant;
import com.team13.context.entity.TrainingRecord;
import com.team13.context.mapper.OrderMapper;
import com.team13.context.mapper.SceneMapper;
import com.team13.context.mapper.TrainingParticipantMapper;
import com.team13.context.mapper.TrainingRecordMapper;
import com.team13.context.room.RoomLiveStore;
import com.team13.context.service.RoomService;
import com.team13.context.service.TrainingService;
import com.team13.context.service.support.RoomAccessHelper;
import com.team13.context.service.support.RoomContext;
import com.team13.context.service.support.RoomContextSupport;
import com.team13.context.service.support.UserDisplayHelper;
import com.team13.context.trtc.TrtcCloudRecordingService;
import com.team13.context.trtc.TrtcTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final TrainingRecordMapper trainingRecordMapper;
    private final TrainingParticipantMapper trainingParticipantMapper;
    private final OrderMapper orderMapper;
    private final SceneMapper sceneMapper;
    private final TrtcTokenService trtcTokenService;
    private final RoomAccessHelper roomAccessHelper;
    private final UserDisplayHelper userDisplayHelper;
    private final TrainingService trainingService;
    private final RoomContextSupport roomContextSupport;
    private final RoomLiveStore roomLiveStore;
    private final TrtcCloudRecordingService trtcCloudRecordingService;

    @Override
    public RoomJoinInfoResponse getJoinInfo(String roomId) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        LocalDateTime now = LocalDateTime.now();
        RoomAccessHelper.EnterDecision decision = roomAccessHelper.evaluateEnter(ctx.order(), ctx.training(), now);
        if (ctx.training() == null) {
            decision = new RoomAccessHelper.EnterDecision(false, "训练尚未开始，请等待用户发起训练");
        }

        String role = roomContextSupport.resolveRole(ctx);
        String trtcUserId = TrtcTokenService.toTrtcUserId(UserContext.requireUserId(), role);
        Long peerUserId = peerUserId(ctx, role);

        return RoomJoinInfoResponse.builder()
                .roomId(roomId)
                .trainingId(ctx.training() != null ? ctx.training().getId() : null)
                .orderId(ctx.order().getId())
                .trainingStatus(ctx.training() != null ? ctx.training().getStatus() : null)
                .sdkAppId(trtcTokenService.resolveSdkAppId())
                .trtcUserId(trtcUserId)
                .userSig(trtcTokenService.generateUserSig(trtcUserId))
                .role(role)
                .peer(buildPeer(peerUserId, oppositeRole(role)))
                .sceneName(resolveSceneName(ctx.order().getSceneId()))
                .scheduledStart(ctx.order().getScheduledStart())
                .scheduledEnd(ctx.order().getScheduledEnd())
                .startedAt(ctx.training() != null ? ctx.training().getStartedAt() : null)
                .serverTime(now)
                .canEnter(decision.canEnter())
                .denyReason(decision.denyReason())
                .participants(buildParticipants(ctx))
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomJoinResponse joinRoom(String roomId) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        if (ctx.training() == null) {
            throw new IllegalArgumentException("训练尚未开始，无法进房");
        }
        RoomAccessHelper.EnterDecision decision =
                roomAccessHelper.evaluateEnter(ctx.order(), ctx.training(), LocalDateTime.now());
        if (!decision.canEnter()) {
            throw new IllegalArgumentException(decision.denyReason());
        }

        String role = roomContextSupport.resolveRole(ctx);
        LocalDateTime now = LocalDateTime.now();
        TrainingParticipant participant = trainingParticipantMapper.selectOne(
                Wrappers.<TrainingParticipant>lambdaQuery()
                        .eq(TrainingParticipant::getTrainingId, ctx.training().getId())
                        .eq(TrainingParticipant::getUserId, UserContext.requireUserId())
                        .eq(TrainingParticipant::getRole, role)
                        .last("LIMIT 1"));
        if (participant == null) {
            participant = new TrainingParticipant();
            participant.setTrainingId(ctx.training().getId());
            participant.setUserId(UserContext.requireUserId());
            participant.setRole(role);
            participant.setJoinedAt(now);
            participant.setCreatedAt(now);
            participant.setUpdatedAt(now);
            trainingParticipantMapper.insert(participant);
        } else {
            participant.setJoinedAt(now);
            participant.setLeftAt(null);
            participant.setUpdatedAt(now);
            trainingParticipantMapper.updateById(participant);
        }

        trtcCloudRecordingService.maybeStartRecording(ctx.training().getId());

        return RoomJoinResponse.builder().joinedAt(now).role(role).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomLeaveResponse leaveRoom(String roomId, RoomLeaveRequest request) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        if (ctx.training() == null) {
            throw new IllegalArgumentException("训练尚未开始");
        }

        String role = roomContextSupport.resolveRole(ctx);
        TrainingParticipant participant = trainingParticipantMapper.selectOne(
                Wrappers.<TrainingParticipant>lambdaQuery()
                        .eq(TrainingParticipant::getTrainingId, ctx.training().getId())
                        .eq(TrainingParticipant::getUserId, UserContext.requireUserId())
                        .eq(TrainingParticipant::getRole, role)
                        .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (participant == null) {
            participant = new TrainingParticipant();
            participant.setTrainingId(ctx.training().getId());
            participant.setUserId(UserContext.requireUserId());
            participant.setRole(role);
            participant.setJoinedAt(now);
            participant.setLeftAt(now);
            participant.setCreatedAt(now);
            participant.setUpdatedAt(now);
            trainingParticipantMapper.insert(participant);
        } else {
            participant.setLeftAt(now);
            participant.setUpdatedAt(now);
            trainingParticipantMapper.updateById(participant);
        }
        return RoomLeaveResponse.builder().leftAt(now).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomEndResponse endRoom(String roomId, RoomEndRequest request) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        String role = roomContextSupport.resolveRole(ctx);
        if (!ParticipantRoles.COACH.equals(role)) {
            throw new ForbiddenOperationException("仅陪练员可结束训练");
        }
        if (ctx.training() == null) {
            throw new IllegalArgumentException("训练尚未开始");
        }

        TrainingEndRequest endRequest = new TrainingEndRequest();
        endRequest.setRoomId(roomId);
        endRequest.setTranscript(request != null ? request.getTranscript() : null);
        endRequest.setSceneName(request != null ? request.getSceneName() : resolveSceneName(ctx.order().getSceneId()));
        trainingService.endTraining(endRequest);
        trtcCloudRecordingService.stopAndFinalize(ctx.training().getId());
        roomLiveStore.clearRoom(roomId);

        TrainingRecord refreshed = trainingRecordMapper.selectById(ctx.training().getId());
        Order order = orderMapper.selectById(ctx.order().getId());
        long durationSeconds = 0L;
        if (refreshed.getStartedAt() != null && refreshed.getEndedAt() != null) {
            durationSeconds = Duration.between(refreshed.getStartedAt(), refreshed.getEndedAt()).getSeconds();
        }

        return RoomEndResponse.builder()
                .trainingId(refreshed.getId())
                .endedAt(refreshed.getEndedAt())
                .durationSeconds(durationSeconds)
                .orderStatus(order != null ? order.getStatus() : null)
                .reportReady(TrainingStatuses.REPORT_READY.equals(refreshed.getStatus()))
                .build();
    }

    @Override
    public RoomDetailResponse getRoomDetail(String roomId) {
        RoomContext ctx = roomContextSupport.loadRoomContext(roomId);
        roomContextSupport.assertParticipant(ctx);
        LocalDateTime now = LocalDateTime.now();
        Long durationSeconds = null;
        if (ctx.training() != null && ctx.training().getStartedAt() != null) {
            LocalDateTime end = ctx.training().getEndedAt() != null ? ctx.training().getEndedAt() : now;
            durationSeconds = Duration.between(ctx.training().getStartedAt(), end).getSeconds();
        }
        return RoomDetailResponse.builder()
                .roomId(roomId)
                .trainingId(ctx.training() != null ? ctx.training().getId() : null)
                .orderId(ctx.order().getId())
                .trainingStatus(ctx.training() != null ? ctx.training().getStatus() : null)
                .orderStatus(ctx.order().getStatus())
                .startedAt(ctx.training() != null ? ctx.training().getStartedAt() : null)
                .endedAt(ctx.training() != null ? ctx.training().getEndedAt() : null)
                .durationSeconds(durationSeconds)
                .serverTime(now)
                .participants(buildParticipants(ctx))
                .build();
    }

    private Long peerUserId(RoomContext ctx, String role) {
        if (ParticipantRoles.COACH.equals(role)) {
            return ctx.order().getUserId();
        }
        return ctx.order().getCoachId();
    }

    private static String oppositeRole(String role) {
        return ParticipantRoles.COACH.equals(role) ? ParticipantRoles.USER : ParticipantRoles.COACH;
    }

    private RoomPeerResponse buildPeer(Long userId, String role) {
        if (userId == null) {
            return null;
        }
        boolean coachSide = ParticipantRoles.COACH.equals(role);
        return RoomPeerResponse.builder()
                .userId(userId)
                .trtcUserId(TrtcTokenService.toTrtcUserId(userId, role))
                .nickname(userDisplayHelper.resolveNickname(userId, coachSide))
                .avatar(userDisplayHelper.resolveAvatar(userId, coachSide))
                .build();
    }

    private List<RoomParticipantResponse> buildParticipants(RoomContext ctx) {
        List<RoomParticipantResponse> list = new ArrayList<>();
        list.add(buildParticipantSlot(
                ctx, ParticipantRoles.USER, ctx.order().getUserId()));
        if (ctx.order().getCoachId() != null) {
            list.add(buildParticipantSlot(
                    ctx, ParticipantRoles.COACH, ctx.order().getCoachId()));
        }
        return list;
    }

    private RoomParticipantResponse buildParticipantSlot(RoomContext ctx, String role, Long userId) {
        if (ctx.training() == null || userId == null) {
            return RoomParticipantResponse.builder()
                    .role(role)
                    .userId(userId)
                    .joined(false)
                    .joinedAt(null)
                    .build();
        }
        TrainingParticipant participant = trainingParticipantMapper.selectOne(
                Wrappers.<TrainingParticipant>lambdaQuery()
                        .eq(TrainingParticipant::getTrainingId, ctx.training().getId())
                        .eq(TrainingParticipant::getUserId, userId)
                        .eq(TrainingParticipant::getRole, role)
                        .last("LIMIT 1"));
        boolean joined = participant != null
                && participant.getJoinedAt() != null
                && (participant.getLeftAt() == null || participant.getJoinedAt().isAfter(participant.getLeftAt()));
        return RoomParticipantResponse.builder()
                .role(role)
                .userId(userId)
                .joined(joined)
                .joinedAt(participant != null ? participant.getJoinedAt() : null)
                .build();
    }

    private String resolveSceneName(Long sceneId) {
        if (sceneId == null) {
            return null;
        }
        Scene scene = sceneMapper.selectById(sceneId);
        return scene != null ? scene.getName() : null;
    }
}
