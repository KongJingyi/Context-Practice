package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {

    private String id;
    private String fromRole;
    private Long fromUserId;
    private String text;
    private LocalDateTime createdAt;
}
