package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomPressureModeView {

    private Boolean enabled;
    private RoomCountdownView countdown;
    private RoomInterruptView lastInterrupt;
    private RoomQuestionView currentQuestion;
}
