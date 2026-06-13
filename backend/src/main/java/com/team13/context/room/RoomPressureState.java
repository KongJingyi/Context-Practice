package com.team13.context.room;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomPressureState {

    private boolean enabled;

    private Countdown countdown = new Countdown();

    private Interrupt lastInterrupt;

    private Question currentQuestion;

    @Data
    public static class Countdown {
        private boolean active;
        private Integer totalSeconds;
        /** 倒计时开始时刻（毫秒时间戳） */
        private Long startedAtEpochMs;
    }

    @Data
    public static class Interrupt {
        private String message;
        private LocalDateTime at;
    }

    @Data
    public static class Question {
        private Long questionId;
        private String text;
        private LocalDateTime at;
    }
}
