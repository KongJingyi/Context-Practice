package com.team13.context.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class WhiteboardStrokeBatchRequest {

    @NotEmpty
    @Valid
    private List<WhiteboardStrokeInput> strokes;
}
