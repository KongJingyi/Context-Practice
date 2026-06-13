package com.team13.context.room;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RoomWhiteboardStroke {

    private String id;
    private String color;
    private int width;
    private List<Point> points = new ArrayList<>();

    @Data
    public static class Point {
        private double x;
        private double y;
    }
}
