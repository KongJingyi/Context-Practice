package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoomLiveStateResponse {

    private RoomPressureModeView pressureMode;
    private RoomWhiteboardView whiteboard;
    private LocalDateTime serverTime;
}
