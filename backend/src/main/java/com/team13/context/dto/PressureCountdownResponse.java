package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PressureCountdownResponse {

    private Boolean active;
    private Integer secondsLeft;
    private Integer totalSeconds;
}
