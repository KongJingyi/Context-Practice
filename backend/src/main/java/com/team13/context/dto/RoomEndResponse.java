package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoomEndResponse {

    private Long trainingId;
    private LocalDateTime endedAt;
    private Long durationSeconds;
    private String orderStatus;
    private Boolean reportReady;
}
