package com.team13.context.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoomWhiteboardStrokeView {

    private String id;
    private String color;
    private int width;
    private List<PointView> points;

    @Data
    @Builder
    public static class PointView {
        private double x;
        private double y;
    }
}
