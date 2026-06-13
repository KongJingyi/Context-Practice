package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RoomJoinInfoResponse {

    private String roomId;
    private Long trainingId;
    private Long orderId;
    private String trainingStatus;
    private Long sdkAppId;
    private String trtcUserId;
    private String userSig;
    private String role;
    private RoomPeerResponse peer;
    private String sceneName;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private LocalDateTime startedAt;
    private LocalDateTime serverTime;
    private Boolean canEnter;
    private String denyReason;
    private List<RoomParticipantResponse> participants;
}
