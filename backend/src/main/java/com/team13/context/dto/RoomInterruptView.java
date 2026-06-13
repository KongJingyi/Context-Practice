package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoomInterruptView {

    private String message;
    private LocalDateTime at;
}
