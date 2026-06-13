package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoomCountdownView {

    private Boolean active;
    private Integer secondsLeft;
    private Integer totalSeconds;
}
