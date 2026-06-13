package com.team13.context.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class WhiteboardStrokeInput {

    private String id;
    private String color;
    private int width;

    @NotNull
    @NotEmpty
    @Valid
    private List<PointInput> points;

    @Data
    public static class PointInput {
        private double x;
        private double y;
    }
}
