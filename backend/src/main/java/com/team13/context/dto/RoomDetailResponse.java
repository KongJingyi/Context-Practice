package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RoomDetailResponse {

    private String roomId;
    private Long trainingId;
    private Long orderId;
    private String trainingStatus;
    private String orderStatus;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Long durationSeconds;
    private LocalDateTime serverTime;
    private List<RoomParticipantResponse> participants;
}
