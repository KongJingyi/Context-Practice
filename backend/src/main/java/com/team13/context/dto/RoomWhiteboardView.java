package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoomWhiteboardView {

    private boolean active;
    private int version;
    private List<RoomWhiteboardStrokeView> strokes;
}
