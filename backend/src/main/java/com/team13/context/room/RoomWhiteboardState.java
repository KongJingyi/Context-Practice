package com.team13.context.room;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RoomWhiteboardState {

    private boolean active;
    private int version;
    private List<RoomWhiteboardStroke> strokes = new ArrayList<>();
}
