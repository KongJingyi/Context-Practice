package com.team13.context.room;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomChatMessage {

    private String id;

    private String fromRole;

    private Long fromUserId;

    private String text;

    private LocalDateTime createdAt;
}
