package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoomJoinResponse {

    private LocalDateTime joinedAt;
    private String role;
}
