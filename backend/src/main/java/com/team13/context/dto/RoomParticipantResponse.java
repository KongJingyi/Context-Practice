package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoomParticipantResponse {

    private String role;
    private Long userId;
    private Boolean joined;
    private LocalDateTime joinedAt;
}
